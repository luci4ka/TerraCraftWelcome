package ru.terracraft.welcome.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Вызывается перед обработкой входа игрока плагином Welcome.
 * Можно отменить — тогда JoinService не будет обрабатывать вход.
 */
public class WelcomeJoinEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final boolean firstJoin;
    private boolean cancelled;

    public WelcomeJoinEvent(Player player, boolean firstJoin) {
        this.player = player;
        this.firstJoin = firstJoin;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isFirstJoin() {
        return firstJoin;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}