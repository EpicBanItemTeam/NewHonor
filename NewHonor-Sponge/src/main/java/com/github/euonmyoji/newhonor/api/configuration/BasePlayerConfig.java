package com.github.euonmyoji.newhonor.api.configuration;

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
