package com.magitechserver.mbdiscordlink;

import com.magitechserver.magibridge.api.DiscordEvent;
import net.dv8tion.jda.core.entities.PrivateChannel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.UUID;

/**
 * Created by Frani on 28/09/2017.
 */

public class Listeners {

    @Listener
    public void onPrivateMessage(DiscordEvent.MessageEvent event) {
        if (!(event.getChannel() instanceof PrivateChannel)) return;
        String code = event.getRawMessage();
        if (MBDiscordLink.pendingUsers.containsKey(code)) {
            UUID uuid = UUID.fromString(MBDiscordLink.pendingUsers.get(code));
            if (Sponge.getServer().getPlayer(uuid).isPresent()) {
                Player player = Sponge.getServer().getPlayer(uuid).get();

                // Linked successfully
                if (MBDiscordLink.addUser(uuid, event.getUser().getId())) {
                    MBDiscordLink.API.replyTo(event.getMessage(), MBDiscordLink.config.messages.discord_linked_successfully.replace("%player%", player.getName()));
                    player.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(MBDiscordLink.config.messages.linked_successfully.replace("%user%", event.getUser().getName())));
                    MBDiscordLink.API.getJDA().getGuilds().stream()
                            .filter(g -> g.getMembers().contains(g.getMember(event.getUser())))
                            .forEach(g -> g.getController().addRolesToMember(g.getMember(event.getUser()), g.getRolesByName(MBDiscordLink.config.linked_users_role, true)).queue());
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
