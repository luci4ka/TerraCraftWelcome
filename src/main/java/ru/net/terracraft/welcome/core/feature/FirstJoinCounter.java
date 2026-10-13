package ru.net.terracraft.welcome.core.feature;

import org.bukkit.configuration.file.YamlConfiguration;
import ru.net.terracraft.welcome.WelcomePlugin;

import java.io.File;
import java.util.UUID;

/**
 * Счётчик первых входов.
 * При первом входе игрока выдаёт его "номер" (#1, #2, #3...).
 * Хранит в firstjoins.yml: UUID → номер.
 */
public class FirstJoinCounter {

    private static final String FILE_NAME = "firstjoins.yml";
    private static final String TOTAL_KEY = "_total";

    private final WelcomePlugin plugin;
    private final File file;
    private YamlConfiguration yaml;

    public FirstJoinCounter(WelcomePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), FILE_NAME);
        load();
    }

    // ═══════════════ LOAD / SAVE ═══════════════

    private void load() {
        if (!file.exists()) {
            this.yaml = new YamlConfiguration();
            this.yaml.set(TOTAL_KEY, 0);
            save();
            return;
        }

        this.yaml = YamlConfiguration.loadConfiguration(file);

        // Если файл есть, но нет ключа total — инициализируем
        if (!yaml.isInt(TOTAL_KEY)) {
            yaml.set(TOTAL_KEY, 0);
            save();
        }
    }

    private void save() {
        try {
            yaml.save(file);
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка сохранения " + FILE_NAME + ": " + e.getMessage());
        }
    }

    // ═══════════════ PUBLIC API ═══════════════

    /**
     * Возвращает номер игрока.
     * Если игрок уже был — возвращает его номер.
     * Если игрок новый — увеличивает счётчик и записывает.
     *
     * @return номер игрока (начиная с 1), или -1 при ошибке
     */
    public int getOrAssign(UUID uuid) {
        if (uuid == null) return -1;

        String key = uuid.toString();

        // Уже есть — возвращаем
        if (yaml.isInt(key)) {
            return yaml.getInt(key);
        }

        // Новый — увеличиваем счётчик
        int total = yaml.getInt(TOTAL_KEY, 0) + 1;
        yaml.set(TOTAL_KEY, total);
        yaml.set(key, total);
        save();

        return total;
    }

    /**
     * Просто получить номер без увеличения.
     * @return номер или -1, если игрок не зарегистрирован
     */
    public int getNumber(UUID uuid) {
        if (uuid == null) return -1;
        String key = uuid.toString();
        return yaml.isInt(key) ? yaml.getInt(key) : -1;
    }

    /**
     * Общее количество первых входов.
     */
    public int getTotal() {
        return yaml.getInt(TOTAL_KEY, 0);
    }
}