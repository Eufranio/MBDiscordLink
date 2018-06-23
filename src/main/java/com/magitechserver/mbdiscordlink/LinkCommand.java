package com.magitechserver.mbdiscordlink;

import net.dv8tion.jda.core.entities.User;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.UUID;

/**
 * Created by Frani on 28/09/2017.
 */
public class LinkCommand {

    public static void registerCommands() {
        CommandSpec link = CommandSpec.builder()
                .description(Text.of("Links your account with your Discord account"))
                .permission("magibridge.link")
                .executor(new CommandExecutor() {
                    @Override
                    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
                        if (!(src instanceof Player)) return CommandResult.success();

                        String code = String.valueOf((int)(Math.random()*9000)+1000);
                        String msg = MBDiscordLink.config.messages.link_code_message
                                .replace("%botname%", MBDiscordLink.API.getJDA().getSelfUser().getName())
                                .replace("%code%", code);
                        MBDiscordLink.pendingUsers.put(code, ((Player)src).getUniqueId().toString());
                        MBDiscordLink.reload();
                        src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(msg));
                        return CommandResult.empty();
                    }
                })
                .build();
        Sponge.getCommandManager().register(MBDiscordLink.instance, link, "link");

        CommandSpec unlink = CommandSpec.builder()
                .description(Text.of("Unlinks your account from your Discord account"))
                .permission("magibridge.unlink")
                .executor(new CommandExecutor() {
                    @Override
                    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
                        if (!(src instanceof Player)) return CommandResult.success();

                        String msg = MBDiscordLink.config.messages.unlink
                                .replace("%player%", ((Player)src).getName());

                        UUID playerUuid = ((Player) src).getUniqueId();
                        String userId = MBDiscordLink.users.users.get(playerUuid.toString());
                        if (userId != null && !userId.isEmpty()) {
                            User user = MBDiscordLink.API.getJDA().getUserById(userId);
                            if (user != null) {
                                MBDiscordLink.API.getJDA().getGuilds().stream()
                                        .filter(g -> g.getMembers().contains(g.getMember(user)))
                                        .forEach(g -> g.getController().removeRolesFromMember
                                                (g.getMember(user), g.getRolesByName(MBDiscordLink.config.linked_users_role, true))
                                                .queue());
                            }
                        }

                        if (!MBDiscordLink.removeUser(((Player)src).getUniqueId())) {
                            msg = MBDiscordLink.config.messages.not_linked;
                        } else {
                            for (String command : MBDiscordLink.config.commands.unlink) {
                                Sponge.getCommandManager().process(Sponge.getServer().getConsole(), command.replace("%player%", ((Player)src).getName()));
                            }

                        }
                        MBDiscordLink.reload();
                        src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(msg));
                        return CommandResult.empty();
                    }
                })
                .build();
        Sponge.getCommandManager().register(MBDiscordLink.instance, unlink, "unlink");
    }

}
