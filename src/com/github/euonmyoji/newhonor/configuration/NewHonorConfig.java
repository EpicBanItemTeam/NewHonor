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
    public static Path playerdataPath;

    static {
        loader = HoconConfigurationLoader.builder()
                .setPath(NewHonor.plugin.cfgDir.resolve("config.conf")).build();
        cfg = load();
        cfg.getNode("player-data-path").getValue(cfg.getNode("player-data-path").getString("default"));
        reload();
        save();
    }

    public static void reload() {
        String path = cfg.getNode("player-data-path").getString("default");
        playerdataPath = path.equals("default") ? NewHonor.plugin.cfgDir.resolve("PlayerData") : Paths.get(path);
        NewHonor.plugin.logger.info("目前正在使用的玩家爱数据路径" + playerdataPath);
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