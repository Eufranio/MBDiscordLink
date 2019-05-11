package com.magitechserver.mbdiscordlink;

import com.google.inject.Inject;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.mbdiscordlink.config.ConfigCategory;
import com.magitechserver.mbdiscordlink.config.ConfigManager;
import com.magitechserver.mbdiscordlink.config.UserCategory;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "mbdiscordlink",
        name = "MBDiscordLink-API7",
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
        if (config.link_players) {
            LinkCommand.registerCommands();
        }
        Task.builder()
                .execute(new RoleSyncTask())
                .interval(config.role_sync_interval > 15 ? config.role_sync_interval : 15, TimeUnit.SECONDS)
                .name("MBDiscordLink Role Sync Task")
                .submit(instance);

        MagiBridge.getInstance().getJDA().addEventListener();
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

    public static void reload() {
        users = manager.reloadUsers();
    }

    public static class LinkListener extends ListenerAdapter {
        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            if (!event.isFromType(ChannelType.PRIVATE)) return;
            String code = event.getMessage().getContentStripped();
            if (MBDiscordLink.pendingUsers.containsKey(code)) {
                UUID uuid = UUID.fromString(MBDiscordLink.pendingUsers.get(code));
                if (Sponge.getServer().getPlayer(uuid).isPresent()) {
                    Player player = Sponge.getServer().getPlayer(uuid).get();

                    // Linked successfully
                    if (MBDiscordLink.addUser(uuid, event.getAuthor().getId())) {
                        event.getMessage().getChannel()
                                .sendMessage(MBDiscordLink.config.messages.discord_linked_successfully.replace("%player%", player.getName()));
                        player.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(MBDiscordLink.config.messages.linked_successfully.replace("%user%", event.getAuthor().getName())));
                        MagiBridge.getInstance().getJDA().getGuilds().stream()
                                .filter(g -> g.getMember(event.getAuthor()) != null && g.getMembers().contains(g.getMember(event.getAuthor())))
                                .forEach(g -> g.getController().addRolesToMember(g.getMember(event.getAuthor()), g.getRolesByName(MBDiscordLink.config.linked_users_role, true)).queue());
                    } else {
                        player.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(MBDiscordLink.config.messages.already_linked));
                        return;
                    }

                    // Run link commands
                    for (String command : MBDiscordLink.config.commands.link) {
                        Task.builder()
                                .execute(() -> Sponge.getCommandManager().process(Sponge.getServer().getConsole(), command.replace("%player%", player.getName())))
                                .submit(MBDiscordLink.instance);
                    }
                }
            }
        }
    }
}
