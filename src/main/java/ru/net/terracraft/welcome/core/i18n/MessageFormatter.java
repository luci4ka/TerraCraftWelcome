package ru.net.terracraft.welcome.core.i18n;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.net.terracraft.welcome.core.integration.IntegrationManager;
import ru.net.terracraft.welcome.core.util.Utils;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Общий форматтер сообщений.
 * Убирает дублирование: PAPI → плейсхолдеры → MiniMessage.
 */
public class MessageFormatter {

    private final LanguageManager lang;
    private final IntegrationManager integrations;
    private final Random random = new Random();

    public MessageFormatter(LanguageManager lang, IntegrationManager integrations) {
        this.lang = lang;
        this.integrations = integrations;
    }

    // ═══════════════ SINGLE ═══════════════

    /**
     * Форматировать строку для игрока.
     */
    public Component format(String text, Player player) {
        if (text == null || text.isEmpty()) return Component.empty();

        String prepared = text;

        // PAPI
        if (player != null && integrations != null && integrations.isPapiEnabled()
                && integrations.getPlaceholder() != null) {
            prepared = integrations.getPlaceholder().parse(player, prepared);
        }

        // MiniMessage + базовые плейсхолдеры
        return lang.render(prepared, player);
    }

    /**
     * Форматировать строку по ключу.
     */
    public Component format(MessageKey key, Player player) {
        return format(lang.getMessage(key), player);
    }

    /**
     * Форматировать строку по ключу с заменами.
     * Пример: format(MessageKey.DEATH_PVP, victim, "{killer}", "Steve")
     */
    public Component format(MessageKey key, Player player, String... replacements) {
        String raw = lang.getMessage(key);

        if (replacements != null && replacements.length >= 2) {
            for (int i = 0; i + 1 < replacements.length; i += 2) {
                raw = raw.replace(replacements[i], replacements[i + 1]);
            }
        }

        return format(raw, player);
    }

    // ═══════════════ LIST ═══════════════

    public List<Component> formatList(List<String> lines, Player player) {
        if (lines == null || lines.isEmpty()) return List.of();
        return lines.stream()
                .map(line -> format(line, player))
                .collect(Collectors.toList());
    }

    public List<Component> formatList(MessageKey key, Player player) {
        return formatList(lang.getMessageList(key), player);
    }

    /**
     * Случайная строка из списка по ключу.
     */
    public Component formatRandom(MessageKey key, Player player) {
        List<String> list = lang.getMessageList(key);
        if (list.isEmpty()) return Component.empty();

        String chosen = list.get(random.nextInt(list.size()));
        return format(chosen, player);
    }

    // ═══════════════ SEND ═══════════════

    public void send(Player player, MessageKey key) {
        if (player == null || !player.isOnline()) return;
        player.sendMessage(format(key, player));
    }

    public void send(Player player, MessageKey key, String... replacements) {
        if (player == null || !player.isOnline()) return;
        player.sendMessage(format(key, player, replacements));
    }

    public void sendRaw(Player player, String text) {
        if (player == null || !player.isOnline()) return;
        player.sendMessage(format(text, player));
    }

    // ═══════════════ BROADCAST ═══════════════

    public void broadcast(MessageKey key) {
        Component msg = format(key, null);
        Bukkit.broadcast(msg);
    }

    public void broadcast(MessageKey key, String... replacements) {
        Component msg = format(key, null, replacements);
        Bukkit.broadcast(msg);
    }

    // ═══════════════ ACCESS ═══════════════

    public LanguageManager getLanguageManager() {
        return lang;
    }

    public IntegrationManager getIntegrations() {
        return integrations;
    }
}