package com.github.euonmyoji.newhonor.sponge.api.event;

import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.UUID;

/**
 * fire when a player lose honor(s)
 *
 * @author yinyangshi
 */
@NonnullByDefault
@SuppressWarnings("unused")
public class PlayerLoseHonorEvent implements Event, Cancellable {
    private Cause cause;
    private UUID uuid;
    private String[] honorIDs;
    private boolean canceled;

    /**
     * @param cause    原因
     * @param uuid     被给玩家的uuid
     * @param honorIDs 被给头衔ID
     */
    public PlayerLoseHonorEvent(Cause cause, UUID uuid, String... honorIDs) {
        this.cause = cause;
        this.uuid = uuid;
        this.honorIDs = honorIDs;
    }

    /**
     * 获得这次事件中失去头衔的玩家的uuid
     *
     * @return 被给玩家的uuid
     */
    public UUID getUuid() {
        return this.uuid;
    }

    /**
     * 获得这次事件中失去的头衔ID
     *
     * @return 被给予的头衔id
     */
    public String[] getHonorIDs() {
        return this.honorIDs;
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
