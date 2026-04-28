package com.luci.welcome.task;

import com.luci.welcome.Main;
import com.luci.welcome.config.AutoMessageConfig;
import com.luci.welcome.utils.LoggerUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AutoMessageTask {

    private final Main plugin;
    private final AutoMessageConfig autoMessageConfig;
    private final LoggerUtil logger;
    private BukkitTask task;
    private int intervalTicks;
    private final AtomicInteger messageCounter = new AtomicInteger(0);

    public AutoMessageTask(Main plugin, AutoMessageConfig autoMessageConfig) {
        this.plugin = plugin;
        this.autoMessageConfig = autoMessageConfig;
        this.intervalTicks = autoMessageConfig.getIntervalTicks();
        this.logger = new LoggerUtil(LoggerFactory.getLogger("Welcome-AutoMessage"));
    }

    /**
     * Запускает задачу авто-сообщений
     */
    public void start() {
        if (task != null && !task.isCancelled()) {
            logger.warn("Задача авто-сообщений уже запущена");
            return;
        }

        if (!autoMessageConfig.isEnabled()) {
            logger.info("Авто-сообщения отключены в конфиге");
            return;
        }

        if (!autoMessageConfig.hasMessages()) {
            logger.warn("Нет сообщений для авто-рассылки");
            return;
        }

        task = Bukkit.getScheduler().runTaskTimer(plugin, this::broadcastMessage, 20L, intervalTicks);
    }

    /**
     * Останавливает задачу
     */
    public void cancel() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * Перезапускает задачу с новым интервалом
     */
    public void restart(int newIntervalTicks) {
        cancel();
        this.intervalTicks = Math.max(100, newIntervalTicks);
        start();
    }

    /**
     * Рассылает сообщение всем игрокам
     */
    private void broadcastMessage() {
        if (!autoMessageConfig.isEnabled()) {
            cancel();
            return;
        }

        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        if (onlinePlayers.isEmpty()) {
            return;
        }

        int sentCount = 0;
        for (Player player : onlinePlayers) {
            if (player == null || !player.isOnline()) continue;

            if (!shouldSendToPlayer(player)) continue;

            try {
                String message = autoMessageConfig.getRandomMessage(player);
                if (message.isEmpty()) continue;

                Component component = autoMessageConfig.deserialize(message);
                player.sendMessage(component);
                sentCount++;
            } catch (Exception e) {
                logger.error("Ошибка отправки авто-сообщения игроку " + player.getName(), e);
            }
        }

        if (sentCount > 0) {
            messageCounter.incrementAndGet();
        }
    }

    /**
     * Отправляет сообщение конкретному игроку
     */
    public void sendToPlayer(Player player) {
        if (player == null || !player.isOnline()) return;
        if (!autoMessageConfig.isEnabled()) return;

        try {
            String message = autoMessageConfig.getRandomMessage(player);
            if (message.isEmpty()) return;

            Component component = autoMessageConfig.deserialize(message);
            player.sendMessage(component);
        } catch (Exception e) {
            logger.error("Ошибка отправки авто-сообщения игроку " + player.getName(), e);
        }
    }

    /**
     * Проверяет, нужно ли отправлять сообщения игроку
     */
    private boolean shouldSendToPlayer(Player player) {
        String permission = plugin.getConfig().getString("auto_messages.permission", "");
        if (!permission.isEmpty() && !player.hasPermission(permission)) {
            return false;
        }

        List<String> worlds = plugin.getConfig().getStringList("auto_messages.worlds");
        if (!worlds.isEmpty() && !worlds.contains(player.getWorld().getName())) {
            return false;
        }

        return true;
    }

    /**
     * Принудительно отправляет следующее сообщение всем
     */
    public void forceBroadcast() {
        broadcastMessage();
    }

    // Геттеры
    public boolean isRunning() {
        return task != null && !task.isCancelled();
    }

    public int getIntervalTicks() {
        return intervalTicks;
    }

    public int getMessagesSent() {
        return messageCounter.get();
    }

    public AutoMessageConfig getConfig() {
        return autoMessageConfig;
    }

    public LoggerUtil getLogger() {
        return logger;
    }
}