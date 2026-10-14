package ru.terracraft.welcome.config;

import de.exlll.configlib.ConfigLib;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import ru.terracraft.welcome.WelcomePlugin;

import java.nio.file.Path;

/**
 * Загрузчик конфига через ConfigLib.
 * Автоматически создаёт config.yml из POJO и обновляет его при изменении структуры.
 */
public class ConfigLoader {

    private static final String HEADER = """
            ═══════════════════════════════════════
             Welcome Plugin — Конфигурация
            ═══════════════════════════════════════
            """;

    private final WelcomePlugin plugin;
    private final Path configPath;
    private MainConfig config;

    public ConfigLoader(WelcomePlugin plugin) {
        this.plugin = plugin;
        this.configPath = plugin.getDataFolder().toPath().resolve("config.yml");
    }

    /**
     * Загружает (или создаёт) config.yml.
     */
    public void load() {
        this.config = YamlConfigurations.update(
                configPath,
                MainConfig.class,
                buildProperties()
        );

        plugin.getLogger().info("Конфигурация загружена (v" + config.configVersion + ")");
    }

    /**
     * Сохраняет текущий конфиг обратно в файл.
     */
    public void save() {
        if (config == null) {
            plugin.getLogger().warning("Нельзя сохранить конфиг — он не загружен");
            return;
        }

        YamlConfigurations.save(
                configPath,
                MainConfig.class,
                config,
                buildProperties()
        );

        plugin.getLogger().info("Конфигурация сохранена");
    }

    /**
     * Получить текущий конфиг.
     */
    public MainConfig get() {
        if (config == null) {
            throw new IllegalStateException("Конфиг не загружен — вызовите load()");
        }
        return config;
    }

    // ═══════════════ HELPERS ═══════════════

    private YamlConfigurationProperties buildProperties() {
        return ConfigLib.BUKKIT_DEFAULT_PROPERTIES.toBuilder()
                .header(HEADER)
                .build();
    }
}