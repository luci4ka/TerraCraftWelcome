package com.welcome.config;

import com.welcome.Main;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class ConfigHandler {

    private final Main plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Random random = new Random();
    private boolean papiEnabled;

    private Map<String, LinkEntry> links = new HashMap<>();

    // -------------------- Звуки и частицы --------------------
    public boolean isSoundEnabled() {
        return getBoolean("sound.enable_sound", true);
    }

    public boolean isParticlesEnabled() {
        return getBoolean("particle_effect.enable_particles", true);
    }

    public void playEffect(Player player, String soundPath, String particlePath) {
        playSound(player, soundPath);
        playParticle(player, particlePath);
    }

    public void playSound(Player player, String path) {
        if (!isSoundEnabled()) return;

        String soundName = getString(path, null);
        if (soundName == null) return;

        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            float volume = (float) getDouble("sound.volume", 1.0);
            float pitch = (float) getDouble("sound.pitch", 1.0);
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException ex) {
            getPlugin().getLogger().warning("Неверное имя звука: " + soundName);
        }
    }

    public void playParticle(Player player, String path) {
        if (!isParticlesEnabled()) return;

        String particleName = getString(path, null);
        if (particleName == null) return;

        try {
            Particle particle = Particle.valueOf(particleName.toUpperCase());
            int amount = getInt("particle_effect.amount", 5);
            player.getWorld().spawnParticle(particle, player.getLocation(), amount);
        } catch (IllegalArgumentException ex) {
            getPlugin().getLogger().warning("Неверное имя частицы: " + particleName);
        }
    }

    // -------------------- Конфигурация --------------------
    public ConfigurationSection getConfigSection(String path) {
        return getPlugin().getConfig().getConfigurationSection(path);
    }

    public ConfigHandler(Main plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        papiEnabled = plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
        loadLinks();
    }

    public void checkConfigVersion(double expectedVersion) {
        double actual = plugin.getConfig().getDouble("config-version", -1);
        if (actual != expectedVersion) {
            plugin.getLogger().warning("⚠ Версия конфига (" + actual +
                    ") не совпадает с ожидаемой (" + expectedVersion + ")");
        }
    }

    public boolean getBoolean(String path, boolean def) {
        return plugin.getConfig().getBoolean(path, def);
    }

    public int getInt(String path, int def) {
        return plugin.getConfig().getInt(path, def);
    }

    public double getDouble(String path, double def) {
        return plugin.getConfig().getDouble(path, def);
    }

    public String getString(String path, String def) {
        String value = plugin.getConfig().getString(path);
        return value != null ? value : def;
    }

    public List<String> getMessageList(String path) {
        List<String> raw = plugin.getConfig().getStringList(path);
        return replaceLinks(raw);
    }

    public String getMessage(String path) {
        String raw = plugin.getConfig().getString(path, "");
        return replaceLinks(raw);
    }

    // -------------------- PlaceholderAPI --------------------
    public void setPapiEnabled(boolean enabled) {
        this.papiEnabled = enabled;
    }

    public boolean isPapiEnabled() {
        return papiEnabled;
    }

    public String parseMessage(Player player, String text) {
        if (text == null) return "";
        if (papiEnabled && player != null) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }

    // -------------------- LINKS --------------------
    public static class LinkEntry {
        private final String url;
        private final String text;

        public LinkEntry(String url, String text) {
            this.url = url;
            this.text = text;
        }

        public String getUrl() { return url; }
        public String getText() { return text; }
    }

    private void loadLinks() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("links");
        if (section == null) return;

        Map<String, LinkEntry> result = new HashMap<>();
        for (String key : section.getKeys(false)) {
            if (key.endsWith("_url")) {
                String baseKey = key.replace("_url", "");
                String url = section.getString(key, "");
                String text = section.getString(baseKey + "_text", baseKey);
                result.put(baseKey, new LinkEntry(url, text));
            }
        }
        this.links = result;
    }

    public String replaceLinksMsg(String message) {
        if (message == null) return "";
        for (Map.Entry<String, LinkEntry> entry : links.entrySet()) {
            String key = entry.getKey();
            String url = entry.getValue().getUrl();
            String text = entry.getValue().getText();

            // MiniMessage тег для кликабельной ссылки
            String miniTag = "<click:open_url='" + url + "'>" + text + "</click>";

            // Заменяем в сообщении
            message = message.replace("%" + key + "_link%", miniTag)
                    .replace("%" + key + "_url%", url)
                    .replace("%" + key + "_text%", text);
        }
        return message;
    }


    public Map<String, LinkEntry> getLinks() {
        return links;
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

    public List<String> replaceLinks(List<String> lines) {
        return lines.stream()
                .map(this::replaceLinks)
                .collect(Collectors.toList());
    }

    public Main getPlugin() {
        return plugin;
    }
}
