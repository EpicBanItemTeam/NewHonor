package com.github.euonmyoji.newhonor.api.event;

import com.github.euonmyoji.newhonor.NewHonor;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * fire when newhonor reload
 *
 * @author yinyangshi
 */
@NonnullByDefault
public class NewHonorReloadEvent implements Event {

    @Override
    public Cause getCause() {
        return Cause.builder().append(NewHonor.plugin).build(EventContext.empty());
    }
}
