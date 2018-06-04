package com.github.euonmyoji.newhonor.api.event;

import com.github.euonmyoji.newhonor.NewHonor;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * fire when offer player effect by effects
 *
 * @author yinyangshi
 */
@NonnullByDefault
@SuppressWarnings("unused")
public class OfferPlayerEffectsEvent implements Event, Cancellable {
    private String effectsID;
    private Player player;
    @Nullable
    private Player from;
    private List<PotionEffect> potionEffects;
    private Cause cause = Cause.builder().append(NewHonor.plugin).build(EventContext.empty());
    private boolean halo;
    private boolean canceled = false;

    public OfferPlayerEffectsEvent(@Nonnull Player player, @Nonnull String effectsID, @Nullable Player from, @Nonnull List<PotionEffect> potionEffects, boolean halo) {
        this.player = player;
        this.effectsID = effectsID;
        this.from = from;
        this.potionEffects = potionEffects;
        this.halo = halo;
    }

    public String getEffectsID() {
        return this.effectsID;
    }

    public Player getPlayer() {
        return this.player;
    }

    public List<PotionEffect> getPotionEffects() {
        return this.potionEffects;
    }

    public Optional<Player> getFrom() {
        return Optional.ofNullable(from);
    }

    public boolean isHalo() {
        return this.halo;
    }

    @Override
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
