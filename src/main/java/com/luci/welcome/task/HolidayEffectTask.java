package com.luci.welcome.task;

import com.luci.welcome.Main;
import com.luci.welcome.config.HolidayConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

public class HolidayEffectTask extends BukkitRunnable {

    private final Main plugin;
    private final HolidayConfig holidayConfig;
    private int intervalTicks;
    private final Random random = new Random();

    public HolidayEffectTask(Main plugin, HolidayConfig holidayConfig) {
        this.plugin = plugin;
        this.holidayConfig = holidayConfig;
        this.intervalTicks = holidayConfig.getIntervalTicks();
    }

    @Override
    public void run() {
        if (!holidayConfig.isEnabled()) return;

        String holiday = holidayConfig.getCurrentHoliday();
        List<String> messages = holidayConfig.getMessages(holiday);

        holidayConfig.getParticle(holiday); // Можно сразу в спавне
        holidayConfig.getSound(holiday);

        holidayConfig.getParticle(holiday);
        holidayConfig.getSound(holiday);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getWorld().spawnParticle(holidayConfig.getParticle(holiday), player.getLocation(), 10);
            player.playSound(player.getLocation(), holidayConfig.getSound(holiday), 1f, 1f);

            if (!messages.isEmpty()) {
                String msg = messages.get(random.nextInt(messages.size()));
                msg = holidayConfig.formatMessage(msg, player);

                try {
                    player.sendMessage(holidayConfig.deserialize(msg));
                } catch (Exception ex) {
                    holidayConfig.getLogger().warn("Ошибка при десериализации MiniMessage для праздничного сообщения: " + ex.getMessage());
                }
            }
        }
    }

    public void start() {
        this.runTaskTimer(plugin, 0L, intervalTicks);
    }

    public void setIntervalTicks(int intervalTicks) {
        this.intervalTicks = intervalTicks;
        this.cancel();
        start();
    }
}
