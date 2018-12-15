package com.github.euonmyoji.newhonor.configuration;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.data.HonorData;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.util.*;

/**
 * @author yinyangshi
 */
public class HonorConfig {
    private static CommentedConfigurationNode cfg;
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static HashMap<String, HonorData> valueMap = new HashMap<>();

    private HonorConfig() {
        throw new UnsupportedOperationException();
    }

    public static void init() {
        loader = HoconConfigurationLoader.builder()
                .setPath(PluginConfig.cfgDir.resolve("honor.conf")).build();
        cfg = load();
        List<String> list = PluginConfig.getDefaultOwnHonors();
        if (list != null) {
            list.forEach(id -> noSaveSet(id, cfg.getNode(id, "value").getString("[default]")));
        }
        cfg.removeChild("created-honors");

        save();
        reload();
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
            valueMap.put(id, new HonorData(cfg.getNode(id), id));
            return save();
        }
        return false;
    }

    public static HonorData getHonorData(String id) {
        return valueMap.get(id);
    }

    public static boolean setHonor(String id, String honor) {
        cfg.getNode(id, "value").setValue(honor);
        valueMap.put(id, new HonorData(cfg.getNode(id), id));
        return save();
    }

    private static void noSaveSet(String id, String honor) {
        cfg.getNode(id, "value").setValue(honor);
    }

    public static boolean deleteHonor(String id) {
        valueMap.remove(id);
        return cfg.removeChild(id) && save();
    }

    private static CommentedConfigurationNode load() {
        try {
            return loader.load();
        } catch (IOException e) {
            NewHonor.logger.error("load Honor Config error!", e);
            return cfg;
        }
    }

    public static void reload() {
        cfg = load();
        valueMap.clear();
        getHonorsMap().forEach((o, o2) -> valueMap.put(o.toString(), new HonorData(o2, o.toString())));
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
        if (valueMap == null || valueMap.isEmpty()) {
            Map<Object, ? extends CommentedConfigurationNode> honorsMap = getHonorsMap();
            valueMap = new HashMap<>(honorsMap.size());
            honorsMap.forEach((o, o2) -> valueMap.put(o.toString(), new HonorData(o2, o.toString())));
        }
        return valueMap.keySet();
    }

    public static boolean isVirtual(String id) {
        return cfg.getNode(id).isVirtual();
    }

    private static Map<Object, ? extends CommentedConfigurationNode> getHonorsMap() {
        return cfg.getChildrenMap();
    }

    public static Optional<Text> getGetMessage(String id, String playername) {
        return Optional.ofNullable(cfg.getNode(id, "getMessage").getString(null))
                .map(s -> "&r" + s.replace("{playername}", playername))
                .map(s -> "&r" + s.replace("{newhonor}", HonorConfig.getHonorData(id).getStrValue() + "&r"))
                .map(TextSerializers.FORMATTING_CODE::deserialize);
    }
}
