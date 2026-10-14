package ru.terracraft.welcome.core.feature;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import ru.terracraft.welcome.WelcomePlugin;
import ru.terracraft.welcome.api.event.WelcomeHolidayEvent;
import ru.terracraft.welcome.config.MainConfig;
import ru.terracraft.welcome.core.i18n.LanguageManager;
import ru.terracraft.welcome.core.i18n.MessageKey;
import ru.terracraft.welcome.core.integration.IntegrationManager;

import java.time.LocalDate;
import java.time.Month;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Сервис праздников: календарь, эффекты, сообщения.
 * Всё в одном классе.
 * Сообщения — на локали получателя.
 */
public class HolidayService {

    private final WelcomePlugin plugin;
    private final MainConfig config;
    private final LanguageManager lang;
    private final IntegrationManager integrations;
    private final Random random = new Random();

    // ═══════════════ CALENDAR ═══════════════

    private record DateRange(Month startMonth, int startDay, Month endMonth, int endDay) {
        boolean isInRange(LocalDate date) {
            LocalDate start = LocalDate.of(date.getYear(), startMonth, startDay);
            LocalDate end = LocalDate.of(date.getYear(), endMonth, endDay);

            // Переход через год (дек → янв)
            if (startMonth.getValue() > endMonth.getValue()) {
                if (date.getMonthValue() < startMonth.getValue()) {
                    start = start.minusYears(1);
                } else {
                    end = end.plusYears(1);
                }
            }

            return !date.isBefore(start) && !date.isAfter(end);
        }
    }

    private static final Map<HolidayType, DateRange> CALENDAR = new LinkedHashMap<>();

    static {
        CALENDAR.put(HolidayType.NEW_YEAR,     new DateRange(Month.DECEMBER, 28, Month.JANUARY, 7));
        CALENDAR.put(HolidayType.CHRISTMAS,    new DateRange(Month.DECEMBER, 23, Month.DECEMBER, 27));
        CALENDAR.put(HolidayType.VALENTINE,    new DateRange(Month.FEBRUARY, 13, Month.FEBRUARY, 15));
        CALENDAR.put(HolidayType.WOMENS_DAY,   new DateRange(Month.MARCH, 6, Month.MARCH, 9));
        CALENDAR.put(HolidayType.DEFENDER_DAY, new DateRange(Month.FEBRUARY, 21, Month.FEBRUARY, 24));
        CALENDAR.put(HolidayType.APRIL_FOOLS,  new DateRange(Month.APRIL, 1, Month.APRIL, 1));
        CALENDAR.put(HolidayType.EASTER,       new DateRange(Month.APRIL, 10, Month.APRIL, 20));
        CALENDAR.put(HolidayType.VICTORY_DAY,  new DateRange(Month.MAY, 7, Month.MAY, 10));
        CALENDAR.put(HolidayType.HALLOWEEN,    new DateRange(Month.OCTOBER, 28, Month.OCTOBER, 31));
    }

    // ═══════════════ CONSTRUCTOR ═══════════════

    public HolidayService(WelcomePlugin plugin, MainConfig config,
                          LanguageManager lang, IntegrationManager integrations) {
        this.plugin = plugin;
        this.config = config;
        this.lang = lang;
        this.integrations = integrations;
    }

    // ═══════════════ PUBLIC API ═══════════════

    public boolean isEnabled() {
        return config.holidays.enabled;
    }

    public int getIntervalTicks() {
        return Math.max(100, config.holidays.intervalTicks);
    }

    public HolidayType getCurrentHoliday() {
        String configured = config.holidays.current;
        if (configured == null || configured.equalsIgnoreCase("auto")) {
            return detectToday();
        }
        return HolidayType.fromKey(configured);
    }

