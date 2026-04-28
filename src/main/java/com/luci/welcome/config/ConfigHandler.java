package com.luci.welcome.config;

import com.luci.welcome.Main;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ConfigHandler {

    private final Main plugin;
    private final Logger logger;
    private final Random random = new Random();
    private Map<String, LinkEntry> links = new HashMap<>();

    public ConfigHandler(Main plugin) {
        this.plugin = plugin;
        this.logger = LoggerFactory.getLogger(plugin.getName());
        loadLinks();
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        plugin.getLanguageManager().reloadLanguages(); // Перезагружаем языки
        loadLinks();
        logger.info("✓ Конфигурация перезагружена");
    }

    public void checkConfigVersion(double expectedVersion) {
        double actual = getDouble("config-version", -1);
        if (actual != expectedVersion) {
            logger.warn("⚠ Версия конфига ({}) не совпадает с ожидаемой ({}). Рекомендуется обновить конфиг!",
                    actual, expectedVersion);
        } else {
            logger.info("✓ Версия конфига актуальна: {}", actual);
        }
    }

    public String parsePlaceholders(Player player, String text) {
        if (text == null || text.isEmpty()) return "";

        if (plugin.getHookManager().isPapiEnabled() && player != null && player.isOnline()) {
            try {
                return plugin.getHookManager().getPapiHook().parse(player, text);
            } catch (Exception e) {
                logger.error("Ошибка парсинга плейсхолдеров для игрока: {}", player.getName(), e);
                return text;
            }
        }

        // Базовая замена плейсхолдеров, если PAPI не доступен
        if (player != null) {
            text = text.replace("%player%", player.getName())
                    .replace("%displayname%", player.getDisplayName())
                    .replace("%world%", player.getWorld().getName());
        }

        return text;
    }

    // Безопасные методы получения значений из config.yml
    public boolean getBoolean(String path, boolean def) {
        try {
            return getConfig().getBoolean(path, def);
        } catch (Exception e) {
            logger.warn("Ошибка чтения boolean из '{}': {}", path, e.getMessage());
            return def;
        }
    }

    public int getInt(String path, int def) {
        try {
            return getConfig().getInt(path, def);
        } catch (Exception e) {
            logger.warn("Ошибка чтения int из '{}': {}", path, e.getMessage());
            return def;
        }
    }

    public double getDouble(String path, double def) {
        try {
            return getConfig().getDouble(path, def);
        } catch (Exception e) {
            logger.warn("Ошибка чтения double из '{}': {}", path, e.getMessage());
            return def;
        }
    }

    public String getString(String path, String def) {
        try {
            String value = getConfig().getString(path);
            return value != null ? value : def;
        } catch (Exception e) {
            logger.warn("Ошибка чтения string из '{}': {}", path, e.getMessage());
            return def;
        }
    }

    public List<String> getStringList(String path) {
        try {
            List<String> value = getConfig().getStringList(path);
            return value != null ? value : new ArrayList<>();
        } catch (Exception e) {
            logger.warn("Ошибка чтения списка из '{}': {}", path, e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Map<?, ?>> getMapList(String path) {
        try {
            return getConfig().getMapList(path);
        } catch (Exception e) {
            logger.warn("Ошибка чтения map list из '{}': {}", path, e.getMessage());
            return new ArrayList<>();
        }
    }

    public ConfigurationSection getConfigurationSection(String path) {
        ConfigurationSection section = getConfig().getConfigurationSection(path);
        if (section == null) {
            logger.debug("Секция '{}' не найдена в конфиге", path);
        }
        return section;
    }

    public Set<String> getKeys(String path, boolean deep) {
        ConfigurationSection section = getConfigurationSection(path);
        if (section != null) {
            return section.getKeys(deep);
        }
        return new HashSet<>();
    }

    public boolean contains(String path) {
        return getConfig().contains(path);
    }

    // ========================================
    // Методы для работы с языковыми файлами
    // ========================================

    /**
     * Получить сообщение из языкового файла
     */
    public String getLangMessage(String path, String def) {
        return plugin.getLanguageManager().getMessage(path, def);
    }

    /**
     * Получить сообщение из языкового файла
     */
    public String getLangMessage(String path) {
        return plugin.getLanguageManager().getMessage(path);
    }

    /**
     * Получить список сообщений из языкового файла
     */
    public List<String> getLangMessageList(String path) {
        return plugin.getLanguageManager().getMessageList(path);
    }

    // Методы для звуков и частиц
    public boolean isSoundEnabled() {
        return getBoolean("settings.sound.enable_sound", true);
    }

    public boolean isParticlesEnabled() {
        return getBoolean("settings.particle_effect.enable_particles", true);
    }

    public void playEffect(Player player, String soundPath, String particlePath) {
        playSound(player, soundPath);
        playParticle(player, particlePath);
    }

    public void playSound(Player player, String path) {
        if (!isSoundEnabled() || player == null || !player.isOnline()) return;

        String soundName = getString(path, null);
        if (soundName == null) return;

        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            float volume = (float) getDouble("settings.sound.volume", 1.0);
            float pitch = (float) getDouble("settings.sound.pitch", 1.0);
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            logger.warn("Неверное имя звука '{}': {}", soundName, e.getMessage());
        }
    }

    public void playParticle(Player player, String path) {
        if (!isParticlesEnabled() || player == null || !player.isOnline()) return;

        String particleName = getString(path, null);
        if (particleName == null) return;

        try {
            Particle particle = Particle.valueOf(particleName.toUpperCase());
            int amount = getInt("settings.particle_effect.amount", 5);
            player.getWorld().spawnParticle(particle, player.getLocation(), amount);
        } catch (IllegalArgumentException e) {
            logger.warn("Неверное имя частицы '{}': {}", particleName, e.getMessage());
        }
    }

    // Рандомные эффекты
    public void playRandomSound(Player player, String... soundPaths) {
        if (soundPaths.length == 0) return;
        String randomPath = soundPaths[random.nextInt(soundPaths.length)];
        playSound(player, randomPath);
    }

    public void playRandomParticle(Player player, String... particlePaths) {
        if (particlePaths.length == 0) return;
        String randomPath = particlePaths[random.nextInt(particlePaths.length)];
        playParticle(player, randomPath);
    }

    // LINKS
    public static class LinkEntry {
        private final String url;
        private final String text;

        public LinkEntry(String url, String text) {
            this.url = url;
            this.text = text;
        }

        public String getUrl() { return url; }
        public String getText() { return text; }

        @Override
        public String toString() {
            return "LinkEntry{url='" + url + "', text='" + text + "'}";
        }
    }

    private void loadLinks() {
        ConfigurationSection section = getConfig().getConfigurationSection("links");
        if (section == null) {
            logger.debug("Секция 'links' не найдена в конфиге");
            return;
        }

        Map<String, LinkEntry> result = new HashMap<>();
        for (String key : section.getKeys(false)) {
            if (key.endsWith("_url")) {
                String baseKey = key.replace("_url", "");
                String url = section.getString(key, "");
                String text = section.getString(baseKey + "_text", baseKey);
                if (!url.isEmpty()) {
                    result.put(baseKey, new LinkEntry(url, text));
                }
            }
        }
        this.links = result;
        logger.debug("Загружено {} ссылок", result.size());
    }

    public String replaceLinks(String text) {
        if (text == null) return "";
        for (Map.Entry<String, LinkEntry> entry : links.entrySet()) {
            String key = entry.getKey();
            text = text
                    .replace("%" + key + "_url%", entry.getValue().getUrl())
                    .replace("%" + key + "_text%", entry.getValue().getText());
        }
        return text;
    }

    public String replaceLinksWithMiniMessage(String text) {
        if (text == null) return "";
        for (Map.Entry<String, LinkEntry> entry : links.entrySet()) {
            String key = entry.getKey();
            String url = entry.getValue().getUrl();
            String linkText = entry.getValue().getText();

            String miniMessageTag = "<click:open_url:'" + url + "'><hover:show_text:'Нажмите для перехода'>" + linkText + "</hover></click>";

            text = text.replace("%" + key + "_link%", miniMessageTag)
                    .replace("%" + key + "_url%", url)
                    .replace("%" + key + "_text%", linkText);
        }
        return text;
    }

    public List<String> replaceLinksInList(List<String> lines) {
        if (lines == null) return new ArrayList<>();
        return lines.stream()
                .map(this::replaceLinks)
                .collect(Collectors.toList());
    }

    public Map<String, LinkEntry> getLinks() {
        return Map.copyOf(links);
    }

    public LinkEntry getLink(String key) {
        return links.get(key);
    }

    public boolean hasLink(String key) {
        return links.containsKey(key);
    }
}