package ru.net.terracraft.welcome.command;

import ru.net.terracraft.welcome.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class GuiCommand {

    private final Main plugin;

    public GuiCommand(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27,
                Component.text("⚙ Настройки Welcome", NamedTextColor.GOLD));

        // Авто-сообщения
        gui.setItem(10, createToggleItem(
                Material.PAPER, "Авто-сообщения",
                plugin.getConfigHandler().getBoolean("auto_messages.enabled", true)
        ));

        // Звуки
        gui.setItem(11, createToggleItem(
                Material.NOTE_BLOCK, "Звуки",
                plugin.getConfigHandler().getBoolean("settings.sound.enable_sound", true)
        ));

        // Частицы
        gui.setItem(12, createToggleItem(
                Material.FIREWORK_ROCKET, "Частицы",
                plugin.getConfigHandler().getBoolean("settings.particle_effect.enable_particles", true)
        ));

        // Праздники
        gui.setItem(13, createToggleItem(
                Material.JACK_O_LANTERN, "Праздники",
                plugin.getConfigHandler().getBoolean("settings.holidays.enabled", false)
        ));

        // Босс-трекер
        gui.setItem(14, createToggleItem(
                Material.DRAGON_HEAD, "Босс-трекер",
                plugin.getConfigHandler().getBoolean("settings.boss_fight.enabled", true)
        ));

        // Язык
        gui.setItem(16, createLanguageItem());

        // Информация
        gui.setItem(22, createInfoItem());

        player.openInventory(gui);
    }

    private ItemStack createToggleItem(Material material, String name, boolean enabled) {
        ItemStack item = new ItemStack(enabled ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text((enabled ? "✔ " : "✘ ") + name,
                enabled ? NamedTextColor.GREEN : NamedTextColor.RED));
        meta.lore(List.of(
                Component.text("Нажмите для переключения", NamedTextColor.GRAY)
        ));
        item.setItemMeta(meta);
        item.setType(material); // ставим правильный тип
        return item;
    }

    private ItemStack createLanguageItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("🌍 Язык: " +
                plugin.getLanguageManager().getCurrentLanguage(), NamedTextColor.AQUA));
        meta.lore(List.of(
                Component.text("Нажмите для смены языка", NamedTextColor.GRAY)
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createInfoItem() {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("ℹ Информация", NamedTextColor.YELLOW));
        meta.lore(List.of(
                Component.text("Версия: " + plugin.getDescription().getVersion(), NamedTextColor.WHITE),
                Component.text("Автор: luci", NamedTextColor.WHITE),
                Component.text("PlaceholderAPI: " +
                                (plugin.getHookManager().isPapiEnabled() ? "✔" : "✘"),
                        plugin.getHookManager().isPapiEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED)
        ));
        item.setItemMeta(meta);
        return item;
    }
}