package com.github.euonmyoji.newhonor.api.configuration;

import com.github.euonmyoji.newhonor.configuration.LocalPlayerConfig;
import com.github.euonmyoji.newhonor.configuration.MysqlManager;

import java.util.HashMap;
import java.util.UUID;

/**
 * @author yinyangshi
 */
public abstract class BasePlayerConfig implements PlayerConfig {
    static String d = "mysql";
    static HashMap<String, Class<? extends PlayerConfig>> map = new HashMap<String, Class<? extends PlayerConfig>>() {{
        put("local", LocalPlayerConfig.class);
        put("mysql", MysqlManager.MysqlPlayerConfig.class);
    }};
    protected UUID uuid;

    @Override
    public UUID getUUID() {
        return this.uuid;
    }
}
