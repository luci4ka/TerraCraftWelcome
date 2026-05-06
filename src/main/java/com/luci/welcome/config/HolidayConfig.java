package com.luci.welcome.config;

import com.luci.welcome.utils.LoggerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;

public class HolidayConfig {

    private final ConfigHandler config;
    private final LoggerUtil logger;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Random random = new Random();

    private static final Map<String, DateRange> BUILT_IN_HOLIDAYS = new LinkedHashMap<>();

    static {
        BUILT_IN_HOLIDAYS.put("new_year", new DateRange(Month.DECEMBER, 28, Month.JANUARY, 7));
        BUILT_IN_HOLIDAYS.put("christmas", new DateRange(Month.DECEMBER, 23, Month.DECEMBER, 27));
        BUILT_IN_HOLIDAYS.put("valentine", new DateRange(Month.FEBRUARY, 13, Month.FEBRUARY, 15));
        BUILT_IN_HOLIDAYS.put("halloween", new DateRange(Month.OCTOBER, 28, Month.OCTOBER, 31));
        BUILT_IN_HOLIDAYS.put("easter", new DateRange(Month.APRIL, 10, Month.APRIL, 20));
        BUILT_IN_HOLIDAYS.put("womens_day", new DateRange(Month.MARCH, 6, Month.MARCH, 9));
        BUILT_IN_HOLIDAYS.put("defender_day", new DateRange(Month.FEBRUARY, 21, Month.FEBRUARY, 24));
        BUILT_IN_HOLIDAYS.put("april_fools", new DateRange(Month.APRIL, 1, Month.APRIL, 1));
        BUILT_IN_HOLIDAYS.put("victory_day", new DateRange(Month.MAY, 7, Month.MAY, 10));
    }

    public HolidayConfig(ConfigHandler config) {
        this.config = config;
        this.logger = new LoggerUtil(LoggerFactory.getLogger("Welcome-Holidays"));
    }

    public boolean isEnabled() {
        return config.getBoolean("settings.holidays.enabled", false);
    }

    public String getCurrentHoliday() {
        String forcedHoliday = config.getString("settings.holidays.current", "auto");
        if (!forcedHoliday.equalsIgnoreCase("auto")) {
            return forcedHoliday;
        }
        return detectHolidayByDate();
    }

    public int getIntervalTicks() {
        return Math.max(100, config.getInt("settings.holidays.interval_ticks", 1200));
    }

    /**
     * Получить список сообщений для праздника из языкового файла
     */
    public List<String> getMessages(String holiday) {
        // ✅ Берём из языкового файла: holidays.new_year.messages, holidays.halloween.messages...
        List<String> messages = config.getLangMessageList("holidays." + holiday + ".messages");
        if (messages.isEmpty()) {
            messages = getDefaultMessages(holiday);
        }
        return messages;
    }

    public String getRandomMessage(String holiday, Player player) {
        List<String> messages = getMessages(holiday);
        if (messages.isEmpty()) {
            return "<gold>С праздником, %player_name%!</gold>";
        }
        String message = messages.get(random.nextInt(messages.size()));
        return formatMessage(message, player);
    }

    public String formatMessage(String message, Player player) {
        if (message == null) return "";
        if (player != null) {
            message = message.replace("%player_name%", player.getName())
                    .replace("%player_displayname%", player.getDisplayName())
                    .replace("%player_world%", player.getWorld().getName());
        }
        message = config.replaceLinksWithMiniMessage(message);
        return message;
    }

    public Component deserialize(String message) {
        if (message == null || message.isEmpty()) return Component.empty();
        try {
            return miniMessage.deserialize(message);
        } catch (Exception e) {
            logger.error("Ошибка десериализации: " + message, e);
            return Component.text(message);
        }
    }

    public Component getMessageComponent(String holiday, Player player) {
        return deserialize(getRandomMessage(holiday, player));
    }

    public Particle getParticle(String holiday) {
        String particleName = config.getString("settings.holidays." + holiday + ".particle", "FIREWORK");
        try {
            return Particle.valueOf(particleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Неверное имя частицы: {}", particleName);
            return Particle.FIREWORK;
        }
    }

    public Sound getSound(String holiday) {
        String soundName = config.getString("settings.holidays." + holiday + ".sound", "ENTITY_PLAYER_LEVELUP");
        try {
            return Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Неверное имя звука: {}", soundName);
            return Sound.ENTITY_PLAYER_LEVELUP;
        }
    }

    public void playHolidayEffect(Player player, String holiday) {
        if (player == null || !player.isOnline()) return;
        try {
            Sound sound = getSound(holiday);
            float volume = (float) config.getDouble("settings.holidays.settings.volume", 1.0);
            float pitch = (float) config.getDouble("settings.holidays.settings.pitch", 1.0);
            player.playSound(player.getLocation(), sound, volume, pitch);

            Particle particle = getParticle(holiday);
            int amount = config.getInt("settings.holidays.settings.particle_amount", 10);
            player.getWorld().spawnParticle(particle, player.getLocation(), amount);
        } catch (Exception e) {
            logger.error("Ошибка эффекта для " + player.getName(), e);
        }
    }

    public void sendHolidayMessage(Player player) {
        if (player == null || !player.isOnline() || !isEnabled()) return;
        String holiday = getCurrentHoliday();
        Component message = getMessageComponent(holiday, player);
        if (message != Component.empty()) {
            player.sendMessage(message);
            playHolidayEffect(player, holiday);
        }
    }

    public Set<String> getAvailableHolidays() {
        return BUILT_IN_HOLIDAYS.keySet();
    }

    private String detectHolidayByDate() {
        LocalDate now = LocalDate.now();
        for (Map.Entry<String, DateRange> entry : BUILT_IN_HOLIDAYS.entrySet()) {
            if (entry.getValue() != null && entry.getValue().isInRange(now)) {
                return entry.getKey();
            }
        }
        return "default";
    }

    private List<String> getDefaultMessages(String holiday) {
        return List.of("<gold>🎉 С праздником, %player_name%!</gold>");
    }

    private static class DateRange {
        private final Month startMonth;
        private final int startDay;
        private final Month endMonth;
        private final int endDay;

        DateRange(Month startMonth, int startDay, Month endMonth, int endDay) {
            this.startMonth = startMonth;
            this.startDay = startDay;
            this.endMonth = endMonth;
            this.endDay = endDay;
        }

        boolean isInRange(LocalDate date) {
            LocalDate start = LocalDate.of(date.getYear(), startMonth, startDay);
            LocalDate end = LocalDate.of(date.getYear(), endMonth, endDay);
            if (startMonth.compareTo(endMonth) > 0) {
                if (date.getMonthValue() < startMonth.getValue()) start = start.minusYears(1);
                else end = end.plusYears(1);
            }
            return !date.isBefore(start) && !date.isAfter(end);
        }
    }

    public LoggerUtil getLogger() {
        return logger;
    }
}