    public void applyTo(Player player) {
        if (!isEnabled()) return;
        if (player == null || !player.isOnline()) return;

        HolidayType holiday = getCurrentHoliday();
        if (holiday.isDefault()) return;

        // ═══════════════ EVENT ═══════════════
        WelcomeHolidayEvent event = new WelcomeHolidayEvent(player, holiday);
        try {
            plugin.getServer().getPluginManager().callEvent(event);
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка вызова WelcomeHolidayEvent: " + e.getMessage());
        }

        if (event.isCancelled()) {
            return;
        }
        // ═════════════════════════════════════

        sendMessage(player, holiday);
        playEffect(player, holiday);
    }

    public void applyToAll() {
        if (!isEnabled()) return;

        HolidayType holiday = getCurrentHoliday();
        if (holiday.isDefault()) return;

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            try {
                applyTo(player);
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка праздника для "
                        + player.getName() + ": " + e.getMessage());
            }
        }
    }

    public void forceApply() {
        applyToAll();
    }

    // ═══════════════ CALENDAR API ═══════════════

    public HolidayType detectToday() {
        return detectOn(LocalDate.now());
    }

    public HolidayType detectOn(LocalDate date) {
        if (date == null) return HolidayType.DEFAULT;

        for (Map.Entry<HolidayType, DateRange> entry : CALENDAR.entrySet()) {
            if (entry.getValue().isInRange(date)) {
                return entry.getKey();
            }
        }
        return HolidayType.DEFAULT;
    }

    public int getHolidayCount() {
        return CALENDAR.size();
    }

    // ═══════════════ MESSAGE ═══════════════

    private void sendMessage(Player player, HolidayType holiday) {
        MessageKey key = mapHolidayToKey(holiday);
        if (key == null) return;

        // ⚠️ Локаль получателя, а не язык плагина
        List<String> messages = lang.getMessageList(player, key);
        if (messages.isEmpty()) return;

        String template = messages.get(random.nextInt(messages.size()));
        Component message = render(template, player);
        player.sendMessage(message);
    }

    private MessageKey mapHolidayToKey(HolidayType holiday) {
        return switch (holiday) {
            case NEW_YEAR     -> MessageKey.HOLIDAY_NEW_YEAR;
            case CHRISTMAS    -> MessageKey.HOLIDAY_CHRISTMAS;
            case VALENTINE    -> MessageKey.HOLIDAY_VALENTINE;
            case HALLOWEEN    -> MessageKey.HOLIDAY_HALLOWEEN;
            case EASTER       -> MessageKey.HOLIDAY_EASTER;
            case WOMENS_DAY   -> MessageKey.HOLIDAY_WOMENS_DAY;
            case DEFENDER_DAY -> MessageKey.HOLIDAY_DEFENDER_DAY;
            case APRIL_FOOLS  -> MessageKey.HOLIDAY_APRIL_FOOLS;
            case VICTORY_DAY  -> MessageKey.HOLIDAY_VICTORY_DAY;
            case DEFAULT      -> MessageKey.HOLIDAY_DEFAULT;
        };
    }

    // ═══════════════ EFFECTS ═══════════════

    private void playEffect(Player player, HolidayType type) {
        if (player == null || !player.isOnline()) return;
        if (type == null || type.isDefault()) return;

        // Звук
        try {
            player.playSound(player.getLocation(),
                    type.getSound(),
                    (float) config.holidays.volume,
                    (float) config.holidays.pitch);
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка звука праздника: " + e.getMessage());
        }

        // Частицы
        try {
            player.getWorld().spawnParticle(
                    type.getParticle(),
                    player.getLocation().add(0, 1, 0),
                    config.holidays.particleAmount,
                    0.5, 0.5, 0.5, 0.01
            );
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка частиц праздника: " + e.getMessage());
        }
    }

    // ═══════════════ RENDER ═══════════════

    private Component render(String text, Player player) {
        String prepared = text;

        if (integrations != null && integrations.isPapiEnabled()
                && integrations.getPlaceholder() != null) {
            prepared = integrations.getPlaceholder().parse(player, prepared);
        }

        return lang.render(prepared, player);
    }
}