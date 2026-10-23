package ru.terracraft.welcome.core.lang;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Обёртка над YamlConfiguration для языковых файлов.
 * Сохраняет старый API (getString/getStringList/has/keys),
 * но данные берутся из настоящего YAML.
 */
public class LangConfig {

    private final YamlConfiguration yaml;

    private LangConfig(YamlConfiguration yaml) {
        this.yaml = yaml;
    }

    public static LangConfig load(File file) throws IOException {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        // Если файл пустой — попробуем загрузить defaults из ресурсов
        if (yaml.getKeys(false).isEmpty()) {
            try (InputStream in = LangConfig.class.getClassLoader()
                    .getResourceAsStream("lang/" + file.getName())) {
                if (in != null) {
                    YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                            new InputStreamReader(in, StandardCharsets.UTF_8));
                    yaml.setDefaults(defaults);
                }
            }
        }
        return new LangConfig(yaml);
    }

    public static LangConfig load(InputStream in) throws IOException {
        return new LangConfig(YamlConfiguration.loadConfiguration(
                new InputStreamReader(in, StandardCharsets.UTF_8)));
    }

    // ═══════════════ PUBLIC API (совместим со старым LangFile) ═══════════════

    public String getString(String key) {
        if (key == null || key.isEmpty()) return null;
        String value = yaml.getString(key);
        if (value != null) return value;

        // fallback: если это был список — вернём первую строку
        List<String> list = yaml.getStringList(key);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<String> getStringList(String key) {
        if (key == null || key.isEmpty()) return List.of();

        // 1. Нативный YAML-список
        if (yaml.isList(key)) {
            return yaml.getStringList(key);
        }

        // 2. Одиночная строка → singleton
        String direct = yaml.getString(key);
        if (direct != null) {
            return List.of(direct);
        }

        // 3. legacy-формат key.1, key.2 (на случай миграции)
        ConfigurationSection section = yaml.getConfigurationSection(key);
        if (section != null) {
            List<String> legacy = section.getKeys(false).stream()
                    .filter(k -> k.chars().allMatch(Character::isDigit))
                    .sorted((a, b) -> Integer.compare(Integer.parseInt(a), Integer.parseInt(b)))
                    .map(section::getString)
                    .filter(v -> v != null && !v.isEmpty())
                    .toList();
            if (!legacy.isEmpty()) return legacy;
        }

        return List.of();
    }

    public boolean has(String key) {
        return key != null && (yaml.contains(key) || yaml.isList(key));
    }

    public Set<String> keys() {
        return Collections.unmodifiableSet(yaml.getKeys(true));
    }
}