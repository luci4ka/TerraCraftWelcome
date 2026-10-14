package ru.terracraft.welcome.core.i18n;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import ru.terracraft.welcome.WelcomePlugin;
import ru.terracraft.welcome.core.util.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Единый менеджер i18n.
 * Загрузка языков из lang/*.lang, получение строк, рендер в Component.
 * Поддерживает локаль игрока (Minecraft locale).
 */
public class LanguageManager {

    private static final String DEFAULT_LANGUAGE = "ru_ru";
    private static final String LANG_EXTENSION = ".lang";

    private final WelcomePlugin plugin;
    private final Map<String, LangFile> languages = new HashMap<>();
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
        }

        saveDefaultLanguages();

        File[] files = langFolder.listFiles((dir, name) -> name.endsWith(LANG_EXTENSION));
        if (files == null || files.length == 0) {
            plugin.getLogger().warning("Не найдено ни одного языкового файла в lang/");
            if (!currentLanguage.equals(DEFAULT_LANGUAGE)) {
                currentLanguage = DEFAULT_LANGUAGE;
            }
            return;
        }

        for (File file : files) {
            String code = file.getName()
                    .substring(0, file.getName().length() - LANG_EXTENSION.length())
                    .toLowerCase();
            try {
                languages.put(code, LangFile.load(file));
                plugin.getLogger().info("Загружен язык: " + code);
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка загрузки " + file.getName() + ": " + e.getMessage());
            }
        }

        if (!languages.containsKey(currentLanguage)) {
            plugin.getLogger().warning("Язык '" + currentLanguage + "' не найден — использую " + DEFAULT_LANGUAGE);
            currentLanguage = DEFAULT_LANGUAGE;
        }

        plugin.getLogger().info("Загружено языков: " + languages.size());
    }

    private void saveDefaultLanguages() {
        for (String lang : List.of("ru_ru", "en_us")) {
            File target = new File(plugin.getDataFolder(), "lang/" + lang + LANG_EXTENSION);
            if (!target.exists()) {
                try {
                    plugin.saveResource("lang/" + lang + LANG_EXTENSION, false);
                } catch (Exception e) {
                    plugin.getLogger().warning("Не удалось сохранить lang/" + lang + LANG_EXTENSION + ": " + e.getMessage());
                }
            }
        }
    }

    // ═══════════════ PLAYER LOCALE ═══════════════

    public String getPlayerLocale(Player player) {
        if (player == null) return currentLanguage;

        Locale locale = player.locale();
        if (locale == null) return currentLanguage;

        // 1. Полный код: ru_ru, en_us
        String full = locale.toString().toLowerCase();
        if (languages.containsKey(full)) return full;

        // 2. Язык + страна: ru_ru
        String lang = locale.getLanguage().toLowerCase();
        String withCountry = lang + "_" + locale.getCountry().toLowerCase();
        if (languages.containsKey(withCountry)) return withCountry;

        // 3. Только язык: ru
        if (languages.containsKey(lang)) return lang;

        // 4. Поиск по префиксу: ru → ru_ru, en → en_us
        for (String code : languages.keySet()) {
            if (code.startsWith(lang + "_")) {
                return code;
            }
        }

        // 5. Fallback на currentLanguage / DEFAULT
        if (languages.containsKey(currentLanguage)) return currentLanguage;
        return DEFAULT_LANGUAGE;
    }

    // ═══════════════ GET STRING (MessageKey) ═══════════════

    public String getMessage(MessageKey key) {
        return getMessage(key, "<red>Missing: " + key.path() + "</red>");
    }

    public String getMessage(MessageKey key, String def) {
        LangFile lang = currentLang();
        if (lang == null) return def;
        String value = lang.getString(key.path());
        return value != null ? value : def;
    }

    public List<String> getMessageList(MessageKey key) {
        LangFile lang = currentLang();
        if (lang == null) return List.of();
        return lang.getStringList(key.path());
    }

    // ═══════════════ GET STRING (MessageKey, Player) ═══════════════

    public String getMessage(Player player, MessageKey key) {
        return getMessage(player, key, "<red>Missing: " + key.path() + "</red>");
    }

    public String getMessage(Player player, MessageKey key, String def) {
        LangFile lang = langFor(player);
        if (lang == null) return def;
        String value = lang.getString(key.path());
        return value != null ? value : def;
    }

    public List<String> getMessageList(Player player, MessageKey key) {
        LangFile lang = langFor(player);
        if (lang == null) return List.of();
        return lang.getStringList(key.path());
    }

    // ═══════════════ GET STRING (String path) ═══════════════

    public String getString(String path) {
        if (path == null || path.isEmpty()) return "";
        LangFile lang = currentLang();
        if (lang == null) return "";
        String value = lang.getString(path);
        return value != null ? value : "";
    }

    public String getString(String path, String def) {
        String value = getString(path);
        return value == null || value.isEmpty() ? def : value;
    }

    public List<String> getMessageList(String path) {
        if (path == null || path.isEmpty()) return List.of();
        LangFile lang = currentLang();
        if (lang == null) return List.of();
        return lang.getStringList(path);
    }

    // ═══════════════ GET STRING (String path, Player) ═══════════════

    public String getString(Player player, String path) {
        if (path == null || path.isEmpty()) return "";
        LangFile lang = langFor(player);
        if (lang == null) return "";
        String value = lang.getString(path);
        return value != null ? value : "";
    }

    public String getString(Player player, String path, String def) {
        String value = getString(player, path);
        return value == null || value.isEmpty() ? def : value;
    }

    public List<String> getMessageList(Player player, String path) {
        if (path == null || path.isEmpty()) return List.of();
        LangFile lang = langFor(player);
        if (lang == null) return List.of();
        return lang.getStringList(path);
    }

    // ═══════════════ YAML/LANG HELPERS ═══════════════

    private LangFile currentLang() {
        LangFile lang = languages.get(currentLanguage);
        if (lang == null) lang = languages.get(DEFAULT_LANGUAGE);
        return lang;
    }

    private LangFile langFor(Player player) {
        if (player == null) return currentLang();
        String code = getPlayerLocale(player);
        LangFile lang = languages.get(code);
        if (lang == null) lang = currentLang();
        return lang;
    }

    // ═══════════════ RENDER ═══════════════

    public Component render(String text) {
        if (text == null || text.isEmpty()) return Component.empty();
        try {
            return miniMessage.deserialize(LangFile.unescape(text));
        } catch (Exception e) {
            return Component.text(text);
        }
    }

    public Component render(String text, Player player) {
        if (text == null || text.isEmpty()) return Component.empty();

        String withPlaceholders = Utils.applyPlaceholders(text, player);

        if (withPlaceholders.indexOf('%') >= 0) {
            withPlaceholders = Utils.stripUnknownPlaceholders(withPlaceholders);
        }

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