package ru.terracraft.welcome.core.group;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import ru.terracraft.welcome.WelcomePlugin;

import java.io.File;
import java.util.*;

/**
 * Менеджер групп с наследованием.
 *
 * groups.yml — id + priority + inherits.
 * Тексты — в lang/<locale>.yml, секция "groups.<id>.join/quit".
 *
 * Наследование: группа может использовать сообщения младших групп.
 * Пример: legend.inherits = [admin, moderator, vip, default]
 *
 * Выбор игрока хранится в players.yml (UUID.group).
 */
public class GroupManager {

    private static final String GROUPS_FILE = "groups.yml";
    private static final String PLAYERS_FILE = "players.yml";
    private static final String PERMISSION_PREFIX = "welcome.group.";

    private final WelcomePlugin plugin;
    private final File groupsFile;
    private final File playersFile;
    private YamlConfiguration groupsYaml;
    private YamlConfiguration playersYaml;

    private final List<Group> cachedGroups = new ArrayList<>();

    public GroupManager(WelcomePlugin plugin) {
        this.plugin = plugin;
        this.groupsFile = new File(plugin.getDataFolder(), GROUPS_FILE);
        this.playersFile = new File(plugin.getDataFolder(), PLAYERS_FILE);
        load();
    }

    // ═══════════════ LOAD / SAVE ═══════════════

    public void load() {
        // groups.yml
        if (!groupsFile.exists()) {
            this.groupsYaml = new YamlConfiguration();
            createDefaultGroups();
            saveGroups();
        } else {
            this.groupsYaml = YamlConfiguration.loadConfiguration(groupsFile);
        }

        // players.yml
        if (!playersFile.exists()) {
            this.playersYaml = new YamlConfiguration();
            savePlayers();
        } else {
            this.playersYaml = YamlConfiguration.loadConfiguration(playersFile);
        }

        rebuildCache();
    }

    public void saveGroups() {
        try {
            groupsYaml.save(groupsFile);
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка сохранения " + GROUPS_FILE + ": " + e.getMessage());
        }
    }

    public void savePlayers() {
        try {
            playersYaml.save(playersFile);
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка сохранения " + PLAYERS_FILE + ": " + e.getMessage());
        }
    }

    private void createDefaultGroups() {
        // ─── default ───
        groupsYaml.set("groups.default.priority", 0);
        groupsYaml.set("groups.default.inherits", List.of());

        // ─── vip ───
        groupsYaml.set("groups.vip.priority", 50);
        groupsYaml.set("groups.vip.inherits", List.of("default"));

        // ─── moderator ───
        groupsYaml.set("groups.moderator.priority", 75);
        groupsYaml.set("groups.moderator.inherits", List.of("default"));

        // ─── admin ───
        groupsYaml.set("groups.admin.priority", 100);
        groupsYaml.set("groups.admin.inherits", List.of("moderator", "vip", "default"));

        // ─── legend ───
        groupsYaml.set("groups.legend.priority", 120);
        groupsYaml.set("groups.legend.inherits", List.of("admin", "moderator", "vip", "default"));
    }

    private void rebuildCache() {
        cachedGroups.clear();

        ConfigurationSection section = groupsYaml.getConfigurationSection("groups");
        if (section == null) {
            plugin.getLogger().warning("В " + GROUPS_FILE + " нет секции groups");
            return;
        }

        for (String id : section.getKeys(false)) {
            String path = "groups." + id;
            int priority = groupsYaml.getInt(path + ".priority", 0);
            List<String> inherits = groupsYaml.getStringList(path + ".inherits");

            String permission = id.equalsIgnoreCase("default")
                    ? null
                    : PERMISSION_PREFIX + id.toLowerCase();

            cachedGroups.add(new Group(id, permission, priority, inherits));
        }

        cachedGroups.sort((a, b) -> Integer.compare(b.priority, a.priority));

        plugin.getLogger().info("Загружено групп: " + cachedGroups.size());
    }

    // ═══════════════ PUBLIC API ═══════════════

