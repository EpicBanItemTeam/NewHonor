package com.github.euonmyoji.newhonor.configuration;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.util.HaloEffects;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yinyangshi
 */
public class EffectsData {
    private static final Path PATH = NewHonorConfig.cfgDir.resolve("EffectsData");
    private CommentedConfigurationNode cfg;
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    static {
        if (!Files.exists(PATH)) {
            try {
                Files.createDirectory(PATH);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public EffectsData(String id) {
        loader = HoconConfigurationLoader.builder()
                .setPath(PATH.resolve(id + ".conf")).build();
        reload();
    }

    public List<String> getEffectsList() throws ObjectMappingException {
        return cfg.getNode("effects").getList(TypeToken.of(String.class), ArrayList::new);
    }

    public HaloEffects getHaloEffectList() {
        return new HaloEffects(cfg);
    }

    public List<PotionEffect> getEffects() throws ObjectMappingException {
        List<PotionEffect> list = new ArrayList<>();
        getEffectsList().forEach(s -> {
            String[] args = s.split(",", 2);
            Sponge.getRegistry().getType(PotionEffectType.class, args[0]).ifPresent(type ->
                    list.add(PotionEffect.builder()
                            .potionType(type)
                            .amplifier(Integer.parseInt(args[1]))
                            .duration(60)
                            .build()));
        });
        return list;
    }

    public boolean remove(List<String> list) {
        cfg.getNode("effects").setValue(list);
        return save();
    }

    public boolean set(List<String> list, String s) {
        list.add(s);
        cfg.getNode("effects").setValue(list);
        return save();
    }

    private CommentedConfigurationNode load() {
        try {
            return loader.load();
        } catch (IOException e) {
            NewHonor.plugin.logger.error("EffectsData Config has error!", e);
            return loader.createEmptyNode(ConfigurationOptions.defaults());
        }
    }

    private void reload() {
        cfg = load();
    }

    private boolean save() {
        try {
            loader.save(cfg);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Path getPath(String id) {
        return PATH.resolve(id + ".conf");
    }

    public boolean anyMatchType(List<String> args, PotionEffectType type) {
        //noinspection ConstantConditions 出问题一定是有人不会用:D
        return args.stream()
                .map(s -> Sponge.getRegistry()
                        .getType(PotionEffectType.class, s.split(",", 2)[0]).get())
                .anyMatch(type::equals);
    }

    public static void refresh() {
        try {
            NewHonor.plugin.effectsCache.clear();
            List<String> createdEffects = getCreatedEffects();
            createdEffects.forEach(id -> {
                EffectsData ed = new EffectsData(id);
                try {
                    NewHonor.plugin.effectsCache.put(id, ed.getEffects());
                } catch (ObjectMappingException e) {
                    NewHonor.plugin.logger.error("refresh effects error", e);
                }
                NewHonor.plugin.haloEffectsCache.put(id, ed.getHaloEffectList());
            });
        } catch (IOException e) {
            NewHonor.plugin.logger.error("refresh effects error", e);
        }
    }

    public static List<String> getCreatedEffects() throws IOException {
        return Files.list(PATH).map(Path::getFileName).map(Path::toString).collect(Collectors.toList());
    }
}
