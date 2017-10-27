package com.magitechserver.mbdiscordlink.config;

import com.google.common.reflect.TypeToken;
import com.magitechserver.mbdiscordlink.MBDiscordLink;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by Frani on 28/09/2017.
 */
public class ConfigManager {

    private File configDir;
    private MBDiscordLink instance;
    private ConfigCategory root;
    private UserCategory users;
    private ConfigurationLoader<CommentedConfigurationNode> userLoader;
    private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private ConfigurationNode configNode;
    private ConfigurationNode userNode;

    public ConfigManager(MBDiscordLink instance, File configDir) {
        this.configDir = configDir;
        this.instance = instance;
        if (!configDir.exists()) {
            try {
                configDir.createNewFile();
            } catch (Exception e) {}
        }
    }

    private ConfigCategory loadConfig() {
        try {
            File file = new File(configDir, "MBDiscordLink.conf");
            if (!file.exists()) {
                file.createNewFile();
            }
            configLoader = HoconConfigurationLoader.builder().setFile(file).build();
            configNode = configLoader.load(ConfigurationOptions.defaults().setObjectMapperFactory(instance.factory).setShouldCopyDefaults(true));
            root = configNode.getValue(TypeToken.of(ConfigCategory.class), new ConfigCategory());
            configLoader.save(configNode);
            return root;
        } catch (Exception e) {
            MBDiscordLink.logger.error("Could not load config.");
            e.printStackTrace();
            return root;
        }
    }

    public UserCategory loadUsers() {
        try {
            File file = new File(configDir, "Users.conf");
            if (!file.exists()) {
                file.createNewFile();
            }
            userLoader = HoconConfigurationLoader.builder().setFile(file).build();
            userNode = userLoader.load(ConfigurationOptions.defaults().setObjectMapperFactory(instance.factory).setShouldCopyDefaults(true));
            users = userNode.getValue(TypeToken.of(UserCategory.class), new UserCategory());
            userLoader.save(userNode);
            return users;
        } catch (Exception e) {
            MBDiscordLink.logger.error("Could not load config.", e);
            return users;
        }
    }

    public ConfigCategory reloadConfig() {
        root = null;
        return loadConfig();
    }

    public UserCategory reloadUsers() {
        try {
            userNode.setValue(TypeToken.of(UserCategory.class), users);
            userLoader.save(userNode);
        } catch (ObjectMappingException | IOException e) {}
        users = null;
        return loadUsers();
    }
}