    /**
     * Все группы.
     */
    public List<Group> getAllGroups() {
        return List.copyOf(cachedGroups);
    }

    /**
     * Группы, доступные игроку (по правам).
     */
    public List<Group> getAvailableGroups(Player player) {
        if (player == null) return List.of();

        List<Group> available = new ArrayList<>();
        for (Group group : cachedGroups) {
            if (group.permission == null || player.hasPermission(group.permission)) {
                available.add(group);
            }
        }
        return available;
    }

    /**
     * Активная группа игрока.
     * 1. Из players.yml (если право есть).
     * 2. Иначе — с наивысшим priority.
     */
    public Group getSelectedGroup(Player player) {
        if (player == null) return null;

        String chosenId = playersYaml.getString(player.getUniqueId() + ".group");
        if (chosenId != null) {
            Group chosen = getGroup(chosenId);
            if (chosen != null) {
                if (chosen.permission == null || player.hasPermission(chosen.permission)) {
                    return chosen;
                }
            }
        }

        return resolveByPermission(player);
    }

    /**
     * Установить активную группу.
     */
    public boolean setSelectedGroup(Player player, String groupId) {
        if (player == null || groupId == null) return false;

        Group group = getGroup(groupId);
        if (group == null) return false;

        if (group.permission != null && !player.hasPermission(group.permission)) {
            return false;
        }

        playersYaml.set(player.getUniqueId() + ".group", groupId.toLowerCase());
        savePlayers();
        return true;
    }

    /**
     * Сбросить выбор.
     */
    public void resetSelectedGroup(Player player) {
        if (player == null) return;
        playersYaml.set(player.getUniqueId() + ".group", null);
        savePlayers();
    }

    public boolean hasCustomChoice(Player player) {
        if (player == null) return false;
        return playersYaml.getString(player.getUniqueId() + ".group") != null;
    }

    // ═══════════════ GROUP CHAIN (с наследованием) ═══════════════

    /**
     * Список ID групп, к которым принадлежит игрок + все унаследованные.
     * Порядок: сначала сама группа, потом её inherits (рекурсивно).
     */
    public List<String> getGroupChain(Group group) {
        if (group == null) return List.of("default");

        LinkedHashSet<String> chain = new LinkedHashSet<>();
        collectChain(group, chain);

        // Гарантируем, что default всегда в цепочке
        chain.add("default");

        return new ArrayList<>(chain);
    }

    private void collectChain(Group group, LinkedHashSet<String> chain) {
        if (group == null) return;
        if (chain.contains(group.id)) return;   // защита от циклов

        chain.add(group.id);

        for (String parentId : group.inherits) {
            Group parent = getGroup(parentId);
            if (parent != null) {
                collectChain(parent, chain);
            }
        }
    }

    // ═══════════════ GETTERS ═══════════════

    public String getGroupId(Player player) {
        Group group = getSelectedGroup(player);
        return group != null ? group.id : "default";
    }

    public Group getGroup(String id) {
        if (id == null) return null;
        for (Group group : cachedGroups) {
            if (group.id.equalsIgnoreCase(id)) return group;
        }
        return null;
    }

    public Set<String> getGroupIds() {
        ConfigurationSection section = groupsYaml.getConfigurationSection("groups");
        return section != null ? section.getKeys(false) : Set.of();
    }

    // ═══════════════ PRIVATE ═══════════════

    private Group resolveByPermission(Player player) {
        for (Group group : cachedGroups) {
            if (group.permission == null) return group;
            if (player.hasPermission(group.permission)) return group;
        }
        return null;
    }

    // ═══════════════ GROUP DATA ═══════════════

    public static class Group {
        public final String id;
        public final String permission;
        public final int priority;
        public final List<String> inherits;

        public Group(String id, String permission, int priority, List<String> inherits) {
            this.id = id;
            this.permission = permission;
            this.priority = priority;
            this.inherits = inherits != null ? inherits : List.of();
        }

        @Override
        public String toString() {
            return "Group{" + id + ", priority=" + priority + ", inherits=" + inherits + "}";
        }
    }
}