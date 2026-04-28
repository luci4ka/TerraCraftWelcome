package com.luci.welcome.config;

import com.luci.welcome.utils.LoggerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class AutoMessageConfig {

    private final ConfigHandler config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Random random = new Random();
    private final LoggerUtil logger;

    private final List<String> allMessages = new ArrayList<>();
    private String lastMessage = "";

    public AutoMessageConfig(ConfigHandler config) {
        this.config = config;
        this.logger = new LoggerUtil(LoggerFactory.getLogger("Welcome-AutoMessages"));
        loadMessages();
    }

    public boolean isEnabled() {
        return config.getBoolean("auto_messages.enabled", true);
    }

    public int getIntervalTicks() {
        return Math.max(100, config.getInt("auto_messages.interval_ticks", 6000));
    }

    /**
     * Загружает все авто-сообщения и сообщения персонажей из конфигурации
     */
    public void loadMessages() {
        allMessages.clear();

        // Загружаем базовые авто-сообщения из языкового файла
        List<String> baseMessages = config.getLangMessageList("auto_messages");
        if (!baseMessages.isEmpty()) {
            allMessages.addAll(baseMessages);
            logger.info("Загружено " + baseMessages.size() + " базовых авто-сообщений из языка");
        }

        // Загружаем сообщения персонажей из конфига
        loadCharacterMessages();

        logger.info("Итоговое количество авто-сообщений: " + allMessages.size());
    }

    /**
     * Загружает сообщения персонажей из конфигурации
     */
    private void loadCharacterMessages() {
        List<Map<?, ?>> charactersRaw = config.getMapList("auto_messages.characters");
        if (charactersRaw == null || charactersRaw.isEmpty()) {
            logger.debug("Персонажи не найдены в конфиге");
            return;
        }

        ConfigurationSection messagesByChar = config.getConfigurationSection("auto_messages.messages_by_character");
        if (messagesByChar == null) {
            logger.warn("Секция 'messages_by_character' не найдена");
            return;
        }

        int charMessageCount = 0;
        for (Map<?, ?> rawCharMap : charactersRaw) {
            Map<String, Object> charMap = convertMap(rawCharMap);
            if (charMap.isEmpty()) continue;

            String name = Objects.toString(charMap.get("name"), "Неизвестный");
            String color = Objects.toString(charMap.get("color"), "<white>");
            String prefix = Objects.toString(charMap.get("prefix"), "");

            Map<String, String> moods = getMoodsMap(charMap.get("moods"));

            for (Map.Entry<String, String> moodEntry : moods.entrySet()) {
                String mood = color + moodEntry.getValue();

                for (String category : messagesByChar.getKeys(false)) {
                    List<String> msgs = messagesByChar.getStringList(category);

                    for (String msg : msgs) {
                        if (msg == null || msg.isEmpty()) continue;

                        String finalMsg = msg
                                .replace("%character_name%", name)
                                .replace("%character_prefix%", prefix)
                                .replace("%character_color%", color)
                                .replace("%character_mood%", mood);
                        allMessages.add(finalMsg);
                        charMessageCount++;
                    }
                }
            }
        }

        logger.debug("Загружено " + charMessageCount + " сообщений от персонажей");
    }

    /**
     * Безопасное преобразование Map
     */
    private Map<String, Object> convertMap(Map<?, ?> rawMap) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            if (entry.getKey() instanceof String key) {
                result.put(key, entry.getValue());
            }
        }
        return result;
    }

    /**
     * Безопасное получение Map с эмоциями
     */
    private Map<String, String> getMoodsMap(Object moodsObject) {
        if (moodsObject instanceof Map<?, ?> rawMoods) {
            return rawMoods.entrySet().stream()
                    .filter(e -> e.getKey() instanceof String && e.getValue() instanceof String)
                    .collect(Collectors.toMap(
                            e -> (String) e.getKey(),
                            e -> (String) e.getValue()
                    ));
        }
        return new HashMap<>();
    }

    /**
     * Возвращает случайное сообщение для игрока
     */
    public String getRandomMessage(Player player) {
        if (allMessages.isEmpty()) {
            return "<red>Нет доступных сообщений</red>";
        }

        String message;
        int attempts = 0;
        int maxAttempts = Math.min(allMessages.size(), 10);

        do {
            message = allMessages.get(random.nextInt(allMessages.size()));
            attempts++;
        } while (allMessages.size() > 1 && message.equals(lastMessage) && attempts < maxAttempts);

        lastMessage = message;

        // Заменяем плейсхолдеры игрока
        if (player != null) {
            message = message.replace("%player_name%", player.getName())
                    .replace("%player_displayname%", player.getDisplayName())
                    .replace("%player_world%", player.getWorld().getName());
        }

        return message;
    }

    /**
     * Преобразует строку в Component для отправки игроку
     */
    public Component deserialize(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }

        try {
            String withLinks = config.replaceLinksWithMiniMessage(message);
            return miniMessage.deserialize(withLinks);
        } catch (Exception e) {
            logger.error("Ошибка десериализации сообщения: " + message, e);
            return Component.text(message);
        }
    }

    /**
     * Отправляет сообщение игроку
     */
    public void sendMessage(Player player, String message) {
        if (player == null || !player.isOnline() || message == null) return;

        try {
            Component component = deserialize(message);
            player.sendMessage(component);
        } catch (Exception e) {
            logger.error("Ошибка отправки сообщения игроку " + player.getName(), e);
        }
    }

    /**
     * Проверить, есть ли сообщения
     */
    public boolean hasMessages() {
        return !allMessages.isEmpty();
    }

    public LoggerUtil getLogger() {
        return logger;
    }
}