package com.github.euonmyoji.newhonor.util;

import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.living.player.Player;

import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * @author yinyangshi
 */
public class Util {
    public static void offerEffectsSafely(Player player, List<PotionEffect> potionEffects) {
        //FIXME: TESTING
//        if (Sponge.getServer().isMainThread()) {
        offerEffects(player, potionEffects);
//        } else {
//            Task.builder().execute(() -> offerEffects(player, potionEffects)).submit(NewHonor.plugin);
//        }
    }

    private static void offerEffects(Player player, List<PotionEffect> potionEffects) throws ConcurrentModificationException {
        PotionEffectData effects = player.getOrCreate(PotionEffectData.class).orElseThrow(NoSuchFieldError::new);
        potionEffects.forEach(effects::addElement);
        player.tryOffer(effects);
    }
}
