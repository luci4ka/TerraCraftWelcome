package com.welcome.task;

import com.welcome.Main;
import com.welcome.config.ConfigHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class AutoMessageTask extends BukkitRunnable {

    private final Main plugin;
    private final ConfigHandler config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Random random = new Random();
    private int intervalTicks;
    private String lastMessage = "";

    public AutoMessageTask(Main plugin, ConfigHandler config) {
        this.plugin = plugin;
        this.config = config;
        this.intervalTicks = config.getInt("auto_messages.interval_ticks", 6000);
    }

    @Override
    public void run() {
        if (!config.getBoolean("auto_messages.enabled", true)) return;

        List<String> finalMessages = new ArrayList<>();
        finalMessages.addAll(config.getMessageList("auto_messages.messages")); // Старые сообщения

        // Загрузка персонажей
        ConfigurationSection charactersSection = plugin.getConfig().getConfigurationSection("auto_messages.characters");
        ConfigurationSection messagesSection = plugin.getConfig().getConfigurationSection("auto_messages.messages_by_character");

        if (charactersSection != null && messagesSection != null) {
            List<Map<String, Object>> characterMaps = new ArrayList<>();
            for (Object obj : plugin.getConfig().getList("auto_messages.characters", Collections.emptyList())) {
                if (obj instanceof Map<?, ?> rawMap) {
                    Map<String, Object> safeMap = new HashMap<>();
                    for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                        if (entry.getKey() instanceof String key) {
                            safeMap.put(key, entry.getValue());
                        }
                    }
                    characterMaps.add(safeMap);
                }
            }

            for (Map<String, Object> charMap : characterMaps) {
                String charName = Objects.toString(charMap.getOrDefault("name", ""), "");
                String charColor = Objects.toString(charMap.getOrDefault("color", "<white>"), "<white>");
                String charPrefix = Objects.toString(charMap.getOrDefault("prefix", ""), "");

                @SuppressWarnings("unchecked")
                Map<String, String> moods = charMap.get("moods") instanceof Map<?, ?> m
                        ? m.entrySet().stream()
                        .filter(e -> e.getKey() instanceof String k && e.getValue() instanceof String v)
                        .collect(HashMap::new, (hm, e) -> hm.put((String)e.getKey(), (String)e.getValue()), HashMap::putAll)
                        : Map.of();

                for (String category : messagesSection.getKeys(false)) {
                    List<String> messages = messagesSection.getStringList(category);
                    for (String msg : messages) {
                        // Случайное настроение
                        String mood = "";
                        if (!moods.isEmpty()) {
                            List<String> moodKeys = new ArrayList<>(moods.keySet());
                            mood = moods.get(moodKeys.get(random.nextInt(moodKeys.size())));
                        }

                        msg = msg.replace("%character_name%", charName)
                                .replace("%character_prefix%", charPrefix)
                                .replace("%character_color%", charColor)
                                .replace("%character_mood%", mood);

                        finalMessages.add(msg);
                    }
                }
            }
        }

        if (finalMessages.isEmpty()) return;

        // Выбираем случайное сообщение, отличное от последнего
        String message;
        do {
            message = finalMessages.get(random.nextInt(finalMessages.size()));
        } while (finalMessages.size() > 1 && message.equals(lastMessage));
        lastMessage = message;

        // Случайный онлайн игрок
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        Player randomPlayer = null;
        if (!onlinePlayers.isEmpty()) {
            int idx = random.nextInt(onlinePlayers.size());
            randomPlayer = new ArrayList<>(onlinePlayers).get(idx);
        }

        String playerName = randomPlayer != null ? randomPlayer.getName() : "Игрок";
        message = message.replace("%player_name%", playerName);

        // Заменяем ссылки
        message = config.replaceLinksMsg(message);

        Component component = miniMessage.deserialize(message);

        // Отправка всем игрокам
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(component);
        }
    }

    public void start() {
        this.runTaskTimer(plugin, 0L, intervalTicks);
    }

    public void setIntervalTicks(int intervalTicks) {
        this.intervalTicks = intervalTicks;
        this.cancel();
        start();
    }
}
