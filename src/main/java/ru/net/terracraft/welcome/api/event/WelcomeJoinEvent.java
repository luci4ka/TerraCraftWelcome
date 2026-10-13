package ru.net.terracraft.welcome.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Вызывается после обработки входа игрока плагином Welcome.
 * Можно отменить — тогда JoinService не будет дальше обрабатывать.
 */
public class WelcomeJoinEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final boolean firstJoin;

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
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}