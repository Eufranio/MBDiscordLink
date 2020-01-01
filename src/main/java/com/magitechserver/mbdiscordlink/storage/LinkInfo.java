package com.magitechserver.mbdiscordlink.storage;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;
import com.magitechserver.magibridge.MagiBridge;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import java.util.UUID;

/**
 * Created by Frani on 31/12/2019.
 */
@DatabaseTable(tableName = "links")
public class LinkInfo extends BaseDaoEnabled<LinkInfo, UUID> {

    @DatabaseField(id = true)
    public UUID uuid;

    @DatabaseField
    public String discordId;

    public net.dv8tion.jda.api.entities.User getDiscordUser() {
        return MagiBridge.getInstance().getJDA().getUserById(this.discordId);
    }

    public User getSpongeUser() {
        return Sponge.getServer().getPlayer(this.uuid)
                .map(User.class::cast)
                .orElseGet(() -> Sponge.getServiceManager().provideUnchecked(UserStorageService.class)
                        .get(this.uuid).get());
    }

}
