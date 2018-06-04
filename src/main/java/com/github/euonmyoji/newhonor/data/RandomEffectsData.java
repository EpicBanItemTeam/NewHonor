package com.github.euonmyoji.newhonor.data;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.event.OfferPlayerEffectsEvent;
import com.github.euonmyoji.newhonor.util.Util;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.scheduler.Task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author yinyangshi
 */
public class RandomEffectsData {
    private final EffectsDelayData delayData;
    private final List<PotionEffect> potionEffects;
    private final double chance;
    public LocalDateTime lastRunTime = LocalDateTime.MIN;
    public int lastDelay = 0;
    private final String id;

    public RandomEffectsData(CommentedConfigurationNode cfg, String id) throws ObjectMappingException {
        this.id = id;
        delayData = new EffectsDelayData(cfg.getNode("delay").getString("15"));
        potionEffects = Util.getPotionEffects(cfg, Util.getPotionEffectsDurationTick(cfg), cfg.getNode("show").getBoolean());
        chance = cfg.getNode("chance").getDouble(1);
    }

    public void execute(List<UUID> list) {
        lastRunTime = LocalDateTime.now();
        lastDelay = delayData.getDelay();
        if (Math.random() < chance) {
            Task.builder().execute(() -> Util.getStream(list)
                    .map(Sponge.getServer()::getPlayer)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(player -> {
                        OfferPlayerEffectsEvent event = new OfferPlayerEffectsEvent(player, id, null, potionEffects, false);
                        Sponge.getEventManager().post(event);
                        if (!event.isCancelled()) {
                            Util.offerEffects(player, potionEffects);
                        }
                    })).submit(NewHonor.plugin);
        }
    }
}
