package com.welcome.task;

import com.welcome.Main;
import com.welcome.config.ConfigHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

public class HolidayEffectTask extends BukkitRunnable {

    private final Main plugin;
    private final ConfigHandler config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Random random = new Random();
    private int intervalTicks;

    public HolidayEffectTask(Main plugin, ConfigHandler config) {
        this.plugin = plugin;
        this.config = config;

        // Интервал из конфига (по умолчанию 1 минута)
        this.intervalTicks = config.getInt("holidays.interval_ticks", 20 * 60);
    }

    @Override
    public void run() {
        // Проверяем, включен ли праздничный режим
        if (!config.getBoolean("holidays.enabled", false)) return;

        // Получаем текущий праздник из конфига
        String holidayName = config.getString("holidays.current", "default");
        List<String> messages = config.getMessageList("holidays." + holidayName + ".messages");
        String particleName = config.getString("holidays." + holidayName + ".particle", "FIREWORKS_SPARK");
        String soundName = config.getString("holidays." + holidayName + ".sound", "ENTITY_PLAYER_LEVELUP");

        Particle particle;
        try {
            particle = Particle.valueOf(particleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("[HolidayEffectTask] Неверное имя частицы: " + particleName);
            particle = Particle.FIREWORK;
        }

        Sound sound;
        try {
            sound = Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("[HolidayEffectTask] Неверное имя звука: " + soundName);
            sound = Sound.ENTITY_PLAYER_LEVELUP;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            // Эффекты
            player.getWorld().spawnParticle(particle, player.getLocation(), 10);
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);

            // Сообщения
            if (!messages.isEmpty()) {
                String message = messages.get(random.nextInt(messages.size()));
                message = message.replace("%player_name%", player.getName());
                message = config.replaceLinks(message);
                Component component = miniMessage.deserialize(message);
                player.sendMessage(component);
            }
        }
    }

    /** Запуск задачи с интервалом из конфига */
    public void start() {
        this.runTaskTimer(plugin, 0L, intervalTicks);
    }

    /** Позволяет обновить интервал на лету */
    public void setIntervalTicks(int intervalTicks) {
        this.intervalTicks = intervalTicks;
        this.cancel();
        start();
    }
}
