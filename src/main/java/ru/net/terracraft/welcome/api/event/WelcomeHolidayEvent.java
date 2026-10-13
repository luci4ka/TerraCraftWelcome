package ru.net.terracraft.welcome.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import ru.net.terracraft.welcome.core.feature.HolidayType;

/**
 * Вызывается при применении праздничного эффекта к игроку.
 */
public class WelcomeHolidayEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final HolidayType holiday;

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
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}