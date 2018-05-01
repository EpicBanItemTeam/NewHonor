package com.github.euonmyoji.newhonor.configuration;

import com.github.euonmyoji.newhonor.NewHonor;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author yinyangshi
 */
public class NewHonorConfig {
    private static CommentedConfigurationNode cfg;
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    public static Path cfgDir;
    public static Path defaultCfgDir;
    private static final String DATA_PATH_NODE = "data-dir-path";
    private static final String CHECK_UPDATE_NODE_PATH = "check-update";

    public static void init() {
        loader = HoconConfigurationLoader.builder()
                .setPath(defaultCfgDir.resolve("config.conf")).build();
        cfg = load();
        cfg.getNode(DATA_PATH_NODE).setValue(cfg.getNode(DATA_PATH_NODE).getString("default"));
        cfg.getNode(CHECK_UPDATE_NODE_PATH).setValue(cfg.getNode(CHECK_UPDATE_NODE_PATH).getBoolean(false));
        reload();
        save();
    }

    public static boolean isCheckUpdate() {
        return cfg.getNode(CHECK_UPDATE_NODE_PATH).getBoolean(false);
    }

    public static void reload() {
        cfg = load();
        String path = cfg.getNode(DATA_PATH_NODE).getString("default");
        cfgDir = "default".equals(path) ? defaultCfgDir : Paths.get(path);
        NewHonor.plugin.logger.info("目前正在使用的配置文件路径" + cfgDir);
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

    private static CommentedConfigurationNode load() {
        try {
            return loader.load();
        } catch (IOException e) {
            return loader.createEmptyNode(ConfigurationOptions.defaults());
        }
    }
}