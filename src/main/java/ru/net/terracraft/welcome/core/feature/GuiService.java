package ru.net.terracraft.welcome.core.feature;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.net.terracraft.welcome.WelcomePlugin;
import ru.net.terracraft.welcome.config.ConfigLoader;
import ru.net.terracraft.welcome.config.MainConfig;
import ru.net.terracraft.welcome.core.i18n.LanguageManager;
import ru.net.terracraft.welcome.core.util.Utils;

import java.util.List;

/**
 * Сервис GUI: отрисовка + обработка кликов + открытие/переоткрытие.
 * Всё в одном классе.
 */
public class GuiService {

    // ═══════════════ CONSTANTS ═══════════════

    public static final String GUI_TITLE = "Настройки Welcome";
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
        player.openInventory(build());
    }

    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!isOurGui(event)) return;

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

    private Inventory build() {
        Inventory gui = Bukkit.createInventory(
                null,
                GUI_SIZE,
                Component.text("⚙ " + GUI_TITLE, NamedTextColor.GOLD)
        );

        gui.setItem(SLOT_SOUND, makeToggle(
                config.effects.soundEnabled, "Звуки", Material.NOTE_BLOCK));

        gui.setItem(SLOT_PARTICLES, makeToggle(
                config.effects.particlesEnabled, "Частицы", Material.FIREWORK_ROCKET));

        gui.setItem(SLOT_HOLIDAYS, makeToggle(
                config.holidays.enabled, "Праздники", Material.JACK_O_LANTERN));

        gui.setItem(SLOT_LANGUAGE, makeLanguageItem());
        gui.setItem(SLOT_INFO, makeInfoItem());

        return gui;
    }

    private ItemStack makeToggle(boolean enabled, String name, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        Component displayName = Component.text()
                .append(Component.text(enabled ? "✔ " : "✘ ",
                        enabled ? NamedTextColor.GREEN : NamedTextColor.RED))
                .append(Component.text(name,
                        enabled ? NamedTextColor.GREEN : NamedTextColor.RED))
                .build();

        meta.displayName(displayName);
        meta.lore(List.of(
                Component.text("Нажмите для переключения", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeLanguageItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text()
                .append(Component.text("🌍 Язык: ", NamedTextColor.AQUA))
                .append(Component.text(lang.getCurrentLanguage().toUpperCase(), NamedTextColor.WHITE))
                .build()
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(List.of(
                Component.text("Нажмите для смены языка", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Доступные: " + String.join(", ", lang.getAvailableLanguages()),
                                NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeInfoItem() {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("ℹ Информация", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));

        boolean papi = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
        boolean vault = plugin.getServer().getPluginManager().getPlugin("Vault") != null;

        meta.lore(List.of(
                Component.text("Версия: " + plugin.getPluginMeta().getVersion(), NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Автор: luci", NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("PlaceholderAPI: " + (papi ? "✔" : "✘"),
                                papi ? NamedTextColor.GREEN : NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Vault: " + (vault ? "✔" : "✘"),
                                vault ? NamedTextColor.GREEN : NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false)
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
        notifyToggle(player, "Звуки", config.effects.soundEnabled);
    }

    private void toggleParticles(Player player) {
        config.effects.particlesEnabled = !config.effects.particlesEnabled;
        configLoader.save();
        notifyToggle(player, "Частицы", config.effects.particlesEnabled);
    }

    private void toggleHolidays(Player player) {
        config.holidays.enabled = !config.holidays.enabled;
        configLoader.save();
        notifyToggle(player, "Праздники", config.holidays.enabled);
    }

    private void notifyToggle(Player player, String name, boolean current) {
        player.sendMessage(Component.text()
                .append(Component.text(current ? "✔ " : "✘ ",
                        current ? NamedTextColor.GREEN : NamedTextColor.RED))
                .append(Component.text(name + ": ", NamedTextColor.WHITE))
                .append(Component.text(current ? "включено" : "выключено",
                        current ? NamedTextColor.GREEN : NamedTextColor.RED))
                .build());
    }

    private void switchLanguage(Player player) {
        var available = lang.getAvailableLanguages();
        if (available.size() < 2) {
            player.sendMessage(Component.text("ℹ Доступен только один язык", NamedTextColor.YELLOW));
            return;
        }

        String current = lang.getCurrentLanguage();
        String next = available.stream()
                .filter(code -> !code.equals(current))
                .findFirst()
                .orElse(current);

        lang.setLanguage(next);
        config.language = next;
        configLoader.save();

        player.sendMessage(Component.text()
                .append(Component.text("🌍 Язык изменён на: ", NamedTextColor.AQUA))
                .append(Component.text(next.toUpperCase(), NamedTextColor.WHITE))
                .build());
    }

    private void sendInfoMessage(Player player) {
        player.sendMessage(Component.text("ℹ Welcome Plugin v"
                + plugin.getPluginMeta().getVersion() + " by luci", NamedTextColor.YELLOW));
    }

    // ═══════════════ HELPERS ═══════════════

    private boolean isOurGui(InventoryClickEvent event) {
        String title = PlainTextComponentSerializer.plainText()
                .serialize(event.getView().title());
        return title.contains(GUI_TITLE);
    }
}