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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author yinyangshi
 */
public class Util {
    public static final int INTERVAL_TICKS = 8;
    private static final int DEFAULT_DURATION_TICK = 60;
    private static final int PARALLEL_GOAL = 16;

    /**
     * 给玩家提供药水效果
     *
     * @param player        who
     * @param potionEffects effects?
     * @throws ConcurrentModificationException 同时修改了玩家数据
     */
    public static void offerEffects(Player player, List<PotionEffect> potionEffects) throws ConcurrentModificationException {
        PotionEffectData effects = player.getOrCreate(PotionEffectData.class).orElseThrow(NoSuchFieldError::new);
        potionEffects.forEach(effects::addElement);
        player.tryOffer(effects);
    }

    /**
     * 药水效果持续多久?
     *
     * @param cfg 一个包含potionEffectsDurationTick的节点
     * @return 持续多少tick
     */
    public static int getPotionEffectsDurationTick(CommentedConfigurationNode cfg) {
        return cfg.getNode("potionEffectsDurationTick").getInt(DEFAULT_DURATION_TICK);
    }

    /**
     * 获得药水效果s
     *
     * @param cfg  一个包含effects的node
     * @param tick 药水持续多少tick
     * @param show 是否显示粒子效果
     * @return 药水效果s
     * @throws ObjectMappingException 解析配置文件时出错
     */
    public static List<PotionEffect> getPotionEffects(CommentedConfigurationNode cfg, int tick, boolean show) throws ObjectMappingException {
        List<PotionEffect> list = new ArrayList<>();
        getEffectsList(cfg).forEach(s -> {
            String[] args = s.split(EffectsConfig.CONNECT_KEY, 2);
            Sponge.getRegistry().getType(PotionEffectType.class, args[0]).ifPresent(type ->
                    list.add(PotionEffect.builder()
                            .potionType(type)
                            .amplifier(Integer.parseInt(args[1]))
                            .duration(tick)
                            .particles(show)
                            .build()));
        });
        return list;
    }

    /**
     * 获得那个node里面的药水效果组节点
     *
     * @param cfg 那个node
     * @return 药水效果组节点
     * @throws ObjectMappingException 配置文件解析出错！
     */
    public static List<String> getEffectsList(CommentedConfigurationNode cfg) throws ObjectMappingException {
        return cfg.getNode("effects").getList(TypeToken.of(String.class), ArrayList::new);
    }

    public static <T> Stream<T> getStream(List<T> list) {
        return list.size() > PARALLEL_GOAL ? list.parallelStream() : list.stream();
    }

    public static List<UUID> getPlayerUsingEffects(String id) {
        List<UUID> list = new ArrayList<>();
        //不锁了 以免死锁
        new HashMap<>(NewHonor.plugin.playerUsingEffectCache).forEach((uuid, s) -> {
            if (s.equals(id)) {
                list.add(uuid);
            }
        });
        return list;
    }

    public static double getTimeDuration(LocalDateTime start) {
        return Duration.between(start, LocalDateTime.now()).getSeconds();
    }

    public static Text toText(String str) {
        return TextSerializers.FORMATTING_CODE.deserialize(str);
    }
}
