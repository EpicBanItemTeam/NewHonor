package com.github.euonmyoji.newhonor.configuration;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.data.HonorValueData;
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
public class HonorConfig {
    private static CommentedConfigurationNode cfg;
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static Set<String> allCreatedHonors;

    public static void init() {
        loader = HoconConfigurationLoader.builder()
                .setPath(NewHonorConfig.cfgDir.resolve("honor.conf")).build();
        cfg = load();
        NewHonorConfig.getDefaultOwnHonors().ifPresent(strings -> strings.forEach(id -> noSaveSet(id, cfg.getNode(id, "value").getString("[default]"))));
        cfg.removeChild("created-honors");
        save();
    }

    public static boolean setHonorEffects(String id, String effectsID) {
        cfg.getNode(id, "effects").setValue(effectsID);
        return save();
    }

    public static Optional<String> getEffectsID(String id) {
        return Optional.ofNullable(cfg.getNode(id, "effects").getString(null));
    }

    public static boolean addHonor(String id, String honor) {
        if (isVirtual(id)) {
            cfg.getNode(id, "value").setValue(honor);
            allCreatedHonors = getHonorsMap().keySet().stream().map(o -> (String) o).collect(Collectors.toSet());
            return save();
        }
        return false;
    }

    public static Optional<HonorValueData> getHonorValueData(String id) {
        return isVirtual(id) ? Optional.empty() : Optional.of(new HonorValueData(cfg.getNode(id)));
    }

    public static boolean setHonor(String id, String honor) {
        cfg.getNode(id, "value").setValue(honor);
        allCreatedHonors = getHonorsMap().keySet().stream().map(o -> (String) o).collect(Collectors.toSet());
        return save();
    }

    private static void noSaveSet(String id, String honor) {
        cfg.getNode(id, "value").setValue(honor);
    }

    public static boolean deleteHonor(String id) {
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
        allCreatedHonors = getHonorsMap().keySet().stream().map(o -> (String) o).collect(Collectors.toSet());
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
        if (allCreatedHonors == null) {
            allCreatedHonors = getHonorsMap().keySet().stream().map(o -> (String) o).collect(Collectors.toSet());
        }
        return allCreatedHonors;
    }

    public static boolean isVirtual(String id) {
        return cfg.getNode(id).isVirtual();
    }

    private static Map<Object, ? extends CommentedConfigurationNode> getHonorsMap() {
        return cfg.getChildrenMap();
    }

    static Optional<Text> getGetMessage(String id, String playername) {
        //noinspection ConstantConditions 之前有检查
        return Optional.ofNullable(cfg.getNode(id, "getMessage").getString(null))
                .map(s -> "&r" + s.replace("{playername}", playername))
                .map(s -> "&r" + s.replace("{newhonor}", HonorConfig.getHonorValueData(id).get().getRawValue() + "&r"))
                .map(TextSerializers.FORMATTING_CODE::deserialize);
    }
}
