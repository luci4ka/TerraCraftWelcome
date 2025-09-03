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
    private final List<String> allMessages = new ArrayList<>();

    public AutoMessageTask(Main plugin, ConfigHandler config) {
        this.plugin = plugin;
        this.config = config;
        this.intervalTicks = config.getInt("auto_messages.interval_ticks", 6000);
        loadMessages(); // загружаем все сообщения при старте
    }

    private void loadMessages() {
        allMessages.clear();

        // старые
        allMessages.addAll(config.getMessageList("auto_messages.messages"));

        // новые
        List<Map<?, ?>> charactersRaw = plugin.getConfig().getMapList("auto_messages.characters");
        ConfigurationSection autoMessages = plugin.getConfig().getConfigurationSection("auto_messages");
        ConfigurationSection messagesSection = autoMessages != null ? autoMessages.getConfigurationSection("messages_by_character") : null;

        if (charactersRaw == null || charactersRaw.isEmpty() || messagesSection == null) return;

        for (Map<?, ?> rawCharMap : charactersRaw) {
            Map<String, Object> charMap = new HashMap<>();
            for (Map.Entry<?, ?> entry : rawCharMap.entrySet()) {
                if (entry.getKey() instanceof String key) {
                    charMap.put(key, entry.getValue());
                }
            }

            String charName = Objects.toString(charMap.get("name"), "");
            String charColor = Objects.toString(charMap.get("color"), "<white>");
            String charPrefix = Objects.toString(charMap.get("prefix"), "");

            @SuppressWarnings("unchecked")
            Map<String, String> moods = charMap.get("moods") instanceof Map<?, ?> m
                    ? m.entrySet().stream()
                    .filter(e -> e.getKey() instanceof String && e.getValue() instanceof String)
                    .collect(HashMap::new, (hm, e) -> hm.put((String) e.getKey(), (String) e.getValue()), HashMap::putAll)
                    : Map.of();

            for (String category : messagesSection.getKeys(false)) {
                List<String> messages = messagesSection.getStringList(category);
                for (String msg : messages) {
                    String mood = "";
                    if (!moods.isEmpty()) {
                        List<String> moodKeys = new ArrayList<>(moods.keySet());
                        String rawMood = moods.get(moodKeys.get(random.nextInt(moodKeys.size())));
                        // красим смайл в цвет персонажа
                        mood = charColor + rawMood;
                    }

                    msg = msg.replace("%character_name%", charName)
                            .replace("%character_prefix%", charPrefix)
                            .replace("%character_color%", charColor)
                            .replace("%character_mood%", mood);

                    allMessages.add(msg);
                }
            }
        }
    }

    @Override
    public void run() {
        if (!config.getBoolean("auto_messages.enabled", true)) return;
        if (allMessages.isEmpty()) return;

        // случайное сообщение ≠ последнему
        String message;
        int attempts = 0;
        do {
            message = allMessages.get(random.nextInt(allMessages.size()));
            attempts++;
        } while (allMessages.size() > 1 && message.equals(lastMessage) && attempts < 10);
        lastMessage = message;

        // случайный игрок
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        Player randomPlayer = null;
        if (!onlinePlayers.isEmpty()) {
            int idx = random.nextInt(onlinePlayers.size());
            randomPlayer = new ArrayList<>(onlinePlayers).get(idx);
        }

        String playerName = randomPlayer != null ? randomPlayer.getName() : "Игрок";
        message = message.replace("%player_name%", playerName);

        // ссылки
        message = config.replaceLinksMsg(message);

        Component component = miniMessage.deserialize(message);

        // отправка всем
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
