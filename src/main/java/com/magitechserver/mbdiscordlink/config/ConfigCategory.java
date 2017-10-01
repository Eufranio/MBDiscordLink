package com.magitechserver.mbdiscordlink.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Frani on 28/09/2017.
 */
@ConfigSerializable
public class ConfigCategory {

    @Setting(value = "link-players", comment = "Should the plugin link players to their Discord account?")
    public boolean link_players = true;

    @Setting(value = "linked-users-role", comment = "Discord role given to linked users")
    public String linked_users_role = "linked";

    @Setting(value = "commands")
    public CommandsCategory commands = new CommandsCategory();

    @ConfigSerializable
    public static class CommandsCategory {

        @Setting(value = "link-commands", comment = "Commands that should be run once a player linked his account" +
                "Supports %player%")
        public List<String> link = new ArrayList<>(Arrays.asList("give %player% stone 1"));

        @Setting(value = "unlink-commands", comment = "Commands that should be run once a player unlinked his account" +
                "Supports %player%")
        public List<String> unlink = new ArrayList<>(Arrays.asList("We'll miss you, %player%!"));

    }

    @Setting(value = "messages")
    public Messages messages = new Messages();

    @ConfigSerializable
    public static class Messages {

        @Setting(value = "link-code-message", comment = "Message sent to the player when he executes the link command" +
                "Supports %botname% and %code% (the random confirmation code)")
        public String link_code_message = "&aSend a message to &6%botname%&a containing the code &b%code%&a to complete your link!";

        @Setting(value = "linked-successfully", comment = "Message sent to the player once he linked his account" +
                "Supports %user%")
        public String linked_successfully = "&aSuccessfully linked with %user%!";

        @Setting(value = "linked-successfully-discord", comment = "Message sent to the Discord user unce he linked his account" +
                "Supports %player%")
        public String discord_linked_successfully = "Successfully linked with %player%!";

        @Setting(value = "unlink-message", comment = "Message sent to the player when he unlinks his account" +
                "Supports %player%")
        public String unlink = "&aYou just unlinked from your discord account!";

        @Setting(value = "not_linked", comment = "Message sent to the user if his account isn't linked")
        public String not_linked = "&cYou have no linked acccounts!";

        @Setting(value = "already-linked", comment = "Message sent to the user if his account is already linked")
        public String already_linked = "&cYou're already linked! Use /unlink if you wish to unlink your account!";

    }

}
