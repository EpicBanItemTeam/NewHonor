package com.github.euonmyoji.newhonor.configuration;

import com.github.euonmyoji.newhonor.NewHonor;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yinyangshi
 */
public class HonorData {
    private static CommentedConfigurationNode cfg;
    private static ConfigurationLoader<CommentedConfigurationNode> loader;

    static {
        loader = HoconConfigurationLoader.builder()
                .setPath(NewHonorConfig.cfgDir.resolve("honor.conf")).build();
        cfg = load();
        set("default", cfg.getNode("default", "value").getString("[default]"));
        cfg.removeChild("created-honors");
        save();
    }

    public static boolean effects(String id, String effectsID) {
        cfg.getNode(id, "effects").setValue(effectsID);
        return save();
    }

    public static Optional<String> getEffectsID(String id) {
        return Optional.ofNullable(cfg.getNode(id, "effects").getString(null));
    }

    public static boolean add(String id, String honor) {
        if (isVirtual(id)) {
            cfg.getNode(id, "value").setValue(honor);
            return save();
        }
        return false;
    }

    public static Optional<Text> getHonorText(String id) {
        return getHonorRawText(id).map(TextSerializers.FORMATTING_CODE::deserialize);
    }

    public static Optional<String> getHonorRawText(String id) {
        return Optional.ofNullable(cfg.getNode(id, "value").getString(null));
    }

    public static boolean set(String id, String honor) {
        cfg.getNode(id, "value").setValue(honor);
        return save();
    }

    public static boolean delete(String id) {
        return cfg.removeChild(id) && save();
    }

    private static CommentedConfigurationNode load() {
        try {
            return loader.load();
        } catch (IOException e) {
            NewHonor.plugin.logger.error("HonorData Config has error!", e);
            return loader.createEmptyNode(ConfigurationOptions.defaults());
        }
    }

    public static void reload() {
        cfg = load();
    }

    private static boolean save() {
        try {
            loader.save(cfg);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Set<String> getAllCreatedHonors() {
        return getHonorsMap().keySet().stream().map(o -> (String) o).collect(Collectors.toSet());
    }

    private static Map<Object, ? extends CommentedConfigurationNode> getHonorsMap() {
        return cfg.getChildrenMap();
    }

    static Optional<Text> getGetMessage(String id, String playername) {
        //noinspection ConstantConditions 之前有检查
        return Optional.ofNullable(cfg.getNode(id, "getMessage").getString(null))
                .map(s -> "&r" + s.replace("{playername}", playername))
                .map(s -> "&r" + s.replace("{newhonor}", TextSerializers.FORMATTING_CODE.serialize(HonorData.getHonorText(id).get())) + "&f")
                .map(TextSerializers.FORMATTING_CODE::deserialize);
    }

    private static boolean isVirtual(String id) {
        return cfg.getNode(id).isVirtual();
    }
}
