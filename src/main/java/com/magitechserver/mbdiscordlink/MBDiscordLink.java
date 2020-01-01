package com.magitechserver.mbdiscordlink;

import com.google.inject.Inject;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.mbdiscordlink.config.Config;
import com.magitechserver.mbdiscordlink.config.MainConfig;
import com.magitechserver.mbdiscordlink.managers.LinkManager;
import com.magitechserver.mbdiscordlink.storage.LinkInfo;
import com.magitechserver.mbdiscordlink.storage.Persistable;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    @Inject
    public Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private File configDir;

    public Config<MainConfig> configManager;
    public LinkManager linkManager;
    public Persistable<LinkInfo, UUID> links;

    @Listener
    public void onServerStart(GameStartingServerEvent event) {
        this.configManager = new Config<>(MainConfig.class, "MBDiscordLink.conf", configDir);
        this.links = Persistable.create(LinkInfo.class, configManager.get().databaseUrl);
        this.linkManager = new LinkManager(this);

        if (configManager.get().link_players) {
            CommandSpec link = CommandSpec.builder()
                    .permission("magibridge.link")
                    .executor((src, args) -> {
                        if (!(src instanceof Player))
                            throw new CommandException(Text.of("Only players can run this command!"));

                        linkManager.openInvite((Player) src);
                        return CommandResult.success();
                    })
                    .build();
            Sponge.getCommandManager().register(this, link, "link");

            CommandSpec unlink = CommandSpec.builder()
                    .permission("magibridge.unlink")
                    .executor((src, args) -> {
                        if (!(src instanceof Player))
                            throw new CommandException(Text.of("Only players can run this command!"));

                        linkManager.unlink((Player) src);
                        return CommandResult.success();
                    })
                    .build();
            Sponge.getCommandManager().register(this, unlink, "unlink");
        }

        Task.builder()
                .execute(() -> Sponge.getServer().getOnlinePlayers().forEach(linkManager::syncRoles))
                .interval(Math.max(15, configManager.get().role_sync_interval), TimeUnit.SECONDS)
                .name("MBDiscordLink Role Sync Task")
                .submit(this);

        MagiBridge.getInstance().getJDA().addEventListener(new ListenerAdapter() {
            @Override
            public void onPrivateMessageReceived(@Nonnull PrivateMessageReceivedEvent event) {
                String code = event.getMessage().getContentStripped();
                linkManager.completeLink(code, event.getAuthor(), event.getChannel());
            }
        });
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        this.configManager.reload();
    }
}
