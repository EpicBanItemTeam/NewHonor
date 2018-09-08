package com.github.euonmyoji.newhonor.manager;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.data.HonorValueData;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.exceptions.PluginAlreadyRegisteredException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.util.Identifiable;

import java.util.Optional;

/**
 * @author yinyangshi
 */
public final class NucleusManager {
    private static boolean done;

    public static void doIt() {
        try {
            if (!done) {
                done = true;
                NucleusAPI.getMessageTokenService().register(Sponge.getPluginManager().getPlugin(NewHonor.NEWHONOR_ID)
                        .orElseThrow(NoSuchFieldError::new), (tokenInput, src, variables) -> {
                    if (NewHonor.NEWHONOR_ID.equals(tokenInput) && src instanceof Identifiable) {
                        return Optional.ofNullable(NewHonor.plugin.honorTextCache.get((((Identifiable) src).getUniqueId())))
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
