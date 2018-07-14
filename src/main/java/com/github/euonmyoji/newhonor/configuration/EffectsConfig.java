package com.github.euonmyoji.newhonor.configuration;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.util.Util;
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
public class EffectsConfig {
    public static final String CONNECT_KEY = ",";
    public CommentedConfigurationNode cfg;
    private static final Path PATH = NewHonorConfig.cfgDir.resolve("EffectsData");
    private final ConfigurationLoader<CommentedConfigurationNode> loader;
    private final int potionEffectsTime;
    private String id;

    static {
        if (!Files.exists(PATH)) {
            try {
                Files.createDirectory(PATH);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public EffectsConfig(String id) {
        this.id = id;
        loader = HoconConfigurationLoader.builder()
                .setPath(PATH.resolve(id + ".conf")).build();
        reload();
        potionEffectsTime = Util.getPotionEffectsDurationTick(cfg.getNode("effects"));
    }

    public List<String> getEffectsList() throws ObjectMappingException {
        return Util.getEffectsList(cfg.getNode("effects"));
    }

    public List<PotionEffect> getEffects() throws ObjectMappingException {
        return cfg.getNode("effects").isVirtual() ? new ArrayList<>() : Util.getPotionEffects(cfg.getNode("effects"), potionEffectsTime, cfg.getNode("effects").getNode("show").getBoolean());
    }

    public boolean remove(List<String> list) {
        cfg.getNode("effects", "effects").setValue(list);
        return save();
    }

    public boolean set(List<String> list, String s) {
        list.add(s);
        cfg.getNode("effects", "effects").setValue(list);
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
                        .getType(PotionEffectType.class, s.split(CONNECT_KEY, 2)[0]).get())
                .anyMatch(type::equals);
    }

    public static List<String> getCreatedEffects() throws IOException {
        return Files.list(PATH).map(Path::getFileName).map(Path::toString).map(s -> s.replace(".conf", "")).collect(Collectors.toList());
    }

    public String getId() {
        return this.id;
    }
}
