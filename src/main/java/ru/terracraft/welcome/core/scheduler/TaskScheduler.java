package ru.terracraft.welcome.core.scheduler;

import org.bukkit.scheduler.BukkitTask;
import ru.terracraft.welcome.WelcomePlugin;
import ru.terracraft.welcome.core.feature.HolidayService;
import ru.terracraft.welcome.core.util.Utils;

/**
 * Единая точка управления периодическими задачами.
 * Хранит BukkitTask, запускает/останавливает, перезапускает.
 */
public class TaskScheduler {

    private final WelcomePlugin plugin;
    private final HolidayService holidayService;

    private BukkitTask holidayTask;

    public TaskScheduler(WelcomePlugin plugin, HolidayService holidayService) {
        this.plugin = plugin;
        this.holidayService = holidayService;
    }

    // ═══════════════ START ═══════════════

    public void startAll() {
        startHolidayTask();
    }

    private void startHolidayTask() {
        if (holidayTask != null && !holidayTask.isCancelled()) {
            plugin.getLogger().warning("Задача праздников уже запущена");
            return;
        }

        if (!holidayService.isEnabled()) {
            plugin.getLogger().info("Праздничные эффекты выключены в конфиге");
            return;
        }

        var currentHoliday = holidayService.getCurrentHoliday();
        if (currentHoliday.isDefault()) {
            plugin.getLogger().info("Сегодня нет праздников — задача не запущена");
            return;
        }

        long interval = holidayService.getIntervalTicks();
        plugin.getLogger().info("Запуск задачи праздников ("
                + currentHoliday.getKey() + ", каждые " + interval + " тиков)");

        this.holidayTask = Utils.syncTimer(
                plugin,
                holidayService::applyToAll,
                20L,
                interval
        );
    }

    // ═══════════════ STOP ═══════════════

    public void stopAll() {
        cancelHolidayTask();
    }

    private void cancelHolidayTask() {
        if (holidayTask != null) {
            Utils.cancel(holidayTask);
            holidayTask = null;
        }
    }

    // ═══════════════ RESTART ═══════════════

    public void restartHolidayTask() {
        cancelHolidayTask();
        startHolidayTask();
    }

    // ═══════════════ STATUS ═══════════════

    public boolean isHolidayTaskRunning() {
        return holidayTask != null && !holidayTask.isCancelled();
    }

    public BukkitTask getHolidayTask() {
        return holidayTask;
    }
}