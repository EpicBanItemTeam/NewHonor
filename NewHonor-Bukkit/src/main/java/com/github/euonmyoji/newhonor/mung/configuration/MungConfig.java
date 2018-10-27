package com.github.euonmyoji.newhonor.mung.configuration;

import com.github.euonmyoji.newhonor.exception.ConfigException;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author MungSoup
 */
public class MungConfig {
    private CommentedConfigurationNode config;
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    public MungConfig(Plugin plugin, String fileName, String fileType) throws IOException {
        Path dataFolder = plugin.getDataFolder().toPath();
        final String strip = "/";
        if (fileName.contains(strip)) {
            dataFolder = dataFolder.resolve(fileName.replaceAll("/.*.yml", ""));
        }
        Path path = Files.createDirectories(dataFolder).resolve(fileName + "." + fileType);
        if (Files.notExists(path)) {
            Files.createFile(path);
        }
        loader = HoconConfigurationLoader.builder().setPath(path).build();
        reload();
        saveDefault();
        save();
    }

    public void saveDefault() {
    }

    protected void setDefault(String node, String defaultValue) {
        config.getNode(node).getString(defaultValue);
    }

    protected String getString(String node, String... defaultValue) {
        CommentedConfigurationNode configurationNode = config.getNode(node);
        if (configurationNode.getString() == null) {
            try {
                throw new ConfigException("配置文件格式错误, 无法找到" + node + "的值");
            } catch (ConfigException e) {
                e.printStackTrace();
            }
            return defaultValue[0];
        }
        return configurationNode.getString();
    }

    public void reload() {
        try {
            config = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
        } catch (IOException e) {
            config = loader.createEmptyNode();
        }
    }

    private void save() {
        try {
            loader.save(config);
        } catch (IOException e) {
            config = loader.createEmptyNode();
        }
    }
}
