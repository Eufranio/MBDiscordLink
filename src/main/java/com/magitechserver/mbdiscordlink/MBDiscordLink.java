package com.magitechserver.mbdiscordlink;

import com.google.inject.Inject;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.mbdiscordlink.config.MainConfig;
import com.magitechserver.mbdiscordlink.managers.LinkManager;
import com.magitechserver.mbdiscordlink.storage.LinkInfo;
import io.github.eufranio.config.Config;
import io.github.eufranio.storage.Persistable;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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
    private LinkManager linkManager;
    public Persistable<LinkInfo, UUID> links;

    ListenerAdapter listener;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
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

            CommandSpec whoIs = CommandSpec.builder()
                    .permission("magibridge.whois")
                    .arguments(GenericArguments.user(Text.of("user")))
                    .executor((src, args) -> {
                        User user = args.requireOne("user");
                        linkManager.whoIs(src, user);
                        return CommandResult.success();
                    })
                    .build();
            Sponge.getCommandManager().register(this, whoIs, "whois", "discorduser");
        }

        this.hookBridge();
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        this.configManager.reload();
        this.hookBridge();
    }

    void hookBridge() {
        CompletableFuture.runAsync(() -> {
            long started = System.currentTimeMillis();
            while ((System.currentTimeMillis() - started) < 15000 && MagiBridge.getInstance().getJDA() == null) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {} // ignored
            }
        }).thenRun(() -> {
            if (MagiBridge.getInstance().getJDA() == null) {
                logger.error("MagiBridge has not loaded correctly, MBDiscordLink will not work!");
            } else {
                logger.info("MBDiscordLink successfully hooked into MagiBridge!");
                this.registerListener();
                Task.builder()
                        .execute(() -> Sponge.getServer().getOnlinePlayers().forEach(linkManager::syncRoles))
                        .interval(Math.max(15, configManager.get().role_sync_interval), TimeUnit.SECONDS)
                        .name("MBDiscordLink Role Sync Task")
                        .submit(this);
            }
        });
    }

    void registerListener() {
        if (this.listener != null) return;
        listener = new ListenerAdapter() {
            @Override
            public void onPrivateMessageReceived(@Nonnull PrivateMessageReceivedEvent event) {
                String code = event.getMessage().getContentStripped();
                linkManager.completeLink(code, event.getAuthor(), event.getChannel());
            }

            @Override
            public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
                if (!MagiBridge.getInstance().getListeningChannels().contains(event.getChannel().getId()))
                    return;
                String msg = event.getMessage().getContentStripped().trim();
                if (msg.startsWith("!whois ")) {
                    event.getMessage().getMentionedMembers().forEach(m -> linkManager.whoIs(m.getUser(), event.getChannel()));
                }
            }
        };
        MagiBridge.getInstance().getJDA().addEventListener(listener);
    }
}
