package com.luci.welcome.listener;

import com.luci.welcome.Main;
import com.luci.welcome.config.ConfigHandler;
import com.luci.welcome.config.PlayerJoinConfig;
import com.luci.welcome.utils.LoggerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.slf4j.LoggerFactory;

public class PlayerJoinListener implements Listener {

    private final Main plugin;
    private final PlayerJoinConfig joinConfig;
    private final LoggerUtil logger;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public PlayerJoinListener(Main plugin) {
        this.plugin = plugin;
        this.joinConfig = new PlayerJoinConfig(plugin.getConfigHandler());
        this.logger = new LoggerUtil(LoggerFactory.getLogger("Welcome-PlayerJoin"));
    }

    /**
     * Обработка входа игрока
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        boolean firstJoin = !player.hasPlayedBefore();

        // Убираем стандартное сообщение
        event.joinMessage(null);

        // Отправляем MOTD
        joinConfig.sendMOTD(player);

        // Отправляем титул
        joinConfig.sendJoinTitle(player, firstJoin);

        // Отправляем сообщение о входе
        if (firstJoin) {
            // Первый вход
            joinConfig.broadcastJoinMessage(player, true);

            // Фейерверк для новичка
            joinConfig.spawnFirstJoinFirework(player);

            logger.info("Игрок {} впервые зашёл на сервер!", player.getName());
        } else {
            // Обычный вход
            if (joinConfig.isJoinEnabled()) {
                joinConfig.broadcastPermissionMessage(player, "join");
            }
        }

        if (plugin.getHookManager().isPapiEnabled()) {
            plugin.getHookManager().getPapiHook().getExpansion().recordJoin(player.getUniqueId());
        }

        // Эффекты входа
        joinConfig.playJoinEffects(player, firstJoin);
        joinConfig.playWelcomeSound(player);

        // Отправляем приветственное сообщение в action bar
        sendActionBar(player, firstJoin);
    }

    /**
     * Обработка выхода игрока
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Убираем стандартное сообщение
        event.quitMessage(null);

        // Отправляем сообщение о выходе
        if (joinConfig.isQuitEnabled()) {
            joinConfig.broadcastPermissionMessage(player, "quit");
        }

        // Эффекты выхода
        joinConfig.playLeaveEffects(player);
    }

    /**
     * Отправляет сообщение в action bar
     */
    private void sendActionBar(Player player, boolean firstJoin) {
        try {
            String message;
            if (firstJoin) {
                message = plugin.getConfig().getString(
                        "messages.local.actionbar.first_join",
                        "<gold>✨ Добро пожаловать на сервер!</gold>"
                );
            } else {
                message = plugin.getConfig().getString(
                        "messages.local.actionbar.join",
                        "<green>С возвращением, %player_name%!</green>"
                );
            }

            message = message.replace("%player_name%", player.getName());
            message = plugin.getConfigHandler().parsePlaceholders(player, message);

            Component component = miniMessage.deserialize(message);
            player.sendActionBar(component);
        } catch (Exception e) {
            logger.debug("Не удалось отправить action bar игроку " + player.getName());
        }
    }

    /**
     * Получить конфиг входа
     */
    public PlayerJoinConfig getJoinConfig() {
        return joinConfig;
    }
}