package net.yeah.mungsoup.mung.configuration;

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
    public CommentedConfigurationNode config;
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    public MungConfig(Plugin plugin, String fileName, String fileType) throws IOException {
        Path dataFolder = plugin.getDataFolder().toPath();
        final String strip = "/";
        if (fileName.contains(strip)) {
            dataFolder = dataFolder.resolve(fileName.replaceAll("/.*.yml", ""));
        }
        Path path = Files.createDirectories(dataFolder).resolve(fileName + "." + fileType);
        loader = HoconConfigurationLoader.builder().setPath(path).build();
        if (Files.notExists(path)) {
            Files.createFile(path);
            reload();
            saveDefault();
            save();
        }
        reload();
    }

    protected void setDefault(String comment, Object defaultValue, String... node) {
        if (comment != null) {
            config.getNode((Object[]) node).setComment(comment).getValue(defaultValue);
            return;
        }
        config.getNode((Object[]) node).getValue(defaultValue);
    }

    public void saveDefault() {
    }

    public void reload() {
        try {
            config = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
        } catch (IOException e) {
            config = loader.createEmptyNode();
        }
    }

    public boolean save() {
        try {
            loader.save(config);
            return true;
        } catch (IOException e) {
            config = loader.createEmptyNode();
            return false;
        }
    }
}
