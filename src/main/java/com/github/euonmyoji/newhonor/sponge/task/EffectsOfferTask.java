package com.github.euonmyoji.newhonor.sponge.task;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import com.github.euonmyoji.newhonor.common.api.OfferType;
import com.github.euonmyoji.newhonor.sponge.NewHonor;
import com.github.euonmyoji.newhonor.sponge.api.event.OfferPlayerEffectsEvent;
import com.github.euonmyoji.newhonor.sponge.configuration.EffectsConfig;
import com.github.euonmyoji.newhonor.sponge.configuration.PluginConfig;
import com.github.euonmyoji.newhonor.sponge.data.ParticleEffectData;
import com.github.euonmyoji.newhonor.sponge.data.RandomDelayData;
import com.github.euonmyoji.newhonor.sponge.data.RandomEffectsData;
import com.github.euonmyoji.newhonor.sponge.util.Util;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.euonmyoji.newhonor.sponge.data.ParticleEffectData.PARTICLES_KEY;

/**
 * @author yinyangshi
 */
public class EffectsOfferTask {
    public static final HashMap<String, SelfTaskData> TASK_DATA = new HashMap<>();
    private static final String EFFECTS_KEY = "effects";

    static {
        Task.builder().async().execute(() -> {
            synchronized (TASK_DATA) {
                TASK_DATA.forEach((s, data) -> data.call());
            }
        }).name("NewHonor - Effects Offer Task").intervalTicks(PluginConfig.getIntervalTicks()).submit(NewHonor.plugin);
    }

    public static void update(Iterable<String> effects) {
        synchronized (TASK_DATA) {
            TASK_DATA.clear();
            effects.forEach(id -> {
                try {
                    EffectsConfig ec = new EffectsConfig(id);
                    if (!ec.cfg.getNode(EFFECTS_KEY).isVirtual() || !ec.cfg.getNode(PARTICLES_KEY).isVirtual()) {
                        TASK_DATA.put(id, new SelfTaskData(new EffectsConfig(id)));
                    }
                } catch (ObjectMappingException e) {
                    NewHonor.logger.warn("The Effects is error | id:" + id, e);
                }
            });
        }
    }

    private static class SelfTaskData {
        private final RandomDelayData delayData;
        private final List<PotionEffect> potionEffects;
        private final String id;
        private final Collection<RandomEffectsData> randomList = new ArrayList<>();
        private final ParticleEffectData particleEffectData;
        private LocalDateTime lastRunTime = LocalDateTime.MIN;
        private int lastDelay = 0;

        private void call() {
            List<Player> list = Util.getStream(Util.getPlayerUsingEffects(id)).map(Sponge.getServer()::getPlayer)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            if (!list.isEmpty()) {
                if (Util.getTimeDuration(lastRunTime) > lastDelay) {
                    execute(list);
                }
                randomList.forEach(data -> {
                    if (Util.getTimeDuration(data.lastRunTime) > data.lastDelay) {
                        data.execute(list);
                    }
                });
            }
        }

        private SelfTaskData(EffectsConfig config) throws ObjectMappingException {
            id = config.getId();
            potionEffects = config.getEffects();
            delayData = new RandomDelayData(config.cfg.getNode(EFFECTS_KEY, "delay").getString("0"));
            config.cfg.getNode(EFFECTS_KEY, "random").getChildrenMap().forEach((o, cfg) -> {
                try {
                    randomList.add(new RandomEffectsData(cfg, id));
                } catch (ObjectMappingException e) {
                    NewHonor.logger.warn(String.format("There is something wrong with effects id:%s, random id:%s",
                            id, o.toString()), e);
                }
            });
            CommentedConfigurationNode node = config.cfg.getNode("effects", PARTICLES_KEY);
            particleEffectData = node.isVirtual() ? null : new ParticleEffectData(node, id);
        }

        private void execute(List<Player> list) {
            lastRunTime = LocalDateTime.now();
            lastDelay = delayData.getDelay();
            Task.builder().execute(() -> {
                Timing timing = Timings.of(NewHonor.plugin, "NewHonorOfferPlayerSelfEffects");
                timing.startTiming();
                list.forEach(player -> {
                    OfferPlayerEffectsEvent event = new OfferPlayerEffectsEvent(player, id, null, OfferType.Owner, particleEffectData);
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

    private EffectsOfferTask() {
        throw new UnsupportedOperationException();
    }
}
