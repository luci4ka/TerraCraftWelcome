package ru.net.terracraft.welcome.core.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Единый класс утилит.
 * Всё в одном: текст, плейсхолдеры, расписание, версии.
 */
public final class Utils {

    private Utils() {
        // утилитный класс
    }

    // ═══════════════════════════════════════════
    //  TEXT — Adventure + MiniMessage
    // ═══════════════════════════════════════════

    private static final MiniMessage MM = MiniMessage.miniMessage();

    /**
     * Парсит MiniMessage-строку в Component.
     * При ошибке — обычный текст.
     */
    public static Component parse(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        try {
            return MM.deserialize(text);
        } catch (Exception e) {
            return Component.text(text);
        }
    }

    /**
     * Парсит текст с заменой плейсхолдеров.
     */
    public static Component parse(String text, Player player) {
        return parse(applyPlaceholders(text, player));
    }

    /**
     * Component → обычный текст.
     */
    public static String plain(Component component) {
        if (component == null) return "";
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    /**
     * Убирает legacy-цвета &a, &b и т.д.
     */
    public static String stripLegacyColors(String text) {
        if (text == null) return "";
        return text.replaceAll("(?i)&[0-9a-fk-or]", "");
    }

    // ═══════════════════════════════════════════
    //  PLACEHOLDERS — замена %...% без PAPI
    // ═══════════════════════════════════════════

    /**
     * Заменяет встроенные плейсхолдеры.
     * Работает без PAPI.
     */
    public static String applyPlaceholders(String text, Player player) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        if (player != null) {
            text = text
                    .replace("%player%", player.getName())
                    .replace("%player_name%", player.getName())
                    .replace("%player_displayname%", player.getDisplayName())
                    .replace("%player_world%", player.getWorld().getName());
        }

        text = text
                .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%online_players%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%max_players%", String.valueOf(Bukkit.getMaxPlayers()));

        return text;
    }

    /**
     * Убирает незаменённые плейсхолдеры %xxx%.
     */
    public static String stripUnknownPlaceholders(String text) {
        if (text == null) return "";
        return text.replaceAll("%[a-zA-Z0-9_]+%", "");
    }

    // ═══════════════════════════════════════════
    //  SCHEDULE — обёртки над Bukkit scheduler
    // ═══════════════════════════════════════════

    public static BukkitTask syncLater(Plugin plugin, Runnable task, long delayTicks) {
        return Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    public static BukkitTask syncTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
        return Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
    }

    public static BukkitTask async(Plugin plugin, Runnable task) {
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    public static BukkitTask asyncLater(Plugin plugin, Runnable task, long delayTicks) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks);
    }

    public static BukkitTask asyncTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
    }

    public static void cancel(BukkitTask task) {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    // ═══════════════════════════════════════════
    //  VERSION — semver сравнение
    // ═══════════════════════════════════════════

    /**
     * Сравнение semver-версий.
     * Поддерживает: 1.2.3, v1.2.3, 1.2.3-beta.1, 1.2.3+build.5
     */
    public static final class Version implements Comparable<Version> {

        private final int major;
        private final int minor;
        private final int patch;
        private final String original;

        private Version(int major, int minor, int patch, String original) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.original = original;
        }

        public static Version parse(String input) {
            if (input == null || input.isBlank()) {
                throw new IllegalArgumentException("Пустая версия");
            }

            String cleaned = input.trim();
            if (cleaned.startsWith("v") || cleaned.startsWith("V")) {
                cleaned = cleaned.substring(1);
            }

            int dashIdx = cleaned.indexOf('-');
            if (dashIdx > 0) cleaned = cleaned.substring(0, dashIdx);

            int plusIdx = cleaned.indexOf('+');
            if (plusIdx > 0) cleaned = cleaned.substring(0, plusIdx);

            String[] parts = cleaned.split("\\.");
            int major = parts.length > 0 ? parseIntSafe(parts[0]) : 0;
            int minor = parts.length > 1 ? parseIntSafe(parts[1]) : 0;
            int patch = parts.length > 2 ? parseIntSafe(parts[2]) : 0;

            return new Version(major, minor, patch, input);
        }

        public static int compareSafe(String a, String b) {
            try {
                return parse(a).compareTo(parse(b));
            } catch (Exception e) {
                return 0;
            }
        }

        public static boolean isNewer(String latest, String current) {
            return compareSafe(latest, current) > 0;
        }

        private static int parseIntSafe(String s) {
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        @Override
        public int compareTo(Version other) {
            int cmp = Integer.compare(this.major, other.major);
            if (cmp != 0) return cmp;

            cmp = Integer.compare(this.minor, other.minor);
            if (cmp != 0) return cmp;

            return Integer.compare(this.patch, other.patch);
        }

        @Override
        public String toString() {
            return original;
        }

        public int getMajor() { return major; }
        public int getMinor() { return minor; }
        public int getPatch() { return patch; }
    }

    /**
     * Сравнить две версии (короткая обёртка).
     */
    public static boolean isNewerVersion(String latest, String current) {
        return Version.isNewer(latest, current);
    }
}