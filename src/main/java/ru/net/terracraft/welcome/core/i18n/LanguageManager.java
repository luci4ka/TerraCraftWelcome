package ru.net.terracraft.welcome.core.i18n;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import ru.net.terracraft.welcome.WelcomePlugin;
import ru.net.terracraft.welcome.core.util.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Единый менеджер i18n.
 * Загрузка языков из lang/*.yml, получение строк, рендер в Component.
 * Поддерживает локаль игрока (Minecraft locale).
 */
public class LanguageManager {

    private static final String DEFAULT_LANGUAGE = "ru";

    private final WelcomePlugin plugin;
    private final Map<String, YamlConfiguration> languages = new HashMap<>();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private String currentLanguage;

    public LanguageManager(WelcomePlugin plugin, String initialLanguage) {
        this.plugin = plugin;
        this.currentLanguage = initialLanguage != null ? initialLanguage : DEFAULT_LANGUAGE;
        loadLanguages();
    }

    // ═══════════════ LOADING ═══════════════

    public void loadLanguages() {
        languages.clear();

        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            if (!langFolder.mkdirs()) {
                plugin.getLogger().warning("Не удалось создать папку lang/");
            }
            saveDefaultLanguages();
        }

        File[] files = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            plugin.getLogger().warning("Не найдено ни одного языкового файла в lang/");
            return;
        }

        for (File file : files) {
            String code = file.getName().replace(".yml", "").toLowerCase();
            languages.put(code, YamlConfiguration.loadConfiguration(file));
            plugin.getLogger().info("Загружен язык: " + code);
        }

        if (!languages.containsKey(currentLanguage)) {
            plugin.getLogger().warning("Язык '" + currentLanguage + "' не найден — использую " + DEFAULT_LANGUAGE);
            currentLanguage = DEFAULT_LANGUAGE;
        }

        plugin.getLogger().info("Загружено языков: " + languages.size());
    }

    private void saveDefaultLanguages() {
        for (String lang : List.of("ru", "en")) {
            File target = new File(plugin.getDataFolder(), "lang/" + lang + ".yml");
            if (!target.exists()) {
                try {
                    plugin.saveResource("lang/" + lang + ".yml", false);
                } catch (Exception e) {
                    plugin.getLogger().warning("Не удалось сохранить lang/" + lang + ".yml: " + e.getMessage());
                }
            }
        }
    }

    // ═══════════════ PLAYER LOCALE ═══════════════

    /**
     * Определить локаль игрока по его клиенту.
     * Приоритет:
     *   1. Точное совпадение (ru_ru, en_us)
     *   2. Только язык (ru, en)
     *   3. currentLanguage плагина
     *   4. DEFAULT_LANGUAGE
     */
    public String getPlayerLocale(Player player) {
        if (player == null) return currentLanguage;

        Locale locale = player.locale();
        if (locale == null) return currentLanguage;

        // 1. Полный код: ru_ru, en_us
        String full = locale.toString().toLowerCase();
        if (languages.containsKey(full)) {
            return full;
        }

        // 2. Только язык: ru, en
        String lang = locale.getLanguage().toLowerCase();
        if (languages.containsKey(lang)) {
            return lang;
        }

        // 3. Fallback
        if (languages.containsKey(currentLanguage)) {
            return currentLanguage;
        }
        return DEFAULT_LANGUAGE;
    }

    // ═══════════════ GET STRING (MessageKey) ═══════════════

    public String getMessage(MessageKey key) {
        return getMessage(key, "<red>Missing: " + key.path() + "</red>");
    }

    public String getMessage(MessageKey key, String def) {
        YamlConfiguration yaml = currentYaml();
        if (yaml == null) return def;

        String value = yaml.getString(key.path());
        return value != null ? value : def;
    }

    public List<String> getMessageList(MessageKey key) {
        YamlConfiguration yaml = currentYaml();
        if (yaml == null) return List.of();

        List<String> list = yaml.getStringList(key.path());
        return list != null ? list : List.of();
    }

    // ═══════════════ GET STRING (MessageKey, Player) ═══════════════

    /**
     * Строка на локали игрока.
     */
    public String getMessage(Player player, MessageKey key) {
        return getMessage(player, key, "<red>Missing: " + key.path() + "</red>");
    }

    public String getMessage(Player player, MessageKey key, String def) {
        YamlConfiguration yaml = yamlFor(player);
        if (yaml == null) return def;

        String value = yaml.getString(key.path());
        return value != null ? value : def;
    }

    /**
     * Список строк на локали игрока.
     */
    public List<String> getMessageList(Player player, MessageKey key) {
        YamlConfiguration yaml = yamlFor(player);
        if (yaml == null) return List.of();

        List<String> list = yaml.getStringList(key.path());
        return list != null ? list : List.of();
    }

    // ═══════════════ GET STRING (String path) ═══════════════

    public String getString(String path) {
        if (path == null || path.isEmpty()) return "";
        YamlConfiguration yaml = currentYaml();
        if (yaml == null) return "";

        String value = yaml.getString(path);
        return value != null ? value : "";
    }

    public String getString(String path, String def) {
        String value = getString(path);
        return value == null || value.isEmpty() ? def : value;
    }

    public List<String> getMessageList(String path) {
        if (path == null || path.isEmpty()) return List.of();
        YamlConfiguration yaml = currentYaml();
        if (yaml == null) return List.of();

        List<String> list = yaml.getStringList(path);
        return list != null ? list : List.of();
    }

    // ═══════════════ GET STRING (String path, Player) ═══════════════

    /**
     * Строка по пути на локали игрока.
     */
    public String getString(Player player, String path) {
        if (path == null || path.isEmpty()) return "";
        YamlConfiguration yaml = yamlFor(player);
        if (yaml == null) return "";

        String value = yaml.getString(path);
        return value != null ? value : "";
    }

    public String getString(Player player, String path, String def) {
        String value = getString(player, path);
        return value == null || value.isEmpty() ? def : value;
    }

    /**
     * Список строк по пути на локали игрока.
     */
    public List<String> getMessageList(Player player, String path) {
        if (path == null || path.isEmpty()) return List.of();
        YamlConfiguration yaml = yamlFor(player);
        if (yaml == null) return List.of();

        List<String> list = yaml.getStringList(path);
        return list != null ? list : List.of();
    }

    // ═══════════════ YAML HELPERS ═══════════════

    private YamlConfiguration currentYaml() {
        YamlConfiguration yaml = languages.get(currentLanguage);
        if (yaml == null) yaml = languages.get(DEFAULT_LANGUAGE);
        return yaml;
    }

    private YamlConfiguration yamlFor(Player player) {
        if (player == null) return currentYaml();
        String code = getPlayerLocale(player);
        YamlConfiguration yaml = languages.get(code);
        if (yaml == null) yaml = currentYaml();
        return yaml;
    }

    // ═══════════════ RENDER ═══════════════

    public Component render(String text) {
        if (text == null || text.isEmpty()) return Component.empty();
        try {
            return miniMessage.deserialize(text);
        } catch (Exception e) {
            return Component.text(text);
        }
    }

    public Component render(String text, Player player) {
        if (text == null || text.isEmpty()) return Component.empty();
        String withPlaceholders = Utils.applyPlaceholders(text, player);
        return render(withPlaceholders);
    }

    public List<Component> renderList(List<String> lines, Player player) {
        if (lines == null || lines.isEmpty()) return List.of();
        return lines.stream()
                .map(line -> render(line, player))
                .collect(Collectors.toList());
    }

    public Component renderKey(MessageKey key, Player player) {
        return render(getMessage(key), player);
    }

    // ═══════════════ GETTERS / SETTERS ═══════════════

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public void setLanguage(String code) {
        if (languages.containsKey(code)) {
            this.currentLanguage = code;
            plugin.getLogger().info("Язык переключён на: " + code);
        } else {
            plugin.getLogger().warning("Язык '" + code + "' не найден");
        }
    }

    public Set<String> getAvailableLanguages() {
        return languages.keySet();
    }

    public boolean hasLanguage(String code) {
        return languages.containsKey(code);
    }
}