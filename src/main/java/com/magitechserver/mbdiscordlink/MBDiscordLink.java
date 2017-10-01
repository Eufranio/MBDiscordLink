package com.magitechserver.mbdiscordlink;

import com.google.inject.Inject;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.api.DiscordEvent;
import com.magitechserver.magibridge.api.MagiBridgeAPI;
import com.magitechserver.mbdiscordlink.config.ConfigCategory;
import com.magitechserver.mbdiscordlink.config.ConfigManager;
import com.magitechserver.mbdiscordlink.config.UserCategory;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.plugin.Dependency;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Plugin(
        id = "mbdiscordlink",
        name = "MBDiscordLink",
        description = "Addon for MagiBridge that links in-game accounts with Discord accounts",
        authors = {
                "Eufranio"
        },
        dependencies = {
                @Dependency(id = "magibridge")
        }
)
public class MBDiscordLink {

    public static Logger logger;

    @Inject
    public MBDiscordLink(Logger logger) {
        this.logger = logger;
    }

    @Inject
    public GuiceObjectMapperFactory factory;

    public static MagiBridgeAPI API;
    public static ConfigCategory config;
    public static ConfigManager manager;
    public static UserCategory users;
    public static MBDiscordLink instance;
    public static Map<String, String> pendingUsers; // CODE <> UUID


    @Listener
    public void onServerStart(GameStartingServerEvent event) {
        pendingUsers = new HashMap<>();
        instance = this;
        manager = new ConfigManager(instance, MagiBridge.getInstance().configDir);
        config = manager.reloadConfig();
        users = manager.loadUsers();
        API = new MagiBridgeAPI();
        if (config.link_players) {
            LinkCommand.registerCommands();
        }
        Sponge.getEventManager().registerListeners(instance, new Listeners());
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        config = null;
        users = null;
        config = manager.reloadConfig();
        users = manager.reloadUsers();
    }

    public static boolean addUser(UUID uuid, String id) {
        if (users.users.containsKey(uuid.toString())) {
            return false;
        }
        users.users.put(uuid.toString(), id);
        manager.reloadUsers();
        return true;
    }

    public static boolean removeUser(UUID uuid) {
        if (!users.users.containsKey(uuid.toString())) {
            return false;
        }
        users.users.remove(uuid.toString());
        manager.reloadUsers();
        return true;
    }
}
