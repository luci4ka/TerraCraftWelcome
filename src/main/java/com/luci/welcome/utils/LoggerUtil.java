package com.luci.welcome.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.slf4j.Logger;

public class LoggerUtil {

    private final Logger logger;

    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";

    public LoggerUtil(Logger logger) {
        if (logger == null) {
            throw new IllegalArgumentException("Logger не может быть null!");
        }
        this.logger = logger;
    }

    public void info(String message) {
        logger.info(message);
        sendConsoleComponent(message, NamedTextColor.GREEN);
    }

    public void warn(String message) {
        logger.warn(message);
        sendConsoleComponent(message, NamedTextColor.YELLOW);
    }

    public void warn(String message, Object... args) {
        try {
            String formatted = String.format(message, args);
            logger.warn(formatted);
            sendConsoleComponent(formatted, NamedTextColor.YELLOW);
        } catch (Exception e) {
            logger.warn("Ошибка форматирования сообщения: {}", message, e);
        }
    }

    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
        sendConsoleComponent(message + " - " + throwable.getMessage(), NamedTextColor.RED);
    }

    public void debug(String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(message);
            sendConsoleComponent(message, NamedTextColor.AQUA);
        }
    }

    public void info(String message, String color) {
        // Конвертируем ANSI цвет в NamedTextColor (для обратной совместимости)
        NamedTextColor namedColor = convertAnsiToNamedColor(color);
        logger.info(message);
        sendConsoleComponent(message, namedColor);
    }

    private void sendConsoleComponent(String message, NamedTextColor color) {
        try {
            Component component = Component.text()
                    .append(Component.text("[Welcome] ", NamedTextColor.GOLD, TextDecoration.BOLD))
                    .append(Component.text(message, color))
                    .build();
            Bukkit.getConsoleSender().sendMessage(component);
        } catch (Exception ignored) {
            // Если не удалось отправить компонент, используем обычный логгер
        }
    }

    private NamedTextColor convertAnsiToNamedColor(String ansiColor) {
        if (ansiColor == null) return NamedTextColor.WHITE;

        // Сравниваем с ANSI кодами
        return switch (ansiColor) {
            case RED -> NamedTextColor.RED;
            case GREEN -> NamedTextColor.GREEN;
            case YELLOW -> NamedTextColor.YELLOW;
            case BLUE -> NamedTextColor.BLUE;
            case CYAN -> NamedTextColor.AQUA;
            default -> NamedTextColor.WHITE;
        };

    }

    @SuppressWarnings("unused")
    public Logger getSlf4jLogger() {
        return logger;
    }
}