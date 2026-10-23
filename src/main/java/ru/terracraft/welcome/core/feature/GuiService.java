package ru.terracraft.welcome.core.feature;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.terracraft.welcome.WelcomePlugin;
import ru.terracraft.welcome.config.ConfigLoader;
import ru.terracraft.welcome.config.MainConfig;
import ru.terracraft.welcome.core.lang.LanguageManager;
import ru.terracraft.welcome.core.lang.MessageKey;
import ru.terracraft.welcome.core.util.Utils;

import java.util.List;

/**
 * Сервис GUI: отрисовка + обработка кликов + открытие/переоткрытие.
 * Все тексты — из lang/*.lang (gui.*).
 */
public class GuiService {

    // ═══════════════ CONSTANTS ═══════════════

    public static final int GUI_SIZE = 27;

    // Слоты кнопок
    public static final int SLOT_SOUND     = 10;
    public static final int SLOT_PARTICLES = 11;
    public static final int SLOT_HOLIDAYS  = 12;
    public static final int SLOT_LANGUAGE  = 14;
    public static final int SLOT_INFO      = 22;

    // ═══════════════ FIELDS ═══════════════

    private final WelcomePlugin plugin;
    private final MainConfig config;
    private final ConfigLoader configLoader;
    private final LanguageManager lang;

    public GuiService(WelcomePlugin plugin, MainConfig config,
                      ConfigLoader configLoader, LanguageManager lang) {
        this.plugin = plugin;
        this.config = config;
        this.configLoader = configLoader;
        this.lang = lang;
    }

    // ═══════════════ PUBLIC API ═══════════════

    public void open(Player player) {
        if (player == null || !player.isOnline()) return;
        player.openInventory(build(player));
    }

    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Проверяем через InventoryHolder, а не по title
        if (!(event.getInventory().getHolder() instanceof WelcomeGuiHolder)) return;

        event.setCancelled(true);

        boolean shouldReopen = dispatchClick(player, event);

