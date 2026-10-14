package ru.terracraft.welcome.core.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import ru.terracraft.welcome.WelcomePlugin;
import ru.terracraft.welcome.core.PluginContext;
import ru.terracraft.welcome.core.feature.DeathService;
import ru.terracraft.welcome.core.feature.GuiService;
import ru.terracraft.welcome.core.feature.JoinService;

/**
 * Единый слушатель всех событий плагина.
 * Сервисы берутся из ServiceRegistry динамически,
 * чтобы переживать reload контекста (см. PluginContext.reload()).
 */
public class PlayerListener implements Listener {

    private final WelcomePlugin plugin;

    public PlayerListener(WelcomePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Возвращает ServiceRegistry или null, если контекст ещё не создан
     * или уже остановлен.
     */
    private PluginContext.ServiceRegistry registry() {
        PluginContext ctx = plugin.context();
        return ctx != null ? ctx.registry() : null;
    }

    // ═══════════════ JOIN ═══════════════

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {
        event.joinMessage(null);

        PluginContext.ServiceRegistry reg = registry();
        if (reg == null || !reg.has(JoinService.class)) return;
        reg.get(JoinService.class).handleJoin(event.getPlayer());
    }

    // ═══════════════ QUIT ═══════════════

    @EventHandler(priority = EventPriority.NORMAL)
    public void onQuit(PlayerQuitEvent event) {
        event.quitMessage(null);

        PluginContext.ServiceRegistry reg = registry();
        if (reg == null || !reg.has(JoinService.class)) return;
        reg.get(JoinService.class).handleQuit(event.getPlayer());
    }

    // ═══════════════ DEATH ═══════════════

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        PluginContext.ServiceRegistry reg = registry();
        if (reg == null || !reg.has(DeathService.class)) return;
        reg.get(DeathService.class).handleDeath(event);
    }

    // ═══════════════ RESPAWN ═══════════════

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRespawn(PlayerRespawnEvent event) {
        PluginContext.ServiceRegistry reg = registry();
        if (reg == null || !reg.has(DeathService.class)) return;
        reg.get(DeathService.class).handleRespawn(event.getPlayer());
    }

    // ═══════════════ GUI ═══════════════

    @EventHandler(priority = EventPriority.HIGH)
    public void onGuiClick(InventoryClickEvent event) {
        PluginContext.ServiceRegistry reg = registry();
        if (reg == null || !reg.has(GuiService.class)) return;
        reg.get(GuiService.class).handleClick(event);
    }
}