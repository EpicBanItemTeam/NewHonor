package com.github.euonmyoji.newhonor;

import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.exceptions.PluginAlreadyRegisteredException;
import org.spongepowered.api.util.Identifiable;

import java.util.Optional;

/**
 * @author yinyangshi
 */
class NucleusManager {
    private static boolean done;

    static void doIt() {
        try {
            if (!done) {
                done = true;
                NucleusAPI.getMessageTokenService().register(NewHonor.container(), (tokenInput, source, variables) -> {
                    if (NewHonor.container().getId().equals(tokenInput) && source instanceof Identifiable) {
                        return Optional.ofNullable(NewHonor.plugin.honorTextCache.get((((Identifiable) source).getUniqueId())));
                    }
                    return Optional.empty();
                });
            }
        } catch (PluginAlreadyRegisteredException e) {
            NewHonor.plugin.logger.warn("Unknown error", e);
        }
    }
}
