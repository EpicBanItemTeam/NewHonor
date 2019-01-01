package com.github.euonmyoji.newhonor.data;

import com.flowpowered.math.vector.Vector3d;
import com.github.euonmyoji.newhonor.NewHonor;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static sun.misc.FloatingDecimal.parseDouble;

/**
 * @author yinyangshi
 */
public class ParticleEffectData {
    public static final String PARTICLES_KEY = "particles";
    private int radiusSquared;
    private List<Consumer<Location<World>>> consumers = new ArrayList<>();

    public ParticleEffectData(CommentedConfigurationNode cfg, String id) {
        cfg.getChildrenMap().forEach((name, node) -> {
            ParticleType type = Optional.ofNullable(node.getNode("type").getString())
                    .flatMap(s -> Sponge.getRegistry().getType(ParticleType.class, s))
                    .orElse(null);
            if (type == null) {
                NewHonor.logger.warn("The effects group: {} ParticleEffect {} type is null", id, name);
                return;
            }
            final String nullValue = "null";
            String[] velocity = node.getNode("velocity").getString("0,0,0").split(",", 3);
            String[] offset = node.getNode("offset").getString("0,0,0").split(",", 3);
            String[] color = node.getNode("color").getString(nullValue).split(",", 3);
            final Vector3d locationOffset;
            {
                String s = node.getNode("locationOffset").getString();
                if (s != null) {
                    String[] arg = s.split(",", 3);
                    locationOffset = new Vector3d(parseDouble(arg[0]), parseDouble(arg[1]), parseDouble(arg[2]));
                } else {
                    locationOffset = null;
                }
            }
            int quantity = node.getNode("quantity").getInt(1);
            radiusSquared = node.getNode("radius").getInt(-1);
            radiusSquared *= radiusSquared;

            ParticleEffect.Builder builder = ParticleEffect.builder()
                    .type(type)
                    .velocity(new Vector3d(parseDouble(velocity[0]), parseDouble(velocity[1]), parseDouble(velocity[2])))
                    .offset(new Vector3d(parseDouble(offset[0]), parseDouble(offset[1]), parseDouble(offset[2])))
                    .quantity(quantity);

            try {
                BlockState blockState = node.getNode("block-state").getValue(TypeToken.of(BlockState.class));
                if (blockState != null) {
                    builder.option(ParticleOptions.BLOCK_STATE, blockState);
                }
            } catch (ObjectMappingException e) {
                NewHonor.logger.warn("The effects group: {} ParticleEffect {} blockState is error", id, name);
            }
            if (!nullValue.equals(color[0])) {
                builder.option(ParticleOptions.COLOR, Color.ofRgb(Integer.valueOf(color[0]), Integer.valueOf(color[1]), Integer.valueOf(color[2])));
            }
            if (type.equals(ParticleTypes.SPLASH_POTION)) {
                try {
                    builder.option(ParticleOptions.POTION_EFFECT_TYPE, Optional.ofNullable(node.getNode("potion-type").getString())
                            .flatMap(s -> Sponge.getRegistry().getType(PotionEffectType.class, s)).orElseThrow(NoSuchFieldException::new));
                } catch (NoSuchFieldException e) {
                    NewHonor.logger.warn("The effects group: {} ParticleEffect {} potion-type is null", id, name);
                }
            }

            ///build build build build build
            ParticleEffect particle = builder.build();
            //如果没有指定半径则用无半径方法 or else
            if (radiusSquared > 0) {
                if (locationOffset != null) {
                    consumers.add(o -> o.getExtent().getPlayers()
                            .forEach(player -> {
                                if (player.getLocation().getPosition().distanceSquared(o.getPosition()) <= radiusSquared) {
                                    player.spawnParticles(particle, o.getPosition().add(locationOffset));
                                }
                            }));
                } else {
                    consumers.add(o -> o.getExtent().getPlayers()
                            .forEach(player -> {
                                if (player.getLocation().getPosition().distanceSquared(o.getPosition()) <= radiusSquared) {
                                    player.spawnParticles(particle, o.getPosition());
                                }
                            }));
                }
            } else {
                if (locationOffset != null) {
                    consumers.add(o -> o.getExtent().getPlayers()
                            .forEach(player -> player.spawnParticles(particle, o.getPosition().add(locationOffset))));
                } else {
                    consumers.add(o -> o.getExtent().getPlayers()
                            .forEach(player -> player.spawnParticles(particle, o.getPosition())));
                }
            }
        });
    }

    /**
     * 以这个位置参数原点执行粒子效果
     *
     * @param o 原点
     */
    public void execute(Location<World> o) {
        consumers.forEach(consumer -> consumer.accept(o));
    }
}
