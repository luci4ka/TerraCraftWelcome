package com.luci.welcome.manager;

import com.luci.welcome.Main;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class LanguageManager {

    private final Main plugin;
    private final Map<String, YamlConfiguration> languages = new HashMap<>();
    private String currentLanguage;

    public LanguageManager(Main plugin) {
        this.plugin = plugin;
        this.currentLanguage = plugin.getConfig().getString("language", "ru");
        loadLanguages();
    }

    /**
     * Загружает все языковые файлы
     */
    public void loadLanguages() {
        languages.clear();

        File langFolder = new File(plugin.getDataFolder(), "languages");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
            saveDefaultLanguages();
        }

        File[] files = langFolder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.getName().endsWith(".yml")) {
                String lang = file.getName().replace(".yml", "");
                languages.put(lang, YamlConfiguration.loadConfiguration(file));
                plugin.getLogUtil().debug("Загружен язык: " + lang);
            }
        }

        plugin.getLogUtil().info("Загружено языков: " + languages.size());
    }

    /**
     * Перезагружает языковые файлы
     */
    public void reloadLanguages() {
        this.currentLanguage = plugin.getConfig().getString("language", "ru");
        loadLanguages();
    }

    /**
     * Сохраняет языковые файлы по умолчанию
     */
    private void saveDefaultLanguages() {
        String[] defaultLangs = {"ru.yml", "en.yml"};
        for (String lang : defaultLangs) {
            File langFile = new File(plugin.getDataFolder(), "languages/" + lang);
            if (!langFile.exists()) {
                plugin.saveResource("languages/" + lang, false);
            }
        }
    }

    /**
     * Получить сообщение по ключу
     */
    public String getMessage(String path) {
        return getMessage(path, "<red>Message not found: " + path + "</red>");
    }

    /**
     * Получить сообщение по ключу со значением по умолчанию
     */
    public String getMessage(String path, String def) {
        YamlConfiguration lang = languages.get(currentLanguage);
        if (lang == null) {
            // Пробуем русский как запасной
            lang = languages.get("ru");
        }
        if (lang != null) {
            String value = lang.getString(path);
            return value != null ? value : def;
        }
        return def;
    }

    /**
     * Получить список сообщений по ключу
     */
    public List<String> getMessageList(String path) {
        YamlConfiguration lang = languages.get(currentLanguage);
        if (lang == null) {
            lang = languages.get("ru");
        }
        if (lang != null) {
            List<String> list = lang.getStringList(path);
            return list != null ? list : new ArrayList<>();
        }
        return new ArrayList<>();
    }

    /**
     * Получить секцию сообщений
     */
    public Map<String, Object> getMessageSection(String path) {
        YamlConfiguration lang = languages.get(currentLanguage);
        if (lang == null) {
            lang = languages.get("ru");
        }
        if (lang != null) {
            return lang.getConfigurationSection(path) != null
                    ? lang.getConfigurationSection(path).getValues(false)
                    : new HashMap<>();
        }
        return new HashMap<>();
    }

    /**
     * Получить текущий язык
     */
    public String getCurrentLanguage() {
        return currentLanguage;
    }

    /**
     * Установить язык
     */
    public void setLanguage(String lang) {
        if (languages.containsKey(lang)) {
            this.currentLanguage = lang;
            plugin.getConfig().set("language", lang);
            plugin.saveConfig();
        }
    }

    /**
     * Получить список доступных языков
     */
    public Set<String> getAvailableLanguages() {
        return languages.keySet();
    }

    /**
     * Проверить существование языка
     */
    public boolean hasLanguage(String lang) {
        return languages.containsKey(lang);
    }
}