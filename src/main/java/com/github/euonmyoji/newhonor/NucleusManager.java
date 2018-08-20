package com.github.euonmyoji.newhonor;

import com.github.euonmyoji.newhonor.data.HonorValueData;
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
                NucleusAPI.getMessageTokenService().register(NewHonor.getContainer(), (tokenInput, source, variables) -> {
                    if (NewHonor.getContainer().getId().equals(tokenInput) && source instanceof Identifiable) {
                        return Optional.ofNullable(NewHonor.plugin.honorTextCache.get((((Identifiable) source).getUniqueId())))
                                .map(HonorValueData::getValue);
                    }
                    return Optional.empty();
                });
            }
        } catch (PluginAlreadyRegisteredException e) {
            NewHonor.logger.warn("Unknown error", e);
        }
    }

    private NucleusManager() {
        throw new UnsupportedOperationException();
    }
}
