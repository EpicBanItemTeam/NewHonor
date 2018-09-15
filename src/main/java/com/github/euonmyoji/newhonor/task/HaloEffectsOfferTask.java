package com.github.euonmyoji.newhonor.task;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.configuration.EffectsConfig;
import com.github.euonmyoji.newhonor.data.HaloEffectsData;
import com.github.euonmyoji.newhonor.util.Util;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yinyangshi
 */
public class HaloEffectsOfferTask {
    public static final HashMap<String, HaloTaskData> TASK_DATA = new HashMap<>();
    private static final String HALO_KEY = "halo";

    static {
        Task.builder().async().execute(() -> {
            synchronized (TASK_DATA) {
                TASK_DATA.forEach((s, data) -> data.call());
            }
        }).name("NewHonor - Halo Effects Offer Task").intervalTicks(Util.INTERVAL_TICKS).submit(NewHonor.plugin);
    }

    public static void update(Iterable<String> effects) {
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
        private final Collection<HaloEffectsData> randomList = new ArrayList<>();
        private final String id;

        private void call() {
            List<Player> list = Util.getStream(Util.getPlayerUsingEffects(id)).map(Sponge.getServer()::getPlayer)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            if (!list.isEmpty()) {
                randomList.forEach(data -> {
                    if (Util.getTimeDuration(data.lastRunTime) > data.lastDelay) {
                        data.execute(list);
                    }
                });
            }
        }

        private HaloTaskData(EffectsConfig config) {
            id = config.getId();
            config.cfg.getNode(HALO_KEY).getChildrenMap().forEach((o, cfg) -> {
                try {
                    randomList.add(new HaloEffectsData(cfg, id));
                } catch (ObjectMappingException e) {
                    NewHonor.logger.warn(String.format("There is something wrong with effects id:%s, random id:%s",
                            id, o.toString()), e);
                }
            });
        }
    }

    private HaloEffectsOfferTask() {
        throw new UnsupportedOperationException();
    }
}
