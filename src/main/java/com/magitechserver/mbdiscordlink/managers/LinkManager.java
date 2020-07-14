package com.magitechserver.mbdiscordlink.managers;

import com.google.common.collect.Maps;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.util.Utils;
import com.magitechserver.mbdiscordlink.MBDiscordLink;
import com.magitechserver.mbdiscordlink.config.MainConfig;
import com.magitechserver.mbdiscordlink.storage.LinkInfo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Frani on 31/12/2019.
 */
public class LinkManager {

    private MBDiscordLink plugin;
    public LinkManager(MBDiscordLink plugin) {
        this.plugin = plugin;
    }

    private Map<String, UUID> codeToPlayer = Maps.newHashMap();

    public void whoIs(User user, TextChannel channel) {
        MainConfig.Messages messages = plugin.configManager.get().messages;
        try {
            List<LinkInfo> links = plugin.links.objDao.queryForEq("discordId", user.getId());
            if (links.isEmpty()) {
                channel.sendMessage(messages.whois_not_linked).queue();
                return;
            }

            LinkInfo info = links.get(0);
            channel.sendMessage(messages.whois_user
                    .replace("%player%", info.getSpongeUser().getName())
                    .replace("%user%", user.getAsTag())).queue();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void whoIs(CommandSource sender, org.spongepowered.api.entity.living.player.User targetUser) {
        MainConfig.Messages messages = plugin.configManager.get().messages;

        LinkInfo link = plugin.links.get(targetUser.getUniqueId());
        if (link == null) {
            sender.sendMessage(Utils.toText(messages.whois_not_linked));
            return;
        }

        Guild guild = MagiBridge.getInstance().getJDA()
                .getTextChannelById(MagiBridge.getInstance().getConfig().CHANNELS.MAIN_CHANNEL)
                .getGuild();

        Member member = guild.getMemberById(link.discordId);
        sender.sendMessage(Utils.toText(messages.whois_player
                .replace("%player%", targetUser.getName())
                .replace("%user%", member.getUser().getAsTag())));
    }

    public void openInvite(Player player) {
        MainConfig config = plugin.configManager.get();
        if (codeToPlayer.containsValue(player.getUniqueId())) {
            player.sendMessage(Text.of(
                    TextColors.RED, "You already have an code generated: ",
                    TextColors.AQUA, codeToPlayer.entrySet().stream()
                            .filter(e -> e.getValue().equals(player.getUniqueId()))
                            .findFirst().get().getKey()
            ));
            return;
        }
        String code = String.valueOf((int)(Math.random()*9000)+1000);
        String msg = config.messages.link_code_message
                .replace("%botname%", MagiBridge.getInstance().getJDA().getSelfUser().getName())
                .replace("%code%", code);
        codeToPlayer.put(code, player.getUniqueId());
        player.sendMessage(Utils.toText(msg));
    }

    public void completeLink(String code, User user, MessageChannel channel) {
        UUID player = this.codeToPlayer.get(code);
        if (player == null)
            return;

        MainConfig config = plugin.configManager.get();

        LinkInfo info = plugin.links.get(player);
        if (info != null) {
            this.codeToPlayer.remove(code);
            channel.sendMessage(config.messages.already_linked).queue();
            return;
        }

        Guild guild = MagiBridge.getInstance().getJDA()
                .getTextChannelById(MagiBridge.getInstance().getConfig().CHANNELS.MAIN_CHANNEL)
                .getGuild();

        Member member = guild.getMember(user);
        if (member == null) {
            channel.sendMessage("You're not part of " + guild.getName() + " anymore, so you can't link!").queue();
            return;
        }

        info = new LinkInfo();
        info.uuid = player;
        info.discordId = user.getId();
        plugin.links.save(info);

        guild.getController().addRolesToMember(member, guild.getRolesByName(config.linked_users_role, true)).queue();

        channel.sendMessage(config.messages.discord_linked_successfully
                .replace("%player%", info.getSpongeUser().getName())
                .replace("%user%", user.getAsTag())).queue();

        info.getSpongeUser().getPlayer().ifPresent(p -> p.sendMessage(
                Utils.toText(config.messages.linked_successfully.replace("%user%", user.getName())
                )));

        final LinkInfo finalInfo = info;
        config.commands.link.forEach(cmd ->
                Task.builder().execute(() ->
                        Sponge.getCommandManager().process(Sponge.getServer().getConsole(),
                                cmd.replace("%player%", finalInfo.getSpongeUser().getName())
                                    .replace("%user%", user.getAsTag())
                        )).submit(plugin)
        );

    }

    public void unlink(Player player) {
        MainConfig config = plugin.configManager.get();

        LinkInfo linkInfo = plugin.links.get(player.getUniqueId());
        if (linkInfo == null) {
            player.sendMessage(Utils.toText(config.messages.not_linked));
            return;
        }

        User discordUser = linkInfo.getDiscordUser();
        player.sendMessage(Utils.toText(config.messages.unlink
                .replace("%player%", player.getName())
                .replace("%user%", discordUser != null ? discordUser.getAsTag() : "Unknown")));

        if (discordUser != null) {
            Guild guild = discordUser.getJDA()
                    .getTextChannelById(MagiBridge.getInstance().getConfig().CHANNELS.MAIN_CHANNEL)
                    .getGuild();
            guild.getController().removeRolesFromMember(
                    guild.getMember(discordUser),
                    guild.getRolesByName(config.linked_users_role, true)).queue();
        }

        config.commands.unlink.forEach(cmd ->
                Sponge.getCommandManager().process(Sponge.getServer().getConsole(),
                        cmd.replace("%player%", player.getName())
                            .replace("%user%", discordUser != null ? discordUser.getAsTag() : "Unknown"))
        );

        plugin.links.delete(linkInfo);
    }

    public void syncRoles(Player p) {
        LinkInfo info = plugin.links.get(p.getUniqueId());
        if (info == null)
            return;

        JDA jda = MagiBridge.getInstance().getJDA();
        Guild guild = jda.getTextChannelById(MagiBridge.getInstance().getConfig().CHANNELS.MAIN_CHANNEL).getGuild();

        Member member = guild.getMemberById(info.discordId);
        if (member == null) {
            plugin.logger.info("The user " + p.getName() + " isn't in the bot guild anymore, unlinking...");
            unlink(p);
            return;
        }

        MainConfig config = plugin.configManager.get();

        if (config.syncNicknames && !member.equals(guild.getOwner())) {
            if (!p.getName().trim().equals(member.getEffectiveName())) {
                try {
                    guild.getController().setNickname(member, p.getName()).queue();
                } catch (Exception e) {
                    plugin.logger.error(e.getMessage());
                }
                plugin.logger.info("Setting the nickname of " + member.getUser().getName() + " to " + p.getName());
            }
        }
        config.roles_to_sync.forEach((permission, roleId) -> {
            Role role = jda.getRoleById(roleId);
            if (role == null) {
                plugin.logger.info("The role with the ID " + roleId + " doesn't exist, remove it from the config!");
                return;
            }

            if (config.inverseSync) {
                // permission = group
                if (permission.equalsIgnoreCase("default"))
                    return;

                if (member.getRoles().contains(role)) {
                    if (!p.hasPermission(config.permission_check_prefix + permission)) {
                        Sponge.getCommandManager().process(Sponge.getServer().getConsole(),
                                config.addGroupCommand.replace("%group%", permission)
                                .replace("%player%", p.getName())
                        );
                        plugin.logger.info("Adding group " + permission + " to " + p.getName());
                    }
                } else {
                    if (p.hasPermission(config.permission_check_prefix+ permission)) {
                        Sponge.getCommandManager().process(Sponge.getServer().getConsole(),
                                config.removeGroupCommand.replace("%group%", permission)
                                        .replace("%player%", p.getName())
                        );
                        plugin.logger.info("Removing group " + permission + " from " + p.getName() + " because he no longer has the required role to use it");
                    }
                }
            } else {
                if (p.hasPermission(permission)) {
                    if (!member.getRoles().contains(role)) {
                        try {
                            guild.getController().addSingleRoleToMember(member, role).queue();
                        } catch (Exception e) {
                            plugin.logger.error(e.getMessage());
                        }
                        plugin.logger.info("Adding role " + role.getName() + " to " + member.getUser().getName());
                    }
                } else {
                    if (member.getRoles().contains(role)) {
                        try {
                            guild.getController().removeSingleRoleFromMember(member, role).queue();
                        } catch (Exception e) {
                            plugin.logger.error(e.getMessage());
                        }
                        plugin.logger.info("Removing \"" + role.getName() + "\" from " + member.getUser().getName() + " because he no longer has permission to get that Role.");
                    }
                }
            }
        });
    }

}
