package com.magitechserver.mbdiscordlink;

import com.magitechserver.magibridge.MagiBridge;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
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
        JDA jda = MagiBridge.getInstance().getJDA();
        for (Player p : Sponge.getServer().getOnlinePlayers()) {
            for (Map.Entry<String, String> entry : MBDiscordLink.config.roles_to_sync.entrySet()) {
                if (p.hasPermission(entry.getKey())) {
                    if (MBDiscordLink.users.users.containsKey(p.getUniqueId().toString())) {
                        String userId = MBDiscordLink.users.users.get(p.getUniqueId().toString());
                        User user = jda.getUserById(userId);
                        Role role = jda.getRoleById(entry.getValue());
                        if (user == null || role == null) return;

                        Member member = role.getGuild().getMember(user);
                        if (member == null) return;
                        if (!member.getRoles().contains(role)) {
                            role.getGuild().getController().addSingleRoleToMember(member, role).queue();
                            MBDiscordLink.logger.info("Adding role " + role.getName() + " to " + user.getName());
                        }
                    }
                } else {
                    if (MBDiscordLink.users.users.containsKey(p.getUniqueId().toString())) {
                        String userId = MBDiscordLink.users.users.get(p.getUniqueId().toString());
                        User user = jda.getUserById(userId);
                        Role role = jda.getRoleById(entry.getValue());
                        if (user == null || role == null) return;

                        Member member = role.getGuild().getMember(user);
                        if (member == null) return;
                        if (member.getRoles().contains(role)) {
                            role.getGuild().getController().removeSingleRoleFromMember(member, role).queue();
                            MBDiscordLink.logger.info("Removing \"" + role.getName() + "\" from " + user.getName() + " because he no loger has permission to get that Role.");
                        }
                    }
                }
            }
        }
    }

}
