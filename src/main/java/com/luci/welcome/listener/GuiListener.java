package com.luci.welcome.listener;

import com.luci.welcome.Main;
import com.luci.welcome.command.GuiCommand;
import com.luci.welcome.utils.LoggerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.slf4j.LoggerFactory;

public class GuiListener implements Listener {

    private final Main plugin;
    private final LoggerUtil logger;
    private static final String GUI_TITLE = "Настройки Welcome";

    public GuiListener(Main plugin) {
        this.plugin = plugin;
        this.logger = new LoggerUtil(LoggerFactory.getLogger("Welcome-GUI"));
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Получаем заголовок через InventoryView
        InventoryView view = event.getView();
        Component title = view.title();

        // Проверяем, что это наш GUI
        String titleText = PlainTextComponentSerializer.plainText().serialize(title);
        if (!titleText.contains(GUI_TITLE)) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getSlot();

        switch (slot) {
            case 10: // Авто-сообщения
                toggleSetting(player, "auto_messages.enabled", "Авто-сообщения");
                break;
            case 11: // Звуки
                toggleSetting(player, "settings.sound.enable_sound", "Звуки");
                break;
            case 12: // Частицы
                toggleSetting(player, "settings.particle_effect.enable_particles", "Частицы");
                break;
            case 13: // Праздники
                toggleSetting(player, "settings.holidays.enabled", "Праздники");
                break;
            case 14: // Босс-трекер
                toggleSetting(player, "settings.boss_fight.enabled", "Босс-трекер");
                break;
            case 16: // Язык
                toggleLanguage(player);
                break;
            case 22: // Информация
                player.sendMessage(Component.text("ℹ Welcome Plugin v" +
                        plugin.getDescription().getVersion() + " by luci", NamedTextColor.YELLOW));
                break;
        }
    }

    private void toggleSetting(Player player, String path, String name) {
        boolean current = plugin.getConfigHandler().getBoolean(path,
                path.contains("holidays") ? false : true);
        boolean newValue = !current;

        plugin.getConfig().set(path, newValue);
        plugin.saveConfig();
        plugin.getConfigHandler().reloadConfig();

        player.sendMessage(Component.text(
                (newValue ? "✔ " : "✘ ") + name + ": " + (newValue ? "включено" : "выключено"),
                newValue ? NamedTextColor.GREEN : NamedTextColor.RED
        ));

        // Переоткрываем GUI с задержкой в 1 тик
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            player.closeInventory();
            new GuiCommand(plugin).open(player);
        });
    }

    private void toggleLanguage(Player player) {
        String currentLang = plugin.getLanguageManager().getCurrentLanguage();
        String newLang = currentLang.equals("ru") ? "en" : "ru";

        plugin.getLanguageManager().setLanguage(newLang);

        player.sendMessage(Component.text(
                "🌍 Язык изменён на: " + newLang.toUpperCase(), NamedTextColor.AQUA
        ));

        // Переоткрываем GUI с задержкой в 1 тик
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            player.closeInventory();
            new GuiCommand(plugin).open(player);
        });
    }
}