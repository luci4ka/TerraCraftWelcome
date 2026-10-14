package ru.terracraft.welcome.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Вызывается перед обработкой смерти игрока плагином Welcome.
 * Можно отменить — тогда DeathService не будет рассылать сообщения/эффекты.
 */
public class WelcomeDeathEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player victim;
    private final Player killer;
    private final String cause;
    private boolean cancelled;

    public WelcomeDeathEvent(Player victim, @Nullable Player killer, String cause) {
        this.victim = victim;
        this.killer = killer;
        this.cause = cause;
    }

    public Player getVictim() {
        return victim;
    }

    @Nullable
    public Player getKiller() {
        return killer;
    }

    public String getCause() {
        return cause;
    }

    public boolean isPvp() {
        return killer != null;
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