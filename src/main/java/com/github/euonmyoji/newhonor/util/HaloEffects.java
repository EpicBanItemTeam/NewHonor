package com.github.euonmyoji.newhonor.util;

import com.github.euonmyoji.newhonor.NewHonor;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.omg.CORBA.UNKNOWN;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author yinyangshi
 */
public class HaloEffects {
    private final HashMap<Double, List<PotionEffect>> cache = new HashMap<>();

    public HaloEffects(CommentedConfigurationNode config) {
        config.getChildrenMap().forEach((string, cfg) -> {
            try {
                double distance;
                try {
                    distance = Double.parseDouble(((String) string).replaceAll("[a-z]|[A-Z]", ""));
                } catch (Exception ignore) {
                    return;
                }
                List<String> effects = getEffectsList(cfg);
                cache.put(distance, effects.stream()
                        .map(s -> {
                            String[] args = s.split(",", 2);
                            return Sponge.getRegistry().getType(PotionEffectType.class, args[0]).map(type ->
                                    PotionEffect.builder()
                                            .potionType(type)
                                            .amplifier(Integer.parseInt(args[1]))
                                            .duration(40)
                                            .build());
                        })
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList()));
            } catch (Exception e) {
                NewHonor.plugin.logger.error("error while parsing effects cfg", e);
            }
        });
    }

    public void execute(Player p) {
        Task.builder().execute(() ->
                cache.forEach((distance, potionEffects) -> p.getNearbyEntities(distance).stream()
                        .filter(entity -> entity instanceof Player)
                        .forEach(entity -> {
                            Player player = ((Player) entity);
                            PotionEffectData effects = player.getOrCreate(PotionEffectData.class).orElseThrow(UNKNOWN::new);
                            potionEffects.forEach(effects::addElement);
                            player.offer(effects);
                        })))
                .name("NewHonor - give" + p.getName() + "NearByEntity Buff").submit(NewHonor.plugin);
    }

    private static List<String> getEffectsList(CommentedConfigurationNode cfg) throws ObjectMappingException {
        return cfg.getNode("effects").getList(TypeToken.of(String.class), ArrayList::new);
    }
}
