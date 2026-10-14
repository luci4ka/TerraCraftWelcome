package ru.terracraft.welcome.core.i18n;

/**
 * Type-safe ключи языковых сообщений.
 * Путь в lang/*.lang соответствует полю path.
 *
 * Списки: ключ соответствует базовому пути, значения — path.1, path.2, ...
 * Пример: MOTD_LINES("motd.lines") → motd.lines.1, motd.lines.2, ...
 */
public enum MessageKey {

    // ─── MOTD ───
    MOTD_LINES("motd.lines"),

    // ─── Join ───
    JOIN_DEFAULT("join.default"),
    JOIN_FIRST("join.first_join"),

    // ─── Quit ───
    QUIT_DEFAULT("quit.default"),

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

    // ─── Help ───
    HELP_HEADER("help.header"),
    HELP_COMMANDS("help.commands"),
    HELP_ADMIN("help.admin"),
    HELP_FOOTER("help.footer"),

    // ─── GUI ───
    GUI_TITLE("gui.title"),
    GUI_TOGGLE_LORE("gui.toggle.lore"),

    GUI_SOUND_NAME("gui.sound.name"),
    GUI_PARTICLES_NAME("gui.particles.name"),
    GUI_HOLIDAYS_NAME("gui.holidays.name"),

    GUI_LANGUAGE_NAME("gui.language.name"),
    GUI_LANGUAGE_LORE_CLICK("gui.language.lore.click"),
    GUI_LANGUAGE_LORE_AVAILABLE("gui.language.lore.available"),

    GUI_INFO_NAME("gui.info.name"),
    GUI_INFO_VERSION("gui.info.version"),
    GUI_INFO_AUTHOR("gui.info.author"),
    GUI_INFO_PAPI("gui.info.papi"),
    GUI_INFO_VAULT("gui.info.vault"),

    GUI_TOGGLE_MESSAGE("gui.toggle.message"),
    GUI_TOGGLE_STATE_ON("gui.toggle.state.on"),
    GUI_TOGGLE_STATE_OFF("gui.toggle.state.off"),

    GUI_INFO_MESSAGE("gui.info.message"),

    // ─── Info ───
    INFO_HEADER("info.header"),
    INFO_BODY("info.body"),
    INFO_FOOTER("info.footer"),

    // ─── Commands ───
    COMMAND_RELOAD_SUCCESS("commands.reload.success"),
    COMMAND_RELOAD_ERROR("commands.reload.error"),
    COMMAND_LANGUAGE_SUCCESS("commands.language.success"),
    COMMAND_LANGUAGE_NOT_FOUND("commands.language.not_found"),
    COMMAND_GUI_PLAYER_ONLY("commands.gui.player_only"),

    // ─── Errors ───
    ERROR_API_UNAVAILABLE("errors.api_unavailable"),
    ERROR_GENERIC("errors.generic"),
    ERROR_GUI_OPEN("errors.gui_open");

    private final String path;

    MessageKey(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }
}