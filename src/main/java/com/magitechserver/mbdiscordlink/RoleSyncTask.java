package com.magitechserver.mbdiscordlink;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Frani on 02/10/2017.
 */
public class RoleSyncTask implements Consumer<Task> {

    @Override
    public void accept(Task task) {
        JDA jda = MBDiscordLink.API.getJDA();
        for (Player p : Sponge.getServer().getOnlinePlayers()) {
            for (Map.Entry<String, String> entry : MBDiscordLink.config.roles_to_sync.entrySet()) {
                if (p.hasPermission(entry.getKey())) {
                    if (MBDiscordLink.users.users.containsKey(p.getUniqueId().toString())) {
                        String userId = MBDiscordLink.users.users.get(p.getUniqueId().toString());
                        User user = jda.getUserById(userId);
                        Role role = jda.getRoleById(entry.getValue());
                        if (user != null && role != null && !role.getGuild().getMember(user).getRoles().contains(role)) {
                            role.getGuild().getController().addSingleRoleToMember(role.getGuild().getMember(user), role).queue();
                            MBDiscordLink.logger.info("Adding role " + role.getName() + " to " + user.getName());
                        }
                    }
                } else {
                    if (MBDiscordLink.users.users.containsKey(p.getUniqueId().toString())) {
                        String userId = MBDiscordLink.users.users.get(p.getUniqueId().toString());
                        User user = jda.getUserById(userId);
                        Role role = jda.getRoleById(entry.getValue());
                        if (role.getGuild().getMember(user).getRoles().contains(role)) {
                            role.getGuild().getController().removeSingleRoleFromMember(role.getGuild().getMember(user), role).queue();
                            MBDiscordLink.logger.info("Removing \"" + role.getName() + "\" from " + user.getName() + " because he no loger has permission to get that Role.");
                        }
                    }
                }
            }
        }
    }

}
