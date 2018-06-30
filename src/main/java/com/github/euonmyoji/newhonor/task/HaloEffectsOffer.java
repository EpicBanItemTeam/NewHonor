package com.github.euonmyoji.newhonor.task;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.configuration.EffectsConfig;
import com.github.euonmyoji.newhonor.data.HaloEffectsData;
import com.github.euonmyoji.newhonor.util.Util;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.scheduler.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * @author yinyangshi
 */
public class HaloEffectsOffer {
    public static final HashMap<String, HaloTaskData> TASK_DATA = new HashMap<>();
    private static final String HALO_KEY = "halo";

    static {
        Task.builder().async().execute(() -> {
            synchronized (TASK_DATA) {
                TASK_DATA.forEach((s, data) -> data.call());
            }
        }).name("NewHonor - Halo Effects Offer Task").intervalTicks(8).submit(NewHonor.plugin);
    }

    static void update(List<String> effects) {
        synchronized (TASK_DATA) {
            TASK_DATA.clear();
            effects.forEach(id -> {
                EffectsConfig ec = new EffectsConfig(id);
                if (!ec.cfg.getNode(HALO_KEY).isVirtual()) {
                    TASK_DATA.put(id, new HaloTaskData(ec));
                }
            });
        }
    }

    private static class HaloTaskData {
        private final List<HaloEffectsData> randomList = new ArrayList<>();
        private final String id;

        private void call() {
            List<UUID> list = Util.getPlayerUsingEffects(id);
            randomList.forEach(data -> {
                if (Util.getTimeDuration(data.lastRunTime) > data.lastDelay) {
                    data.execute(list);
                }
            });
        }

        private HaloTaskData(EffectsConfig config) {
            id = config.getId();
            config.cfg.getNode(HALO_KEY).getChildrenMap().forEach((o, cfg) -> {
                try {
                    randomList.add(new HaloEffectsData(cfg, id));
                } catch (ObjectMappingException e) {
                    NewHonor.plugin.logger.warn(String.format("There is something wrong with effects id:%s, random id:%s",
                            id, o.toString()), e);
                }
            });
        }
    }
}
