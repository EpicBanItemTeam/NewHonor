package com.github.euonmyoji.newhonor.manager;

import com.github.euonmyoji.newhonor.api.data.HonorData;
import com.github.euonmyoji.newhonor.api.manager.HonorManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yinyangshi
 */
public class HonorManagerImpl implements HonorManager {
    private Map<UUID, HonorData> honorDataCache = new ConcurrentHashMap<>(8);
    private Map<UUID, String> playerUsingEffectCache = new ConcurrentHashMap<>(8);

    @Override
    public HonorData getUsingHonor(UUID uuid) {
        return honorDataCache.get(uuid);
    }

    @Override
    public String getUsingEffects(UUID uuid) {
        return playerUsingEffectCache.get(uuid);
    }

    @Override
    public void setUsingHonor(UUID uuid, HonorData data) {
        honorDataCache.put(uuid, data);
    }

    @Override
    public void setUsingEffects(UUID uuid, String effects) {
        playerUsingEffectCache.put(uuid, effects);
    }

    @Override
    public void remove(UUID uuid) {
        honorDataCache.remove(uuid);
        playerUsingEffectCache.remove(uuid);
    }

    @Override
    public List<UUID> getPlayersUsingEffects(String id) {
        List<UUID> list = new ArrayList<>((playerUsingEffectCache.size() / 4) + 1);
        for (Map.Entry<UUID, String> entry : playerUsingEffectCache.entrySet()) {
            if (entry.getValue().equals(id)) {
                list.add(entry.getKey());
            }
        }
        return list;
    }

    @Override
    public void clear() {
        honorDataCache = new ConcurrentHashMap<>(8);
        playerUsingEffectCache = new ConcurrentHashMap<>(8);
    }
}
