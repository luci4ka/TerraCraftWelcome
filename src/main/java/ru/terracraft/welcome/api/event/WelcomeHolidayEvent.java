package ru.terracraft.welcome.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.welcome.core.feature.HolidayType;

/**
 * Вызывается перед применением праздничного эффекта к игроку.
 * Можно отменить — тогда эффект и сообщение не будут показаны этому игроку.
 */
public class WelcomeHolidayEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final HolidayType holiday;
    private boolean cancelled;

    public WelcomeHolidayEvent(Player player, HolidayType holiday) {
        this.player = player;
        this.holiday = holiday;
    }

    public Player getPlayer() {
        return player;
    }

    public HolidayType getHoliday() {
        return holiday;
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