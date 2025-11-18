package com.luci.welcome.utils;

import org.slf4j.Logger;

public class LoggerUtil {

    private final Logger logger;

    // ANSI коды
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";

    public LoggerUtil(Logger logger) {
        this.logger = logger;
    }

    public void info(String message) {
        logger.info(GREEN + message + RESET);
    }

    public void warn(String message) {
        logger.warn(YELLOW + message + RESET);
    }
    public void warn(String message, Object... args) {
        logger.warn(YELLOW + String.format(message, args) + RESET);
    }

    public void error(String message) {
        logger.error(RED + message + RESET);
    }

    public void debug(String message) {
        logger.debug(CYAN + message + RESET);
    }

    // Метод для кастомного цвета
    public void info(String message, String color) {
        logger.info(color + message + RESET);
    }
}
