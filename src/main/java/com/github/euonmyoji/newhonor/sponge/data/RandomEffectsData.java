package com.github.euonmyoji.newhonor.sponge.data;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import com.github.euonmyoji.newhonor.common.api.OfferType;
import com.github.euonmyoji.newhonor.sponge.NewHonor;
import com.github.euonmyoji.newhonor.sponge.api.event.OfferPlayerEffectsEvent;
import com.github.euonmyoji.newhonor.sponge.util.Util;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import java.time.LocalDateTime;
import java.util.List;

import static com.github.euonmyoji.newhonor.sponge.data.ParticleEffectData.PARTICLES_KEY;

/**
 * 随机特效数据
 *
 * @author yinyangshi
 */
public class RandomEffectsData {
    private final RandomDelayData delayData;
    private final List<PotionEffect> potionEffects;
    private final ParticleEffectData particleEffectData;
    private final double chance;
    public LocalDateTime lastRunTime = LocalDateTime.MIN;
    public int lastDelay = 0;
    private final String id;

    public RandomEffectsData(CommentedConfigurationNode cfg, String id) throws ObjectMappingException {
        this.id = id;
        delayData = new RandomDelayData(cfg.getNode("delay").getString("15"));
        potionEffects = Util.getPotionEffects(cfg, Util.getPotionEffectsDurationTick(cfg), cfg.getNode("show").getBoolean());
        chance = cfg.getNode("chance").getDouble(1);
        particleEffectData = cfg.getNode(PARTICLES_KEY).isVirtual() ? null : new ParticleEffectData(cfg.getNode(PARTICLES_KEY), id);
    }

    public void execute(List<Player> list) {
        lastRunTime = LocalDateTime.now();
        lastDelay = delayData.getDelay();
        if (Math.random() < chance) {
            Task.builder().execute(() -> {
                Timing timing = Timings.of(NewHonor.plugin, "NewHonorOfferPlayerSelfEffects(Random)");
                timing.startTimingIfSync();
                list.forEach(player -> {
                    OfferPlayerEffectsEvent event = new OfferPlayerEffectsEvent(player, id, null, OfferType.Owner);
                    if (!Sponge.getEventManager().post(event)) {
                        Util.offerEffects(player, potionEffects);
                        if (particleEffectData != null) {
                            particleEffectData.execute(player.getLocation());
                        }
                    }
                });
                timing.stopTiming();
            }).submit(NewHonor.plugin);
        }
    }
}
