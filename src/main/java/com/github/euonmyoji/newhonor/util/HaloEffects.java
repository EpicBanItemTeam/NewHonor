package com.github.euonmyoji.newhonor.util;

import com.flowpowered.math.vector.Vector3d;
import com.github.euonmyoji.newhonor.NewHonor;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.entity.living.player.Player;

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
        int time = config.getNode("potionEffectsDurationTick").getInt(60);
        config.getNode("halo").getChildrenMap().values().forEach(cfg -> {
            try {
                double distance = cfg.getNode("radius").getDouble(-1);
                if (distance == -1) {
                    throw new ObjectMappingException("the effects radius is unknown!");
                }
                List<String> effects = getEffectsList(cfg);
                cache.put(distance, effects.stream()
                        .map(s -> {
                            String[] args = s.split(",", 2);
                            return Sponge.getRegistry().getType(PotionEffectType.class, args[0]).map(type ->
                                    PotionEffect.builder()
                                            .potionType(type)
                                            .amplifier(Integer.parseInt(args[1]))
                                            .duration(time)
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
        final Vector3d o = p.getLocation().getPosition();
        p.getWorld().getPlayers().forEach(player -> {
            double distanceSquared = player.getLocation().getPosition().distanceSquared(o);
            cache.forEach((distanceLimit, potionEffects) -> {
                if (!p.getUniqueId().equals(player.getUniqueId()) && distanceSquared < (distanceLimit * distanceLimit)) {
                    PotionEffectData effects = player.getOrCreate(PotionEffectData.class).orElseThrow(NoSuchFieldError::new);
                    potionEffects.forEach(effects::addElement);
                    player.offer(effects);
                }
            });
        });
    }

    private static List<String> getEffectsList(CommentedConfigurationNode cfg) throws ObjectMappingException {
        return cfg.getNode("effects").getList(TypeToken.of(String.class), ArrayList::new);
    }
}
