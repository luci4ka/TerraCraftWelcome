package com.luci.welcome.task;

import com.luci.welcome.Main;
import com.luci.welcome.config.HolidayConfig;
import com.luci.welcome.utils.LoggerUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class HolidayEffectTask {

    private final Main plugin;
    private final HolidayConfig holidayConfig;
    private final LoggerUtil logger;
    private final Random random = new Random();
    private BukkitTask task;
    private int intervalTicks;
    private String lastHoliday = "";
    private final AtomicInteger effectCounter = new AtomicInteger(0);

    public HolidayEffectTask(Main plugin, HolidayConfig holidayConfig) {
        this.plugin = plugin;
        this.holidayConfig = holidayConfig;
        this.intervalTicks = holidayConfig.getIntervalTicks();
        this.logger = new LoggerUtil(LoggerFactory.getLogger("Welcome-HolidayTask"));
    }

    /**
     * Запускает задачу праздничных эффектов
     */
    public void start() {
        if (task != null && !task.isCancelled()) {
            logger.warn("Задача праздничных эффектов уже запущена");
            return;
        }

        if (!holidayConfig.isEnabled()) {
            logger.info("Праздничные эффекты отключены в конфиге");
            return;
        }

        // Проверяем текущий праздник
        String holiday = holidayConfig.getCurrentHoliday();
        if (holiday.equals("default")) {
            logger.info("Сегодня нет праздников, задача не запущена");
            return;
        }

        task = Bukkit.getScheduler().runTaskTimer(plugin, this::applyHolidayEffects, 20L, intervalTicks);
    }

    /**
     * Останавливает задачу
     */
    public void cancel() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * Перезапускает задачу с новым интервалом
     */
    public void restart(int newIntervalTicks) {
        cancel();
        this.intervalTicks = Math.max(100, newIntervalTicks);
        start();
    }

    /**
     * Применяет праздничные эффекты ко всем игрокам
     */
    private void applyHolidayEffects() {
        if (!holidayConfig.isEnabled()) {
            cancel();
            return;
        }

        String holiday = holidayConfig.getCurrentHoliday();

        // Проверяем, изменился ли праздник
        if (!holiday.equals(lastHoliday)) {
            lastHoliday = holiday;
            logger.info("Праздник изменён на: {}", holiday);
        }

        // Если сегодня нет праздника - останавливаем
        if (holiday.equals("default")) {
            logger.info("Праздник закончился, останавливаем задачу");
            cancel();
            return;
        }

        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        if (onlinePlayers.isEmpty()) {
            logger.debug("Нет игроков онлайн, пропускаем эффекты");
            return;
        }

        // Кэшируем настройки праздника
        Particle particle = holidayConfig.getParticle(holiday);
        Sound sound = holidayConfig.getSound(holiday);

        int playersAffected = 0;
        for (Player player : onlinePlayers) {
            if (player == null || !player.isOnline()) continue;

            try {
                // Применяем эффекты
                applyEffectsToPlayer(player, holiday, particle, sound);
                playersAffected++;
            } catch (Exception e) {
                logger.error("Ошибка применения эффектов для игрока " + player.getName(), e);
            }
        }

        if (playersAffected > 0) {
            effectCounter.incrementAndGet();
        }
    }

    /**
     * Применяет эффекты к конкретному игроку
     */
    private void applyEffectsToPlayer(Player player, String holiday, Particle particle, Sound sound) {
        // Спавним частицы
        int particleAmount = plugin.getConfig().getInt("holidays.settings.particle_amount", 10);
        player.getWorld().spawnParticle(particle, player.getLocation(), particleAmount);

        // Воспроизводим звук
        float volume = (float) plugin.getConfig().getDouble("holidays.settings.volume", 1.0);
        float pitch = (float) plugin.getConfig().getDouble("holidays.settings.pitch", 1.0);
        player.playSound(player.getLocation(), sound, volume, pitch);

        // Отправляем сообщение (опционально)
        boolean sendMessages = plugin.getConfig().getBoolean("holidays.settings.send_messages", true);
        if (sendMessages) {
            String message = holidayConfig.getRandomMessage(holiday, player);
            if (message != null && !message.isEmpty()) {
                Component component = holidayConfig.deserialize(message);
                player.sendMessage(component);
            }
        }

        // Дополнительные эффекты
        applyExtraEffects(player, holiday);
    }

    /**
     * Применяет дополнительные праздничные эффекты
     */
    private void applyExtraEffects(Player player, String holiday) {
        switch (holiday) {
            case "new_year":
                // Снежинки вокруг игрока
                player.getWorld().spawnParticle(Particle.SNOWFLAKE,
                        player.getLocation().add(0, 2, 0), 5);
                break;
            case "halloween":
                // Летучие мыши для хэллоуина
                player.getWorld().spawnParticle(Particle.WITCH,
                        player.getLocation().add(0, 1, 0), 3);
                break;
            case "valentine":
                // Сердечки
                player.getWorld().spawnParticle(Particle.HEART,
                        player.getLocation().add(0, 2, 0), 2);
                break;
            case "christmas":
                // Снежинки
                player.getWorld().spawnParticle(Particle.SNOWFLAKE,
                        player.getLocation().add(0, 2, 0), 8);
                break;
        }
    }

    /**
     * Принудительно применяет эффекты ко всем
     */
    public void forceApply() {
        logger.debug("Принудительное применение праздничных эффектов");
        applyHolidayEffects();
    }

    /**
     * Применяет эффекты к конкретному игроку
     */
    public void applyToPlayer(Player player) {
        if (player == null || !player.isOnline() || !holidayConfig.isEnabled()) return;

        String holiday = holidayConfig.getCurrentHoliday();
        if (holiday.equals("default")) return;

        Particle particle = holidayConfig.getParticle(holiday);
        Sound sound = holidayConfig.getSound(holiday);
        applyEffectsToPlayer(player, holiday, particle, sound);
    }

    // Геттеры
    public boolean isRunning() {
        return task != null && !task.isCancelled();
    }

    public int getIntervalTicks() {
        return intervalTicks;
    }

    public int getEffectsApplied() {
        return effectCounter.get();
    }

    public String getCurrentHoliday() {
        return holidayConfig.getCurrentHoliday();
    }

    public HolidayConfig getConfig() {
        return holidayConfig;
    }

    public LoggerUtil getLogger() {
        return logger;
    }
}