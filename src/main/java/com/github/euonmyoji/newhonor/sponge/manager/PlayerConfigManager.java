package com.github.euonmyoji.newhonor.sponge.manager;

import com.github.euonmyoji.newhonor.sponge.api.configuration.PlayerConfig;
import com.github.euonmyoji.newhonor.sponge.configuration.LocalPlayerConfig;

import java.util.HashMap;

/**
 * @author yinyangshi
 */
public final class PlayerConfigManager {
    public static String d = "mysql";
    public static HashMap<String, Class<? extends PlayerConfig>> map = new HashMap<String, Class<? extends PlayerConfig>>() {{
        put("local", LocalPlayerConfig.class);
        put("mysql", MysqlManager.MysqlPlayerConfig.class);
    }};

    private PlayerConfigManager() {
        throw new UnsupportedOperationException();
    }
}
