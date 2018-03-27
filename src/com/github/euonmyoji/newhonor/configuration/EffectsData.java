package com.github.euonmyoji.newhonor.configuration;

import com.github.euonmyoji.newhonor.NewHonor;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class EffectsData {
    private static final Path path = NewHonor.plugin.cfgDir.resolve("EffectsData");
    private CommentedConfigurationNode cfg;
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    static {
        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public EffectsData(String id) {
        loader = HoconConfigurationLoader.builder()
                .setPath(path.resolve(id + ".conf")).build();
        reload();
    }

    @SuppressWarnings("SpellCheckingInspection")
    public List<PotionEffect> getEffects() throws ObjectMappingException {
        //TODO: here is a Exception[wait for USTC_ZZZZ]
        return cfg.getNode("effects").getList(TypeToken.of(PotionEffect.class), ArrayList::new);
    }

    public boolean remove(List<PotionEffect> list) {
        cfg.getNode("effects").setValue(list);
        return save();
    }

    public boolean set(PotionEffectType type, int level) {
        try {
            List<PotionEffect> list = getEffects();
            new ArrayList<>(list).stream().filter(type::equals).forEach(list::remove);
            PotionEffect effect = PotionEffect.builder()
                    .potionType(type).amplifier(level).duration(100).build();
            list.add(effect);
            cfg.getNode("effects").setValue(new TypeToken<List<PotionEffect>>() {
            }, list);
            return save();
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
        return false;
    }

    private CommentedConfigurationNode load() {
        try {
            return loader.load();
        } catch (IOException e) {
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
        return path.resolve(id + ".conf");
    }

    public static void refresh() {
        try {
            NewHonor.effectsCache.clear();
            Files.list(path).forEach(path -> {
                String id = "" + path.getFileName();
                EffectsData ed = new EffectsData(id);
                try {
                    NewHonor.effectsCache.put(id, ed.getEffects());
                } catch (ObjectMappingException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
