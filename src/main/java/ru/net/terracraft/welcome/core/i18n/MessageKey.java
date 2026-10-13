package ru.net.terracraft.welcome.core.i18n;

/**
 * Type-safe ключи языковых сообщений.
 * Путь в lang/*.yml соответствует полю path.
 */
public enum MessageKey {

    // ─── MOTD ───
    MOTD_LINES("motd.lines"),

    // ─── Join ───
    JOIN_DEFAULT("join.default"),
    JOIN_FIRST("join.first_join"),
    JOIN_ADMIN("join.admin"),
    JOIN_VIP("join.vip"),

    // ─── Quit ───
    QUIT_DEFAULT("quit.default"),
    QUIT_ADMIN("quit.admin"),
    QUIT_VIP("quit.vip"),

    // ─── Titles ───
    TITLE_JOIN_TITLE("titles.join.title"),
    TITLE_JOIN_SUBTITLE("titles.join.subtitle"),
    TITLE_FIRST_JOIN_TITLE("titles.first_join.title"),
    TITLE_FIRST_JOIN_SUBTITLE("titles.first_join.subtitle"),
    TITLE_DEATH_TITLE("titles.death.title"),
    TITLE_DEATH_SUBTITLE("titles.death.subtitle"),

    // ─── Action bar ───
    ACTIONBAR_JOIN("actionbar.join"),
    ACTIONBAR_FIRST_JOIN("actionbar.first_join"),

    // ─── Death ───
    DEATH_PVP("death.pvp"),
    DEATH_MOB_DEFAULT("death.mob.default"),
    DEATH_ENVIRONMENT("death.player_kill"),
    DEATH_LAVA("death.lava"),
    DEATH_FALL("death.fall"),
    DEATH_EXPLOSION("death.explosion"),
    DEATH_DROWNING("death.drowning"),
    DEATH_FIRE("death.fire"),
    DEATH_VOID("death.void"),

    // ─── Respawn ───
    RESPAWN("respawn"),

    // ─── Holidays ───
    HOLIDAY_NEW_YEAR("holidays.new_year.messages"),
    HOLIDAY_CHRISTMAS("holidays.christmas.messages"),
    HOLIDAY_VALENTINE("holidays.valentine.messages"),
    HOLIDAY_HALLOWEEN("holidays.halloween.messages"),
    HOLIDAY_EASTER("holidays.easter.messages"),
    HOLIDAY_WOMENS_DAY("holidays.womens_day.messages"),
    HOLIDAY_DEFENDER_DAY("holidays.defender_day.messages"),
    HOLIDAY_APRIL_FOOLS("holidays.april_fools.messages"),
    HOLIDAY_VICTORY_DAY("holidays.victory_day.messages"),
    HOLIDAY_DEFAULT("holidays.default.messages"),

    // ─── Links ───
    LINK_VK("links.vk"),
    LINK_SITE("links.site"),
    LINK_DISCORD("links.discord"),
    LINK_TELEGRAM("links.telegram"),
    LINK_YOUTUBE("links.youtube"),

    // ─── Commands ───
    COMMAND_RELOAD_SUCCESS("commands.reload.success"),
    COMMAND_MIGRATE_SUCCESS("commands.migrate.success"),
    COMMAND_MIGRATE_ALREADY_FRESH("commands.migrate.already_fresh"),
    COMMAND_LANGUAGE_SUCCESS("commands.language.success"),
    COMMAND_LANGUAGE_NOT_FOUND("commands.language.not_found"),
    COMMAND_GUI_PLAYER_ONLY("commands.gui.player_only");

    private final String path;

    MessageKey(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }
}