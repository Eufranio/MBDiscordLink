package com.magitechserver.mbdiscordlink.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.*;

/**
 * Created by Frani on 28/09/2017.
 */
@ConfigSerializable
public class MainConfig {

    public MainConfig() {
        roles_to_sync.put("some.perm", "19281928192819");
    }

    @Setting(comment = "The URL of the database that MBDiscordLink should store links")
    public String databaseUrl = "jdbc:sqlite:MBDiscordLink.db";

    @Setting(value = "link-players", comment = "Should the plugin link players to their Discord account?")
    public boolean link_players = true;

    @Setting(value = "linked-users-role", comment = "Discord role given to linked users")
    public String linked_users_role = "linked";

    @Setting(comment = "Should the plugin sync the Discord nicknames of linked users with their in-game names?")
    public boolean syncNicknames = true;

    @Setting(comment = "If this is enabled, the plugin will sync in the inverse direction: has discord role -> get in-game group. If " +
            "enabled, the format of roles-to-sync SHOULD be group-name=role-id, and addGroupCommand and removeGroupCommand must be set! \"default\" can't be used.")
    public boolean inverseSync = false;

    @Setting(comment = "If inverse sync is enabled, the plugin will run this command to add a player to a in-game group")
    public String addGroupCommand = "lp user %player% parent add %group%";

    @Setting(comment = "If inverse sync is enabled, the plugin will run this command to remove a player from a in-game group")
    public String removeGroupCommand = "lp user %player% parent remove %group%";

    @Setting(value = "commands")
    public CommandsCategory commands = new CommandsCategory();

    @ConfigSerializable
    public static class CommandsCategory {

        @Setting(value = "link-commands", comment = "Commands that should be run once a player linked his account" +
                "Supports %player%, %user%")
        public List<String> link = new ArrayList<>(Arrays.asList("give %player% stone 1"));

        @Setting(value = "unlink-commands", comment = "Commands that should be run once a player unlinked his account" +
                "Supports %player%, %user%")
        public List<String> unlink = new ArrayList<>(Arrays.asList("say We'll miss you, %player%!"));

    }

    @Setting(value = "permission-check-prefix", comment = "The prefix of the permission when checking groups of roles-to-sync, if inverse sync is enabled.\n" +
            "This prefix will be used as prefix + groupname to check the permission. Leave empty if using a permission node instead\n" +
            "the group name on roles-to-sync.")
    public String permission_check_prefix = "group.";

    @Setting(value = "roles-to-sync", comment = "A key-value list with roles that should sync with in-game groups\n" +
            "The format is \"<in-game permission node>\"=\"discord-role-id\"")
    public Map<String, String> roles_to_sync = new HashMap<>();

    @Setting(value = "role-sync-interval", comment = "Interval between every role sync check, in seconds\n" +
            "Set to 0 to disable. The minimum value is 15 seconds")
    public int role_sync_interval = 15;

    @Setting(value = "messages")
    public Messages messages = new Messages();

    @ConfigSerializable
    public static class Messages {

        @Setting(value = "link-code-message", comment = "Message sent to the player when he executes the link command" +
                "Supports %botname% and %code% (the random confirmation code)")
        public String link_code_message = "&aSend a message to &6%botname%&a containing the code &b%code%&a to complete your link!";

        @Setting(value = "linked-successfully", comment = "Message sent to the player once he linked his account" +
                "Supports %user%, %player%")
        public String linked_successfully = "&aSuccessfully linked with %user%!";

        @Setting(value = "linked-successfully-discord", comment = "Message sent to the Discord user unce he linked his account" +
                "Supports %player%")
        public String discord_linked_successfully = "Successfully linked with %player%!";

        @Setting(value = "unlink-message", comment = "Message sent to the player when he unlinks his account" +
                "Supports %player%, %user%")
        public String unlink = "&aYou just unlinked from your discord account!";

        @Setting(value = "not_linked", comment = "Message sent to the user if his account isn't linked")
        public String not_linked = "&cYou have no linked acccounts!";

        @Setting(value = "already-linked", comment = "Message sent to the Discord user if his account is already linked")
        public String already_linked = "You're already linked! Use /unlink in-game if you wish to unlink your account!";

        @Setting(value = "whois-player", comment = "Message sent to the player, in-game, when using /whois <other player>")
        public String whois_player = "&a%player%&7's Discord user: &b%user%";

        @Setting(value = "whois-user", comment = "Message sent to the user, in Discord, when using !whois @User")
        public String whois_user = "%user%'s Minecraft nickname: %player%";

        @Setting(value = "whois-not-linked", comment = "Message sent to the user/player when the target user/player is not linked")
        public String whois_not_linked = "This user/player is not linked!";

    }

}
