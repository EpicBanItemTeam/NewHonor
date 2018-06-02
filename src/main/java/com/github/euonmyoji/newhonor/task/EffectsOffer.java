package com.github.euonmyoji.newhonor.task;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.configuration.EffectsConfig;
import com.github.euonmyoji.newhonor.data.EffectsDelayData;
import com.github.euonmyoji.newhonor.data.RandomEffectsData;
import com.github.euonmyoji.newhonor.util.Util;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.scheduler.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author yinyangshi
 */
public class EffectsOffer {
    public static final HashMap<String, SelfTaskData> TASK_DATA = new HashMap<>();

    static {
        org.spongepowered.api.scheduler.Task.builder().async().execute(() -> {
            synchronized (TASK_DATA) {
                TASK_DATA.forEach((s, data) -> data.call());
            }
        }).name("NewHonor - Effects Offer Task").intervalTicks(10).submit(NewHonor.plugin);
    }

    static void update(List<String> effects) {
        effects.forEach(id -> {
            synchronized (TASK_DATA) {
                try {
                    TASK_DATA.put(id, new SelfTaskData(new EffectsConfig(id)));
                } catch (ObjectMappingException e) {
                    NewHonor.plugin.logger.warn("The Effects is error | id:" + id, e);
                }
            }
        });
    }

    private static class SelfTaskData {
        private final EffectsDelayData delayData;
        private final List<PotionEffect> potionEffects;
        private final String id;
        private final List<RandomEffectsData> randomList = new ArrayList<>();
        private LocalDateTime lastRunTime = LocalDateTime.MIN;
        private int lastDelay = 0;

        private void call() {
            LocalDateTime now = LocalDateTime.now();
            List<UUID> list = Util.getPlayerUsingEffects(id);
            double duration = ((double) Duration.between(lastRunTime, now).getSeconds()) / 20 + 5;
            if (duration > lastDelay) {
                execute(list);
            }
            randomList.forEach(data -> {
                double d = ((double) Duration.between(data.lastRunTime, now).getSeconds()) / 20 + 5;
                if (d > data.lastDelay) {
                    data.execute(list);
                }
            });
        }

        private SelfTaskData(EffectsConfig config) throws ObjectMappingException {
            id = config.getId();
            potionEffects = config.getEffects();
            delayData = new EffectsDelayData(config.cfg.getNode("effects", "delay").getString("15"));
            config.cfg.getNode("effects", "random").getChildrenMap().forEach((o, cfg) -> {
                try {
                    randomList.add(new RandomEffectsData(cfg));
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
                    .forEach(player -> Util.offerEffects(player, potionEffects))).submit(NewHonor.plugin);
        }
    }
}
