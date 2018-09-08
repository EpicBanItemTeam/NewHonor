package com.github.euonmyoji.newhonor.configuration;

import com.github.euonmyoji.newhonor.api.configuration.PlayerConfig;

import java.util.UUID;

/**
 * @author yinyangshi
 */
public abstract class BasePlayerConfig implements PlayerConfig {
    protected UUID uuid;

    @Override
    public UUID getUUID() {
        return this.uuid;
    }
}
