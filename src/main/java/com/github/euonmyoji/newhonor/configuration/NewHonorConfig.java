package com.github.euonmyoji.newhonor.configuration;

import com.github.euonmyoji.newhonor.NewHonor;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author yinyangshi
 */
public class NewHonorConfig {
    private static final TypeToken<List<String>> LIST_STRING_TYPE = new TypeToken<List<String>>() {
    };
    private static CommentedConfigurationNode cfg;
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    public static Path cfgDir;
    public static Path defaultCfgDir;
    private static final String DATA_PATH_NODE = "data-dir-path";
    private static final String CHECK_UPDATE_NODE_PATH = "check-update";
    private static final String LANGUAGE = "lang";
    private static final String DEFAULT_HONORS = "default-honors";

    public static void init() {
        loader = HoconConfigurationLoader.builder()
                .setPath(defaultCfgDir.resolve("config.conf")).build();
        cfg = load();
        cfg.getNode(DATA_PATH_NODE).setValue(cfg.getNode(DATA_PATH_NODE).getString("default"));
        cfg.getNode(CHECK_UPDATE_NODE_PATH).setValue(cfg.getNode(CHECK_UPDATE_NODE_PATH).getBoolean(false));
        cfg.getNode(LANGUAGE).setValue(cfg.getNode(LANGUAGE).getString(Locale.getDefault().toString()));
        try {
            cfg.getNode(DEFAULT_HONORS).setValue(LIST_STRING_TYPE, getDefaultOwnHonors().orElseGet(() -> new ArrayList<String>() {{
                add("default");
            }}));
        } catch (ObjectMappingException e) {
            NewHonor.plugin.logger.error("default honor is error!", e);
        }
        save();
        reload();
    }

    static Locale getUsingLang() {
        String[] args = cfg.getNode(LANGUAGE).getString(Locale.getDefault().toString()).split("_", 2);
        return new Locale(args[0], args[1]);
    }

    public static boolean isCheckUpdate() {
        return cfg.getNode(CHECK_UPDATE_NODE_PATH).getBoolean(false);
    }

    public static void reload() {
        cfg = load();
        String path = cfg.getNode(DATA_PATH_NODE).getString("default");
        cfgDir = "default".equals(path) ? defaultCfgDir : Paths.get(path);
        NewHonor.plugin.logger.info("using data dir path:" + cfgDir);
    }

    public static CommentedConfigurationNode getCfg() {
        return cfg;
    }


    public static void save() {
        try {
            loader.save(cfg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Optional<List<String>> getDefaultOwnHonors() {
        try {
            return Optional.ofNullable(cfg.getNode(DEFAULT_HONORS).getValue(LIST_STRING_TYPE));
        } catch (ObjectMappingException e) {
            NewHonor.plugin.logger.error("default own honor is error!", e);
            return Optional.of(Collections.emptyList());
        }
    }

    private static CommentedConfigurationNode load() {
        try {
            return loader.load();
        } catch (IOException e) {
            return loader.createEmptyNode(ConfigurationOptions.defaults());
        }
    }
}