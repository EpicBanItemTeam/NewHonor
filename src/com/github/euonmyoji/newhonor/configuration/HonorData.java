package com.github.euonmyoji.newhonor.configuration;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class HonorData {
    private static CommentedConfigurationNode cfg;
    private static ConfigurationLoader<CommentedConfigurationNode> loader;

    static {
        loader = HoconConfigurationLoader.builder()
                .setPath(NewHonorConfig.cfgDir.resolve("honor.conf")).build();
        cfg = load();
        set("default", cfg.getNode("default", "value").getString("[默认头衔]"));
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
            check(id);
            return save();
        }
        return false;
    }

    public static Optional<Text> getHonorText(String id) {
        return Optional.ofNullable(cfg.getNode(id, "value").getString(null)).map(TextSerializers.FORMATTING_CODE::deserializeUnchecked);
    }

    public static boolean set(String id, String honor) {
        cfg.getNode(id, "value").setValue(honor);
        check(id);
        return save();
    }

    public static boolean delete(String id) {
        checkDelete(id);
        return cfg.removeChild(id) && save();
    }

    private static CommentedConfigurationNode load() {
        try {
            return loader.load();
        } catch (IOException e) {
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

    public static List<String> getAllCreatedHonors() throws ObjectMappingException {
        return cfg.getNode("created-honors").getList(TypeToken.of(String.class), ArrayList::new);
    }

    static Optional<Text> getGetMessage(String id, String playername) {
        //noinspection ConstantConditions 之前有检查
        return Optional.ofNullable(cfg.getNode(id, "getMessage").getString(null))
                .map(s -> "&f" + s.replace("{playername}", playername))
                .map(s -> "&f" + s.replace("{newhonor}", TextSerializers.FORMATTING_CODE.serialize(HonorData.getHonorText(id).get())) + "&f")
                .map(TextSerializers.FORMATTING_CODE::deserialize);
    }

    public static boolean check(List<String> honors) {
        try {
            List<String> list = getAllCreatedHonors();
            list.addAll(honors);
            list = list.stream().filter(s -> !HonorData.isVirtual(s))
                    .distinct()
                    .collect(Collectors.toList());
            cfg.getNode("created-honors").setValue(new TypeToken<List<String>>() {
            }, list);
            return save();
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void check(String... strs) {
        try {
            List<String> list = getAllCreatedHonors();
            for (String str : strs) {
                if (!list.contains(str) && !isVirtual(str)) {
                    list.add(str);
                }
            }
            cfg.getNode("created-honors").setValue(new TypeToken<List<String>>() {
            }, list);
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    private static void checkDelete(String honor) {
        try {
            List<String> list = getAllCreatedHonors();
            if (list.contains(honor)) {
                list.remove(honor);
                cfg.getNode("created-honors").setValue(new TypeToken<List<String>>() {
                }, list);
            }
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    private static boolean isVirtual(String id) {
        return cfg.getNode(id).isVirtual();
    }
}
