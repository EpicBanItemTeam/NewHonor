package com.github.euonmyoji.newhonor.task;

import com.flowpowered.math.vector.Vector3d;
import com.github.euonmyoji.newhonor.configuration.HaloEffectsData;
import com.github.euonmyoji.newhonor.util.Util;
import org.spongepowered.api.entity.living.player.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author yinyangshi
 */
public class HaloEffects {
    public static Set<UUID> using = new HashSet<>();

    public void execute(Player p, HaloEffectsData data) {
        final Vector3d o = p.getLocation().getPosition();
        p.getWorld().getPlayers().forEach(player -> {
            double distanceSquared = player.getLocation().getPosition().distanceSquared(o);
            data.cache.forEach((distanceLimit, potionEffects) -> {
                if (!p.getUniqueId().equals(player.getUniqueId()) && distanceSquared < (distanceLimit * distanceLimit)) {
                    Util.offerEffectsSafely(player, potionEffects);
                }
            });
        });
    }
}
