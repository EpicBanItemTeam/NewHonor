package com.github.euonmyoji.newhonor.util;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.configuration.EffectsConfig;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.entity.living.player.Player;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * @author yinyangshi
 */
public class Util {
    public static void offerEffects(Player player, List<PotionEffect> potionEffects) throws ConcurrentModificationException {
        PotionEffectData effects = player.getOrCreate(PotionEffectData.class).orElseThrow(NoSuchFieldError::new);
        potionEffects.forEach(effects::addElement);
        player.tryOffer(effects);
    }

    public static int getPotionEffectsDurationTick(CommentedConfigurationNode cfg) {
        return cfg.getNode("potionEffectsDurationTick").getInt(60);
    }

    public static List<PotionEffect> getPotionEffects(CommentedConfigurationNode cfg, int tick) throws ObjectMappingException {
        List<PotionEffect> list = new ArrayList<>();
        getEffectsList(cfg).forEach(s -> {
            String[] args = s.split(EffectsConfig.CONNECT_KEY, 2);
            Sponge.getRegistry().getType(PotionEffectType.class, args[0]).ifPresent(type ->
                    list.add(PotionEffect.builder()
                            .potionType(type)
                            .amplifier(Integer.parseInt(args[1]))
                            .duration(tick)
                            .build()));
        });
        return list;
    }

    public static List<String> getEffectsList(CommentedConfigurationNode cfg) throws ObjectMappingException {
        return cfg.getNode("effects").getList(TypeToken.of(String.class), ArrayList::new);
    }

    public static <T> Stream<T> getStream(List<T> list) {
        return list.size() > 25 ? list.parallelStream() : list.stream();
    }

    public static List<UUID> getPlayerUsingEffects(String id) {
        List<UUID> list = new ArrayList<>();
        synchronized (NewHonor.DATA_LOCK) {
            NewHonor.plugin.playerUsingEffectCache.forEach((uuid, s) -> {
                if (s.equals(id)) {
                    list.add(uuid);
                }
            });
        }
        return list;
    }

    public static double getTimeDuration(LocalDateTime start) {
        return Duration.between(start, LocalDateTime.now()).getSeconds();
    }
}
