package ru.net.terracraft.welcome.core.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import ru.net.terracraft.welcome.core.feature.DeathService;
import ru.net.terracraft.welcome.core.feature.GuiService;
import ru.net.terracraft.welcome.core.feature.JoinService;

/**
 * Единый слушатель всех событий плагина.
 * Только маршрутизация → соответствующие сервисы.
 */
public class PlayerListener implements Listener {

    private final JoinService joinService;
    private final DeathService deathService;
    private final GuiService guiService;

    public PlayerListener(JoinService joinService, DeathService deathService, GuiService guiService) {
        this.joinService = joinService;
        this.deathService = deathService;
        this.guiService = guiService;
    }

    // ═══════════════ JOIN ═══════════════

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {
        // Убираем стандартное сообщение
        event.joinMessage(null);

        // Логика в сервисе
        joinService.handleJoin(event.getPlayer());
    }

    // ═══════════════ QUIT ═══════════════

    @EventHandler(priority = EventPriority.NORMAL)
    public void onQuit(PlayerQuitEvent event) {
        // Убираем стандартное сообщение
        event.quitMessage(null);

        // Логика в сервисе
        joinService.handleQuit(event.getPlayer());
    }

    // ═══════════════ DEATH ═══════════════

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        deathService.handleDeath(event);
    }

    // ═══════════════ RESPAWN ═══════════════

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRespawn(PlayerRespawnEvent event) {
        deathService.handleRespawn(event.getPlayer());
    }

    // ═══════════════ GUI ═══════════════

    @EventHandler(priority = EventPriority.HIGH)
    public void onGuiClick(InventoryClickEvent event) {
        guiService.handleClick(event);
    }
}