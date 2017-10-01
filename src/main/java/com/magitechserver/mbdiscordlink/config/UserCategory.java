package com.magitechserver.mbdiscordlink.config;

import com.google.common.collect.Maps;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.Map;

/**
 * Created by Frani on 28/09/2017.
 */
@ConfigSerializable
public class UserCategory {

    // UUID <> DISCORD ID
    @Setting(value = "users")
    public Map<String, String> users = Maps.newHashMap();

}
