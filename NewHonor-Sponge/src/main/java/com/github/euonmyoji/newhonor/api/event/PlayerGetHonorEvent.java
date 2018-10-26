package com.github.euonmyoji.newhonor.api.event;

import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.UUID;

/**
 * fire when a player get a honor
 *
 * @author yinyangshi
 */
@SuppressWarnings("unused")
@NonnullByDefault
public class PlayerGetHonorEvent implements Event, Cancellable {
    private Cause cause;
    private UUID uuid;
    private String honorID;
    private boolean canceled;

    /**
     * @param cause   原因
     * @param uuid    被给玩家的uuid
     * @param honorID 被给头衔ID
     */
    public PlayerGetHonorEvent(Cause cause, UUID uuid, String honorID) {
        this.cause = cause;
        this.uuid = uuid;
        this.honorID = honorID;
    }

    /**
     * 获得这次事件中被给头衔的玩家的uuid
     *
     * @return 被给玩家的uuid
     */
    public UUID getUuid() {
        return this.uuid;
    }

    /**
     * 获得这次事件中被给头衔ID
     *
     * @return 被给予的头衔id
     */
    public String getHonorID() {
        return this.honorID;
    }

    @Override
    public Cause getCause() {
        return cause;
    }

    @Override
    public boolean isCancelled() {
        return this.canceled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.canceled = cancel;
    }
}
