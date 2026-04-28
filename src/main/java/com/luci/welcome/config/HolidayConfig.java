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

    // Карта праздников с датами
    private static final Map<String, DateRange> BUILT_IN_HOLIDAYS = new LinkedHashMap<>();

    static {
        BUILT_IN_HOLIDAYS.put("new_year", new DateRange(Month.DECEMBER, 28, Month.JANUARY, 7));
        BUILT_IN_HOLIDAYS.put("christmas", new DateRange(Month.DECEMBER, 23, Month.DECEMBER, 27));
        BUILT_IN_HOLIDAYS.put("valentine", new DateRange(Month.FEBRUARY, 13, Month.FEBRUARY, 15));
        BUILT_IN_HOLIDAYS.put("halloween", new DateRange(Month.OCTOBER, 28, Month.OCTOBER, 31));
        BUILT_IN_HOLIDAYS.put("easter", new DateRange(Month.APRIL, 10, Month.APRIL, 20));
        BUILT_IN_HOLIDAYS.put("womens_day", new DateRange(Month.MARCH, 6, Month.MARCH, 9));    // 8 марта
        BUILT_IN_HOLIDAYS.put("defender_day", new DateRange(Month.FEBRUARY, 21, Month.FEBRUARY, 24)); // 23 февраля
        BUILT_IN_HOLIDAYS.put("april_fools", new DateRange(Month.APRIL, 1, Month.APRIL, 1));    // 1 апреля
        BUILT_IN_HOLIDAYS.put("victory_day", new DateRange(Month.MAY, 7, Month.MAY, 10));       // 9 мая
        BUILT_IN_HOLIDAYS.put("server_birthday", null); // Настраивается в конфиге
    }

    public HolidayConfig(ConfigHandler config) {
        this.config = config;
        this.logger = new LoggerUtil(LoggerFactory.getLogger("Welcome-Holidays"));
    }

    public boolean isEnabled() {
        return config.getBoolean("holidays.enabled", false);
    }

    public String getCurrentHoliday() {
        // Приоритет: конфиг > автоопределение по дате
        String forcedHoliday = config.getString("holidays.current", null);
        if (forcedHoliday != null && !forcedHoliday.equalsIgnoreCase("auto")) {
            return forcedHoliday;
        }

        // Автоопределение
        return detectHolidayByDate();
    }

    public int getIntervalTicks() {
        return Math.max(100, config.getInt("holidays.interval_ticks", 1200));
    }

    /**
     * Получить список сообщений для праздника
     */
    public List<String> getMessages(String holiday) {
        List<String> messages = config.getStringList("holidays." + holiday + ".messages");

        if (messages.isEmpty()) {
            // Запасные сообщения если нет в конфиге
            messages = getDefaultMessages(holiday);
        }

        return messages;
    }

    /**
     * Получить случайное сообщение
     */
    public String getRandomMessage(String holiday, Player player) {
        List<String> messages = getMessages(holiday);
        if (messages.isEmpty()) {
            return "<gold>С праздником, %player_name%!</gold>";
        }

        String message = messages.get(random.nextInt(messages.size()));
        return formatMessage(message, player);
    }

    /**
     * Форматирование сообщения
     */
    public String formatMessage(String message, Player player) {
        if (message == null) return "";

        // Замена плейсхолдеров
        if (player != null) {
            message = message.replace("%player_name%", player.getName())
                    .replace("%player_displayname%", player.getDisplayName())
                    .replace("%player_world%", player.getWorld().getName());
        }

        // Замена ссылок с MiniMessage
        message = config.replaceLinksWithMiniMessage(message); // ИСПРАВЛЕНО

        return message;
    }

    /**
     * Создать Component из сообщения
     */
    public Component deserialize(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }

        try {
            return miniMessage.deserialize(message);
        } catch (Exception e) {
            logger.error("Ошибка десериализации сообщения: " + message, e);
            return Component.text(message);
        }
    }

    /**
     * Получить компонент для отправки
     */
    public Component getMessageComponent(String holiday, Player player) {
        String message = getRandomMessage(holiday, player);
        return deserialize(message);
    }

    /**
     * Получить частицу для праздника
     */
    public Particle getParticle(String holiday) {
        String particleName = config.getString("holidays." + holiday + ".particle", "FIREWORKS_SPARK");
        try {
            return Particle.valueOf(particleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Неверное имя частицы для праздника '{}': {}", holiday, particleName);
            return Particle.FIREWORK;
        }
    }

    /**
     * Получить звук для праздника
     */
    public Sound getSound(String holiday) {
        String soundName = config.getString("holidays." + holiday + ".sound", "ENTITY_PLAYER_LEVELUP");
        try {
            return Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Неверное имя звука для праздника '{}': {}", holiday, soundName);
            return Sound.ENTITY_PLAYER_LEVELUP;
        }
    }

    /**
     * Воспроизвести праздничный эффект для игрока
     */
    public void playHolidayEffect(Player player, String holiday) {
        if (player == null || !player.isOnline()) return;

        try {
            // Воспроизводим звук
            Sound sound = getSound(holiday);
            float volume = (float) config.getDouble("holidays.settings.volume", 1.0);
            float pitch = (float) config.getDouble("holidays.settings.pitch", 1.0);
            player.playSound(player.getLocation(), sound, volume, pitch);

            // Спавним частицы
            Particle particle = getParticle(holiday);
            int amount = config.getInt("holidays.settings.particle_amount", 10);
            player.getWorld().spawnParticle(particle, player.getLocation(), amount);
        } catch (Exception e) {
            logger.error("Ошибка воспроизведения эффекта для игрока " + player.getName(), e);
        }
    }

    /**
     * Отправить праздничное сообщение игроку
     */
    public void sendHolidayMessage(Player player) {
        if (player == null || !player.isOnline() || !isEnabled()) return;

        String holiday = getCurrentHoliday();
        Component message = getMessageComponent(holiday, player);

        if (message != Component.empty()) {
            player.sendMessage(message);
            playHolidayEffect(player, holiday);
        }
    }

    /**
     * Получить список всех доступных праздников
     */
    public Set<String> getAvailableHolidays() {
        Set<String> holidays = new HashSet<>(BUILT_IN_HOLIDAYS.keySet());

        // Добавляем праздники из конфига
        if (config.contains("holidays")) {
            var section = config.getConfigurationSection("holidays");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    if (!key.equals("enabled") && !key.equals("current") &&
                            !key.equals("interval_ticks") && !key.equals("settings")) {
                        holidays.add(key);
                    }
                }
            }
        }

        return holidays;
    }

    /**
     * Определить праздник по текущей дате
     */
    private String detectHolidayByDate() {
        LocalDate now = LocalDate.now();

        for (Map.Entry<String, DateRange> entry : BUILT_IN_HOLIDAYS.entrySet()) {
            if (entry.getValue().isInRange(now)) {
                return entry.getKey();
            }
        }

        return "default";
    }

    /**
     * Запасные сообщения если нет в конфиге
     */
    private List<String> getDefaultMessages(String holiday) {
        Map<String, List<String>> defaults = new HashMap<>();
        defaults.put("new_year", List.of(
                "<gold>🎄 С Новым Годом, %player_name%! 🎄</gold>",
                "<green>Счастливого Нового Года! Пусть сбудутся все мечты!</green>"
        ));
        defaults.put("christmas", List.of(
                "<gold>🎅 С Рождеством, %player_name%! 🎅</gold>",
                "<red>Рождественского настроения и чудес!</red>"
        ));
        defaults.put("valentine", List.of(
                "<red>❤ С Днём Святого Валентина, %player_name%! ❤</red>",
                "<pink>Любви и счастья в этот прекрасный день!</pink>"
        ));
        defaults.put("halloween", List.of(
                "<gold>🎃 С Хэллоуином, %player_name%! 🎃</gold>",
                "<dark_purple>Сладость или гадость? Пусть будет только сладость!</dark_purple>"
        ));
        defaults.put("default", List.of(
                "<gold>🎉 С праздником, %player_name%! 🎉</gold>"
        ));

        return defaults.getOrDefault(holiday, defaults.get("default"));
    }

    /**
     * Вспомогательный класс для диапазона дат
     */
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

            // Если диапазон переходит через новый год
            if (startMonth.compareTo(endMonth) > 0) {
                if (date.getMonthValue() < startMonth.getValue()) {
                    start = start.minusYears(1);
                } else {
                    end = end.plusYears(1);
                }
            }

            return !date.isBefore(start) && !date.isAfter(end);
        }
    }

    public LoggerUtil getLogger() {
        return logger;
    }
}