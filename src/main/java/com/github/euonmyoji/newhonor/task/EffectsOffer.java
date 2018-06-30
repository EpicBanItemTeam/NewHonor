package com.github.euonmyoji.newhonor.task;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.event.OfferPlayerEffectsEvent;
import com.github.euonmyoji.newhonor.configuration.EffectsConfig;
import com.github.euonmyoji.newhonor.data.EffectsDelayData;
import com.github.euonmyoji.newhonor.data.RandomEffectsData;
import com.github.euonmyoji.newhonor.util.Util;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.scheduler.Task;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author yinyangshi
 */
public class EffectsOffer {
    public static final HashMap<String, SelfTaskData> TASK_DATA = new HashMap<>();
    private static final String EFFECTS_KEY = "effects";

    static {
        Task.builder().async().execute(() -> {
            synchronized (TASK_DATA) {
                TASK_DATA.forEach((s, data) -> data.call());
            }
        }).name("NewHonor - Effects Offer Task").intervalTicks(8).submit(NewHonor.plugin);
    }

    static void update(List<String> effects) {
        synchronized (TASK_DATA) {
            TASK_DATA.clear();
            effects.forEach(id -> {
                try {
                    EffectsConfig ec = new EffectsConfig(id);
                    if (!ec.cfg.getNode(EFFECTS_KEY).isVirtual()) {
                        TASK_DATA.put(id, new SelfTaskData(new EffectsConfig(id)));
                    }
                } catch (ObjectMappingException e) {
                    NewHonor.plugin.logger.warn("The Effects is error | id:" + id, e);
                }
            });
        }
    }

    private static class SelfTaskData {
        private final EffectsDelayData delayData;
        private final List<PotionEffect> potionEffects;
        private final String id;
        private final List<RandomEffectsData> randomList = new ArrayList<>();
        private LocalDateTime lastRunTime = LocalDateTime.MIN;
        private int lastDelay = 0;

        private void call() {
            List<UUID> list = Util.getPlayerUsingEffects(id);
            if (Util.getTimeDuration(lastRunTime) > lastDelay) {
                execute(list);
            }
            randomList.forEach(data -> {
                if (Util.getTimeDuration(data.lastRunTime) > data.lastDelay) {
                    data.execute(list);
                }
            });
        }

        private SelfTaskData(EffectsConfig config) throws ObjectMappingException {
            id = config.getId();
            potionEffects = config.getEffects();
            delayData = new EffectsDelayData(config.cfg.getNode(EFFECTS_KEY, "delay").getString("0"));
            config.cfg.getNode(EFFECTS_KEY, "random").getChildrenMap().forEach((o, cfg) -> {
                try {
                    randomList.add(new RandomEffectsData(cfg, id));
                } catch (ObjectMappingException e) {
                    NewHonor.plugin.logger.warn(String.format("There is something wrong with effects id:%s, random id:%s",
                            id, o.toString()), e);
                }
            });
        }

        private void execute(List<UUID> list) {
            lastRunTime = LocalDateTime.now();
            lastDelay = delayData.getDelay();
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
