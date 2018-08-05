package com.github.euonmyoji.newhonor.data;

import com.flowpowered.math.vector.Vector3d;
import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.OfferType;
import com.github.euonmyoji.newhonor.api.event.OfferPlayerEffectsEvent;
import com.github.euonmyoji.newhonor.util.Util;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.github.euonmyoji.newhonor.data.ParticleEffectData.PARTICLES_KEY;

/**
 * 光环特效数据
 *
 * @author yinyangshi
 */
public class HaloEffectsData {
    private final RandomDelayData delayData;
    private final List<PotionEffect> potionEffects;
    private final ParticleEffectData particleEffectData;
    private final double chance;
    private final double radius;
    private final boolean include;
    public LocalDateTime lastRunTime = LocalDateTime.MIN;
    private final String effectID;
    public int lastDelay = 0;

    public HaloEffectsData(CommentedConfigurationNode cfg, String id) throws ObjectMappingException {
        effectID = id;
        delayData = new RandomDelayData(cfg.getNode("delay").getString("0"));
        radius = cfg.getNode("radius").getDouble(5);
        chance = cfg.getNode("chance").getDouble(1);
        include = cfg.getNode("include-me").getBoolean(false);
        potionEffects = Util.getPotionEffects(cfg, Util.getPotionEffectsDurationTick(cfg), cfg.getNode("show").getBoolean(false));
        particleEffectData = cfg.getNode(PARTICLES_KEY).isVirtual() ? null : new ParticleEffectData(cfg.getNode(PARTICLES_KEY), id);
    }

    public void execute(List<UUID> list) {
        lastRunTime = LocalDateTime.now();
        lastDelay = delayData.getDelay();
        if (Math.random() < chance) {
            Task.builder().execute(() -> Util.getStream(list)
                    .map(Sponge.getServer()::getPlayer)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(this::offer)).submit(NewHonor.plugin);
        }
    }

    private void offer(Player p) {
        final Vector3d o = p.getLocation().getPosition();
        p.getWorld().getPlayers().forEach(player -> {
            double distanceSquared = player.getLocation().getPosition().distanceSquared(o);
            boolean playerPass = include || !(p.getUniqueId().equals(player.getUniqueId()));
            if (playerPass && distanceSquared < (radius * radius)) {
                OfferPlayerEffectsEvent event = new OfferPlayerEffectsEvent(player, effectID, p, OfferType.Halo, particleEffectData, potionEffects);
                if (!Sponge.getEventManager().post(event)) {
                    Util.offerEffects(player, potionEffects);
                    if (particleEffectData != null) {
                        particleEffectData.execute(player.getLocation());
                    }
                }
            }
        });
        if (particleEffectData != null) {
            OfferPlayerEffectsEvent event = new OfferPlayerEffectsEvent(p, effectID, null, OfferType.Owner, particleEffectData);
            if (!Sponge.getEventManager().post(event)) {
                particleEffectData.execute(p.getLocation());
            }
        }
    }
}