        if (shouldReopen) {
            Utils.syncLater(plugin, () -> {
                if (player.isOnline()) {
                    player.closeInventory();
                    open(player);
                }
            }, 1L);
        }
    }

    // ═══════════════ RENDER ═══════════════

    private Inventory build(Player player) {
        WelcomeGuiHolder holder = new WelcomeGuiHolder();

        String titleRaw = lang.getMessage(player, MessageKey.GUI_TITLE, "Welcome");
        Inventory gui = Bukkit.createInventory(
                holder,
                GUI_SIZE,
                lang.render(titleRaw, player)
        );
        holder.setInventory(gui);

        gui.setItem(SLOT_SOUND, makeToggle(player,
                config.effects.soundEnabled, MessageKey.GUI_SOUND_NAME, Material.NOTE_BLOCK));

        gui.setItem(SLOT_PARTICLES, makeToggle(player,
                config.effects.particlesEnabled, MessageKey.GUI_PARTICLES_NAME, Material.FIREWORK_ROCKET));

        gui.setItem(SLOT_HOLIDAYS, makeToggle(player,
                config.holidays.enabled, MessageKey.GUI_HOLIDAYS_NAME, Material.JACK_O_LANTERN));

        gui.setItem(SLOT_LANGUAGE, makeLanguageItem(player));
        gui.setItem(SLOT_INFO, makeInfoItem(player));

        return gui;
    }

    private ItemStack makeToggle(Player player, boolean enabled, MessageKey nameKey, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String nameRaw = lang.getMessage(player, nameKey, nameKey.path());
        String color = enabled ? "<green>" : "<red>";
        String icon = enabled ? "✔ " : "✘ ";

        String displayRaw = color + icon + nameRaw + "</" + (enabled ? "green" : "red") + ">";

        meta.displayName(lang.render(displayRaw, player).decoration(
                net.kyori.adventure.text.format.TextDecoration.ITALIC, false));

        String loreRaw = lang.getMessage(player, MessageKey.GUI_TOGGLE_LORE, "Click to toggle");
        meta.lore(List.of(
                lang.render(loreRaw, player).decoration(
                        net.kyori.adventure.text.format.TextDecoration.ITALIC, false)
        ));

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeLanguageItem(Player player) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();

        String nameRaw = lang.getMessage(player, MessageKey.GUI_LANGUAGE_NAME, "Language: %lang%")
                .replace("%lang%", lang.getCurrentLanguage().toUpperCase());

        meta.displayName(lang.render(nameRaw, player).decoration(
                net.kyori.adventure.text.format.TextDecoration.ITALIC, false));

        String clickRaw = lang.getMessage(player, MessageKey.GUI_LANGUAGE_LORE_CLICK, "Click to change");
        String availableRaw = lang.getMessage(player, MessageKey.GUI_LANGUAGE_LORE_AVAILABLE, "Available: %list%")
                .replace("%list%", String.join(", ", lang.getAvailableLanguages()));

        meta.lore(List.of(
                lang.render(clickRaw, player).decoration(
                        net.kyori.adventure.text.format.TextDecoration.ITALIC, false),
                lang.render(availableRaw, player).decoration(
                        net.kyori.adventure.text.format.TextDecoration.ITALIC, false)
        ));

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeInfoItem(Player player) {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();

        String nameRaw = lang.getMessage(player, MessageKey.GUI_INFO_NAME, "Info");
        meta.displayName(lang.render(nameRaw, player).decoration(
                net.kyori.adventure.text.format.TextDecoration.ITALIC, false));

        boolean papi = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
        boolean vault = plugin.getServer().getPluginManager().getPlugin("Vault") != null;

        String version = plugin.getPluginMeta().getVersion();

        String versionRaw = lang.getMessage(player, MessageKey.GUI_INFO_VERSION, "Version: %version%")
                .replace("%version%", version);
        String authorRaw = lang.getMessage(player, MessageKey.GUI_INFO_AUTHOR, "Author: luci");
        String papiRaw = lang.getMessage(player, MessageKey.GUI_INFO_PAPI, "PlaceholderAPI: %status%")
                .replace("%status%", statusIcon(papi));
        String vaultRaw = lang.getMessage(player, MessageKey.GUI_INFO_VAULT, "Vault: %status%")
                .replace("%status%", statusIcon(vault));

        meta.lore(List.of(
                lang.render(versionRaw, player).decoration(
                        net.kyori.adventure.text.format.TextDecoration.ITALIC, false),
                lang.render(authorRaw, player).decoration(
                        net.kyori.adventure.text.format.TextDecoration.ITALIC, false),
                lang.render(papiRaw, player).decoration(
                        net.kyori.adventure.text.format.TextDecoration.ITALIC, false),
                lang.render(vaultRaw, player).decoration(
                        net.kyori.adventure.text.format.TextDecoration.ITALIC, false)
        ));

        item.setItemMeta(meta);
        return item;
    }

    // ═══════════════ CLICK DISPATCH ═══════════════

    private boolean dispatchClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return false;

        int slot = event.getSlot();

        switch (slot) {
            case SLOT_SOUND -> {
                toggleSound(player);
                return true;
            }
            case SLOT_PARTICLES -> {
                toggleParticles(player);
                return true;
            }
            case SLOT_HOLIDAYS -> {
                toggleHolidays(player);
                return true;
            }
            case SLOT_LANGUAGE -> {
                switchLanguage(player);
                return true;
            }
            case SLOT_INFO -> {
                sendInfoMessage(player);
                return false;
            }
            default -> {
                return false;
            }
        }
    }

    // ═══════════════ ACTIONS ═══════════════

    private void toggleSound(Player player) {
        config.effects.soundEnabled = !config.effects.soundEnabled;
        configLoader.save();
        notifyToggle(player, MessageKey.GUI_SOUND_NAME, config.effects.soundEnabled);
    }

    private void toggleParticles(Player player) {
        config.effects.particlesEnabled = !config.effects.particlesEnabled;
        configLoader.save();
        notifyToggle(player, MessageKey.GUI_PARTICLES_NAME, config.effects.particlesEnabled);
    }

    private void toggleHolidays(Player player) {
        config.holidays.enabled = !config.holidays.enabled;
        configLoader.save();
        notifyToggle(player, MessageKey.GUI_HOLIDAYS_NAME, config.holidays.enabled);
    }

    private void notifyToggle(Player player, MessageKey nameKey, boolean current) {
        String nameRaw = lang.getMessage(player, nameKey, nameKey.path());

        String stateRaw = lang.getMessage(player,
                current ? MessageKey.GUI_TOGGLE_STATE_ON : MessageKey.GUI_TOGGLE_STATE_OFF,
                current ? "on" : "off");

        String messageRaw = lang.getMessage(player, MessageKey.GUI_TOGGLE_MESSAGE,
                        "%name%: %state%")
                .replace("%name%", nameRaw)
                .replace("%state%", stateRaw);

        player.sendMessage(lang.render(messageRaw, player));
    }

    private void switchLanguage(Player player) {
        var available = lang.getAvailableLanguages();
        if (available.size() < 2) {
            player.sendMessage(renderKey(player, MessageKey.COMMAND_LANGUAGE_NOT_FOUND));
            return;
        }

        String current = lang.getCurrentLanguage();
        String next = available.stream()
                .filter(code -> !code.equals(current))
                .findFirst()
                .orElse(current);

        if (!lang.hasLanguage(next)) {
            player.sendMessage(renderKey(player, MessageKey.COMMAND_LANGUAGE_NOT_FOUND));
            return;
        }

        lang.setLanguage(next);
        config.language = next;
        configLoader.save();

        player.sendMessage(renderKey(player, MessageKey.COMMAND_LANGUAGE_SUCCESS,
                "%lang%", next.toUpperCase()));
    }

    private void sendInfoMessage(Player player) {
        String raw = lang.getMessage(player, MessageKey.GUI_INFO_MESSAGE,
                        "Welcome Plugin v%version% by luci")
                .replace("%version%", plugin.getPluginMeta().getVersion());

        player.sendMessage(lang.render(raw, player));
    }

    // ═══════════════ HELPERS ═══════════════

    private String statusIcon(boolean enabled) {
        return enabled ? "<green>✔</green>" : "<red>✘</red>";
    }

    /**
     * Рендер строки из lang по ключу с плейсхолдерами.
     */
    private Component renderKey(Player player, MessageKey key, String... replacements) {
        String raw = lang.getMessage(player, key, key.path());
        if (raw == null || raw.isEmpty()) {
            return Component.empty();
        }
        if (replacements != null && replacements.length >= 2) {
            for (int i = 0; i + 1 < replacements.length; i += 2) {
                raw = raw.replace(replacements[i], replacements[i + 1]);
            }
        }
        return lang.render(raw, player);
    }
}