package com.github.euonmyoji.newhonor.data;

import com.flowpowered.math.vector.Vector3d;
import com.github.euonmyoji.newhonor.NewHonor;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author yinyangshi
 */
public class ParticleEffectData {
    public static final String PARTICLES_KEY = "particles";
    private int radius;
    private List<Consumer<Location<World>>> consumers = new ArrayList<>();

    public ParticleEffectData(CommentedConfigurationNode cfg, String id) {
        cfg.getChildrenMap().forEach((name, node) -> {
            ParticleType type = Optional.ofNullable(node.getNode("type").getString())
                    .flatMap(s -> Sponge.getRegistry().getType(ParticleType.class, s))
                    .orElse(null);
            if (type == null) {
                NewHonor.plugin.logger.warn("The effects group: {} ParticleEffect {} type is null", id, name);
                return;
            }
            String[] velocity = node.getNode("velocity").getString("0,0,0").split(",", 3);
            String[] offset = node.getNode("offset").getString("0,0,0").split(",", 3);
            int quantity = node.getNode("quantity").getInt(1);
            radius = node.getNode("radius").getInt(-1);
            ParticleEffect particle = ParticleEffect.builder()
                    .type(type)
                    .velocity(new Vector3d(Double.valueOf(velocity[0]), Double.valueOf(velocity[1]), Double.valueOf(velocity[2])))
                    .offset(new Vector3d(Double.valueOf(offset[0]), Double.valueOf(offset[1]), Double.valueOf(offset[2])))
                    .quantity(quantity)
                    .build();
            //如果没有指定半径则用无半径方法 or else
            consumers.add(radius > 0 ? (o -> o.getExtent().getPlayers()
                    .forEach(player -> player.spawnParticles(particle, o.getPosition(), radius))) : (o -> o.getExtent().getPlayers()
                    .forEach(player -> player.spawnParticles(particle, o.getPosition()))));
        });
    }

    public void execute(Location<World> o) {
        consumers.forEach(consumer -> consumer.accept(o));
    }
}
