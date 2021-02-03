package com.github.euonmyoji.newhonor.api.manager;

import com.github.euonmyoji.newhonor.api.data.HonorData;

import java.util.List;
import java.util.UUID;

/**
 * @author yinyangshi
 */
public interface HonorManager {
    /**
     * Get the using honor data of the user
     *
     * @param uuid the uuid of the user
     * @return the data of honor
     */
    HonorData getUsingHonor(UUID uuid);

    /**
     * set player using honor
     *
     * @param uuid the uuid of the user
     * @param data the data to using
     */
    void setUsingHonor(UUID uuid, HonorData data);

    /**
     * get using effects of a user
     *
     * @param uuid the uuid of the user
     * @return the effects id that using
     */
    String getUsingEffects(UUID uuid);

    /**
     * get the players that using the effects
     *
     * @param id the id of the effects
     * @return player's uuid
     */
    List<UUID> getPlayersUsingEffects(String id);

    /**
     * set using effects of a user
     *
     * @param uuid    the uuid of the user
     * @param effects the effects id
     */
    void setUsingEffects(UUID uuid, String effects);

    /**
     * remove cache data of a user
     *
     * @param uuid the uuid of the user
     */
    void remove(UUID uuid);

    /**
     * clear all caches data
     */
    void clear();
}
