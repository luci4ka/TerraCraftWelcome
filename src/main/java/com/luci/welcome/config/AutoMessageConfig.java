package com.luci.welcome.config;

import com.luci.welcome.utils.LoggerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class AutoMessageConfig {

    private final ConfigHandler config;
    private final MiniMessage mini = MiniMessage.miniMessage();
    private final Random random = new Random();
    private final LoggerUtil logger;

    private final List<String> allMessages = new ArrayList<>();
    private String lastMessage = "";

    public AutoMessageConfig(ConfigHandler config) {
        this.config = config;
        this.logger = new LoggerUtil(config.getPlugin().getSLF4JLogger());
        loadMessages();
    }

    public boolean isEnabled() {
        return config.getBoolean("auto_messages.enabled", true);
    }

    public int getIntervalTicks() {
        return config.getInt("auto_messages.interval_ticks", 6000);
    }

    /**
     * Загружает все авто-сообщения и сообщения персонажей из конфигурации
     */
    public void loadMessages() {
        allMessages.clear();

        // Загружаем базовые авто-сообщения
        allMessages.addAll(config.getMessageList("auto_messages.messages"));
        logger.info("Загружено " + allMessages.size() + " авто-сообщений из конфигурации");

        // Получаем список персонажей
        List<Map<?, ?>> charactersRaw = config.getPlugin().getConfig().getMapList("auto_messages.characters");
        if (charactersRaw == null || charactersRaw.isEmpty()) return;

        ConfigurationSection messagesByChar = config.getConfigSection("auto_messages.messages_by_character");
        if (messagesByChar == null) return;

        for (Map<?, ?> rawCharMap : charactersRaw) {
            Map<String, Object> charMap = new HashMap<>();
            for (Map.Entry<?, ?> entry : rawCharMap.entrySet()) {
                if (entry.getKey() instanceof String key) charMap.put(key, entry.getValue());
            }

            String name = Objects.toString(charMap.get("name"), "");
            String color = Objects.toString(charMap.get("color"), "<white>");
            String prefix = Objects.toString(charMap.get("prefix"), "");

            @SuppressWarnings("unchecked")
            Map<String, String> moods = charMap.get("moods") instanceof Map<?, ?> m
                    ? m.entrySet().stream()
                    .filter(e -> e.getKey() instanceof String && e.getValue() instanceof String)
                    .collect(HashMap::new, (hm, e) -> hm.put((String) e.getKey(), (String) e.getValue()), HashMap::putAll)
                    : Map.of();

            // Для каждой эмоции персонажа добавляем все категории сообщений
            for (Map.Entry<String, String> moodEntry : moods.entrySet()) {
                String mood = color + moodEntry.getValue();
                for (String category : messagesByChar.getKeys(false)) {
                    List<String> msgs = messagesByChar.getStringList(category);
                    for (String msg : msgs) {
                        String finalMsg = msg.replace("%character_name%", name)
                                .replace("%character_prefix%", prefix)
                                .replace("%character_color%", color)
                                .replace("%character_mood%", mood);
                        allMessages.add(finalMsg);
                    }
                }
            }
        }

        logger.info("Итоговое количество авто-сообщений после добавления персонажей: " + allMessages.size());
    }

    /**
     * Возвращает случайное сообщение для игрока, не повторяющееся с последним
     */
    public String getRandomMessage(Player player) {
        if (allMessages.isEmpty()) return "";
        String message;
        int attempts = 0;
        do {
            message = allMessages.get(random.nextInt(allMessages.size()));
            attempts++;
        } while (allMessages.size() > 1 && message.equals(lastMessage) && attempts < 10);
        lastMessage = message;

        return message.replace("%player_name%", player.getName());
    }

    /**
     * Преобразует строку в Component для отправки игроку
     */
    public Component deserialize(String message) {
        return mini.deserialize(config.replaceLinksMsg(message));
    }

    /**
     * Получение логгера для внешнего использования
     */
    public LoggerUtil getLogger() {
        return logger;
    }
}
