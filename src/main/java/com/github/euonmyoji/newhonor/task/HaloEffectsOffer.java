package com.github.euonmyoji.newhonor.task;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.configuration.EffectsConfig;
import com.github.euonmyoji.newhonor.data.HaloEffectsData;
import com.github.euonmyoji.newhonor.util.Util;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * @author yinyangshi
 */
public class HaloEffectsOffer {
    public static final HashMap<String, HaloTaskData> TASK_DATA = new HashMap<>();

    static {
        org.spongepowered.api.scheduler.Task.builder().async().execute(() -> {
            synchronized (TASK_DATA) {
                TASK_DATA.forEach((s, data) -> data.call());
            }
        }).name("NewHonor - Halo Effects Offer Task").intervalTicks(10).submit(NewHonor.plugin);
    }

    static void update(List<String> effects) {
        effects.forEach(id -> {
            synchronized (TASK_DATA) {
                TASK_DATA.put(id, new HaloTaskData(new EffectsConfig(id)));
            }
        });
    }

    private static class HaloTaskData {
        private final List<HaloEffectsData> randomList = new ArrayList<>();
        private final String id;

        private void call() {
            LocalDateTime now = LocalDateTime.now();
            List<UUID> list = Util.getPlayerUsingEffects(id);
            randomList.forEach(data -> {
                double d = ((double) Duration.between(data.lastRunTime, now).getSeconds()) / 20 + 5;
                if (d > data.lastDelay) {
                    data.execute(list);
                }
            });
        }

        private HaloTaskData(EffectsConfig config) {
            id = config.getId();
            config.cfg.getNode("halo").getChildrenMap().forEach((o, cfg) -> {
                try {
                    randomList.add(new HaloEffectsData(cfg));
                } catch (ObjectMappingException e) {
                    NewHonor.plugin.logger.warn(String.format("There is something wrong with effects id:%s, random id:%s",
                            id, o.toString()), e);
                }
            });
        }
    }
}
