package com.luci.welcome.config;

import com.luci.welcome.utils.LoggerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

public class HolidayConfig {

    private final ConfigHandler config;
    private final LoggerUtil logger;
    private final MiniMessage mini = MiniMessage.miniMessage();
    private final Random random = new Random();

    public HolidayConfig(ConfigHandler config) {
        this.config = config;
        this.logger = new LoggerUtil(config.getPlugin().getSLF4JLogger());
    }

    public boolean isEnabled() {
        return config.getBoolean("holidays.enabled", false);
    }

    public String getCurrentHoliday() {
        return config.getString("holidays.current", "default");
    }

    public int getIntervalTicks() {
        return config.getInt("holidays.interval_ticks", 1200); // по конфигу 1200
    }

    public List<String> getMessages(String holiday) {
        return config.getMessageList("holidays." + holiday + ".messages");
    }

    public Particle getParticle(String holiday) {
        String particleName = config.getString("holidays." + holiday + ".particle", "FIREWORKS_SPARK");
        try {
            return Particle.valueOf(particleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Неверное имя частицы для праздника {}: {}", holiday, particleName);
            return Particle.FIREWORK;
        }
    }

    public Sound getSound(String holiday) {
        String soundName = config.getString("holidays." + holiday + ".sound", "ENTITY_PLAYER_LEVELUP");
        try {
            return Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Неверное имя звука для праздника {}: {}", holiday, soundName);
            return Sound.ENTITY_PLAYER_LEVELUP;
        }
    }

    public String formatMessage(String message, Player player) {
        message = message.replace("%player_name%", player.getName());
        return config.replaceLinks(message);
    }

    public Component deserialize(String message) {
        return mini.deserialize(message);
    }

    public LoggerUtil getLogger() {
        return logger;
    }
}
