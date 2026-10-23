package ru.terracraft.welcome.command;

import dev.jorel.commandapi.CommandAPICommand;
import ru.terracraft.welcome.WelcomePlugin;
import ru.terracraft.welcome.core.lang.LanguageManager;
import ru.terracraft.welcome.core.lang.MessageKey;

import java.util.List;

/**
 * /welcome info — информация о плагине.
 * Все строки — из lang/*.lang (info.*).
 * Плейсхолдеры: %version%, %papi%, %vault%, %luckperms%.
 */
public class InfoCommand {

    private final WelcomePlugin plugin;

    public InfoCommand(WelcomePlugin plugin) {
        this.plugin = plugin;
    }

    public CommandAPICommand build() {
        return new CommandAPICommand("info")
                .executes((sender, args) -> {
                    boolean papi = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
                    boolean vault = plugin.getServer().getPluginManager().getPlugin("Vault") != null;
                    boolean luckPerms = plugin.getServer().getPluginManager().getPlugin("LuckPerms") != null;

                    String version = plugin.getPluginMeta().getVersion();

                    LanguageManager lang = plugin.getServiceRegistry().get(LanguageManager.class);

                    // Заголовок
                    sendList(sender, lang, MessageKey.INFO_HEADER, version, papi, vault, luckPerms);

                    // Тело
                    sendList(sender, lang, MessageKey.INFO_BODY, version, papi, vault, luckPerms);

                    // Подвал
                    sendList(sender, lang, MessageKey.INFO_FOOTER, version, papi, vault, luckPerms);
                });
    }

    private void sendList(net.kyori.adventure.audience.Audience audience,
                          LanguageManager lang,
                          MessageKey key,
                          String version,
                          boolean papi,
                          boolean vault,
                          boolean luckPerms) {

        List<String> lines = lang.getMessageList(key);

        for (String line : lines) {
            if (line == null || line.isEmpty()) continue;

            String prepared = line
                    .replace("%version%", version)
                    .replace("%papi%", statusIcon(papi))
                    .replace("%vault%", statusIcon(vault))
                    .replace("%luckperms%", statusIcon(luckPerms));

            audience.sendMessage(lang.render(prepared));
        }
    }

    /**
     * Возвращает MiniMessage-строку со значком статуса.
     */
    private String statusIcon(boolean enabled) {
        return enabled ? "<green>✔</green>" : "<red>✘</red>";
    }
}