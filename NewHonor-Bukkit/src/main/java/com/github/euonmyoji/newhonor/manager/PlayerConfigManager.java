package com.github.euonmyoji.newhonor.manager;

import com.github.euonmyoji.newhonor.api.configuration.PlayerConfig;

import java.util.HashMap;

/**
 * @author yinyangshi
 */
public final class PlayerConfigManager {
    public static String d = "mysql";
    public static HashMap<String, Class<? extends PlayerConfig>> map = new HashMap<String, Class<? extends PlayerConfig>>() {{
        put("mysql", MysqlManager.MysqlPlayerConfig.class);
    }};

    private PlayerConfigManager() {
        throw new UnsupportedOperationException();
    }
}
