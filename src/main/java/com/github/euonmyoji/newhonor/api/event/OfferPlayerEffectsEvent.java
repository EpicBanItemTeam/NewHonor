package com.github.euonmyoji.newhonor.api.event;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.OfferType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * fire when offer player effect by effects
 *
 * @author yinyangshi
 */
public class OfferPlayerEffectsEvent implements Event, Cancellable {
    private String effectsID;
    private Player player;
    @Nullable
    private Player from;
    private Cause cause;
    private OfferType offerType;
    private boolean canceled = false;

    public OfferPlayerEffectsEvent(@Nonnull Player player, @Nonnull String effectsID, @Nullable Player from, OfferType type,
                                   Object... causes) {
        this.player = player;
        this.effectsID = effectsID;
        this.from = from;
        this.offerType = type;
        Cause.Builder builder = Cause.builder();
        for (Object cause : causes) {
            if (cause != null) {
                builder.append(cause);
            }
        }
        this.cause = builder.append(NewHonor.plugin).build(EventContext.empty());
    }

    public String getEffectsID() {
        return this.effectsID;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Optional<Player> getFrom() {
        return Optional.ofNullable(from);
    }

    public boolean isHalo() {
        return this.offerType == OfferType.Halo;
    }

    @Override
    @Nonnull
    public Cause getCause() {
        return cause;
    }

    @Override
    public boolean isCancelled() {
        return canceled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        canceled = cancel;
    }
}
