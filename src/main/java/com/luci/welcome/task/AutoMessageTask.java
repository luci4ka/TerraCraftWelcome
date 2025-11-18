package com.luci.welcome.task;

import com.luci.welcome.Main;
import com.luci.welcome.config.AutoMessageConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AutoMessageTask extends BukkitRunnable {

    private final Main plugin;
    private final AutoMessageConfig autoMessageConfig;
    private int intervalTicks;

    public AutoMessageTask(Main plugin, AutoMessageConfig autoMessageConfig) {
        this.plugin = plugin;
        this.autoMessageConfig = autoMessageConfig;
        this.intervalTicks = autoMessageConfig.getIntervalTicks();
    }

    @Override
    public void run() {
        if (!autoMessageConfig.isEnabled()) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            String msg = autoMessageConfig.getRandomMessage(player);
            if (msg.isEmpty()) continue;

            try {
                player.sendMessage(autoMessageConfig.deserialize(msg));
            } catch (Exception e) {
                autoMessageConfig.getLogger().warn("Ошибка при отправке авто-сообщения игроку {}: {}", player.getName(), e.getMessage());
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
