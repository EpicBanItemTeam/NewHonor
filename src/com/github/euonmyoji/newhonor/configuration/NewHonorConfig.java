package com.github.euonmyoji.newhonor.configuration;

import com.github.euonmyoji.newhonor.NewHonor;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NewHonorConfig {
    private static CommentedConfigurationNode cfg;
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    public static Path cfgDir;
    public static Path defaultCfgDir;
    private static final String cfgNodePath = "config-path";

    public static void init() {
        loader = HoconConfigurationLoader.builder()
                .setPath(defaultCfgDir.resolve("config.conf")).build();
        cfg = load();
        cfg.getNode(cfgNodePath).getValue(cfg.getNode(cfgNodePath).getString("default"));
        reload();
        save();
    }

    public static void reload() {
        cfg = load();
        String path = cfg.getNode(cfgNodePath).getString("default");
        cfgDir = path.equals("default") ? defaultCfgDir : Paths.get(path);
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