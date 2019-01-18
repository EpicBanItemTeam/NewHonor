package com.github.euonmyoji.newhonor.data;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import com.flowpowered.math.vector.Vector3d;
import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.OfferType;
import com.github.euonmyoji.newhonor.api.event.OfferPlayerEffectsEvent;
import com.github.euonmyoji.newhonor.util.RandomDelay;
import com.github.euonmyoji.newhonor.util.Util;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import java.time.LocalDateTime;
import java.util.List;

import static com.github.euonmyoji.newhonor.util.Util.offerEffects;

/**
 * 光环特效数据
 *
 * @author yinyangshi
 */
public class HaloEffectsData {
    private final RandomDelay delayData;
    private final List<PotionEffect> potionEffects;
    private final ParticleEffectData particleEffectData;
    private final double chance;
    private final double radius;
    private final boolean include;
    private final String effectID;
    public LocalDateTime lastRunTime = LocalDateTime.MIN;
    public int lastDelay = 0;

    public HaloEffectsData(CommentedConfigurationNode cfg, String id) throws ObjectMappingException {
        effectID = id;
        delayData = new RandomDelay(cfg.getNode("delay").getString("0"));
        radius = cfg.getNode("radius").getDouble(5);
        chance = cfg.getNode("chance").getDouble(1);
        include = cfg.getNode("include-me").getBoolean(false);
        potionEffects = Util.getPotionEffects(cfg, Util.getPotionEffectsDurationTick(cfg), cfg.getNode("show").getBoolean(false));
        particleEffectData = cfg.getNode(ParticleEffectData.PARTICLES_KEY).isVirtual() ? null : new ParticleEffectData(cfg.getNode(ParticleEffectData.PARTICLES_KEY), id);
    }

    public void execute(List<Player> list) {
        lastRunTime = LocalDateTime.now();
        lastDelay = delayData.getDelay();
        if (Math.random() < chance) {
            Task.builder().execute(() -> {
                Timing timing = Timings.of(NewHonor.plugin, "NewHonorOfferPlayerHaloEffects");
                timing.startTimingIfSync();
                list.forEach(this::offer);
                timing.stopTiming();
            }).submit(NewHonor.plugin);
        }
    }

    private void offer(Player p) {
        final Vector3d o = p.getLocation().getPosition();
        p.getWorld().getPlayers().forEach(player -> {
            double distanceSquared = player.getLocation().getPosition().distanceSquared(o);
            boolean playerPass = include || !(p.getUniqueId().equals(player.getUniqueId()));
            if (playerPass && distanceSquared < (radius * radius)) {
                OfferPlayerEffectsEvent event = null;
                try {
                    event = new OfferPlayerEffectsEvent(player, effectID, p, OfferType.Halo, particleEffectData, potionEffects);
                } catch (NoSuchMethodError ignore) {
                }
                offerEffects(player, event, potionEffects, particleEffectData);
            }
        });
        if (particleEffectData != null) {
            OfferPlayerEffectsEvent event = null;
            try {
                event = new OfferPlayerEffectsEvent(p, effectID, null, OfferType.Owner, particleEffectData);
            } catch (NoSuchMethodError ignore) {
            }

            if (event == null || !Sponge.getEventManager().post(event)) {
                particleEffectData.execute(p.getLocation());
            }
        }
    }
}
