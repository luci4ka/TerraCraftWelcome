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
    }

    public void warn(String message) {
        logger.warn(message);
    }

    public void warn(String message, Object... args) {
        try {
            logger.warn(String.format(message, args));
        } catch (Exception e) {
            logger.warn("Ошибка форматирования сообщения: {}", message, e);
        }
    }

    public void error(String message) {
        logger.error(message);
    }

    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    public void debug(String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(message);
        }
    }

    public void info(String message, String color) {
        logger.info(message);
    }

    public Logger getSlf4jLogger() {
        return logger;
    }
}