package ru.terracraft.welcome.core.feature;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;
import ru.terracraft.welcome.WelcomePlugin;

import java.io.File;
import java.util.UUID;

/**
 * Счётчик первых входов.
 * При первом входе игрока выдаёт его "номер" (#1, #2, #3...).
 * Хранит в firstjoins.yml: UUID → номер.
 *
 * Запись на диск — асинхронная, раз в N секунд (если есть изменения).
 */
public class FirstJoinCounter {

    private static final String FILE_NAME = "firstjoins.yml";
    private static final String TOTAL_KEY = "_total";
    private static final long SAVE_INTERVAL_TICKS = 20L * 30;   // 30 секунд

    private final WelcomePlugin plugin;
    private final File file;
    private final Object lock = new Object();

    private YamlConfiguration yaml;
    private volatile boolean dirty = false;
    private BukkitTask saveTask;

    public FirstJoinCounter(WelcomePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), FILE_NAME);
        load();
        startAutoSave();
    }

    // ═══════════════ LOAD / SAVE ═══════════════

    private void load() {
        if (!file.exists()) {
            this.yaml = new YamlConfiguration();
            this.yaml.set(TOTAL_KEY, 0);
            saveSync();
            return;
        }

        this.yaml = YamlConfiguration.loadConfiguration(file);

        if (!yaml.isInt(TOTAL_KEY)) {
            yaml.set(TOTAL_KEY, 0);
            saveSync();
        }
    }

    /**
     * Синхронный save — только при инициализации и shutdown.
     */
    private void saveSync() {
        try {
            yaml.save(file);
            dirty = false;
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка сохранения " + FILE_NAME + ": " + e.getMessage());
        }
    }

    /**
     * Асинхронный save — вызывается из auto-save задачи.
     * Делаем копию YAML под lock, чтобы main-поток мог продолжать писать
     * в оригинал, пока async-поток сохраняет копию.
     */
    private void saveAsync() {
        if (!dirty) return;

        YamlConfiguration snapshot;
        synchronized (lock) {
            if (!dirty) return;
            snapshot = copyYaml(yaml);
            dirty = false;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                snapshot.save(file);
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка async-сохранения " + FILE_NAME + ": " + e.getMessage());
                dirty = true;   // попробуем в следующий раз
            }
        });
    }

    /**
     * Глубокая копия YamlConfiguration.
     * Нужна, потому что YamlConfiguration не потокобезопасен:
     * main-поток может писать в оригинал, пока async-поток сохраняет копию.
     */
    private YamlConfiguration copyYaml(YamlConfiguration source) {
        YamlConfiguration copy = new YamlConfiguration();
        for (String key : source.getKeys(true)) {
            copy.set(key, source.get(key));
        }
        return copy;
    }

    private void startAutoSave() {
        this.saveTask = plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                this::saveAsync,
                SAVE_INTERVAL_TICKS,
                SAVE_INTERVAL_TICKS
        );
    }

    /**
     * Останавливает автосейв и сохраняет остаток синхронно.
     * Вызывать при выключении плагина.
     */
    public void shutdown() {
        if (saveTask != null) {
            saveTask.cancel();
            saveTask = null;
        }
        saveSync();
    }

    // ═══════════════ PUBLIC API ═══════════════

    /**
     * Возвращает номер игрока.
     * Если игрок уже был — возвращает его номер.
     * Если игрок новый — увеличивает счётчик и записывает.
     *
     * @return номер игрока (начиная с 1), или -1 при ошибке
     */
    public synchronized int getOrAssign(UUID uuid) {
        if (uuid == null) return -1;

        String key = uuid.toString();

        if (yaml.isInt(key)) {
            return yaml.getInt(key);
        }

        int total = yaml.getInt(TOTAL_KEY, 0) + 1;
        yaml.set(TOTAL_KEY, total);
        yaml.set(key, total);
        dirty = true;

        return total;
    }

    /**
     * Просто получить номер без увеличения.
     *
     * @return номер или -1, если игрок не зарегистрирован
     */
    public int getNumber(UUID uuid) {
        if (uuid == null) return -1;
        String key = uuid.toString();
        synchronized (lock) {
            return yaml.isInt(key) ? yaml.getInt(key) : -1;
        }
    }

    /**
     * Общее количество первых входов.
     */
    public int getTotal() {
        synchronized (lock) {
            return yaml.getInt(TOTAL_KEY, 0);
        }
    }
}