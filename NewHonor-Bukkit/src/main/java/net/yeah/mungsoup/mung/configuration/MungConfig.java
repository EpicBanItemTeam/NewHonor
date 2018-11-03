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
    public CommentedConfigurationNode cfg;
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    public MungConfig(Plugin plugin, String fileName) throws IOException {
        Path dataFolder = plugin.getDataFolder().toPath();
        final String strip = "/";
        if (fileName.contains(strip)) {
            String directory = fileName.replaceAll("/.*\\..*$", "/");
            dataFolder = dataFolder.resolve(directory);
            fileName = fileName.replace(directory, "");
        }
        Path path = Files.createDirectories(dataFolder).resolve(fileName);
        loader = HoconConfigurationLoader.builder().setPath(path).build();
        if (Files.notExists(path)) {
            Files.createFile(path);
            reload();
            saveDefault();
            save();
        }
        reload();
    }

    protected void setDefault(String comment, Object defaultValue, String... nodes) {
        CommentedConfigurationNode node = cfg.getNode(((Object[]) nodes));
        if (comment != null) {
            node.setComment(node.getComment().orElse(comment));
        }
        node.getValue(defaultValue);
    }

    public void saveDefault() {
    }

    public void reload() {
        try {
            cfg = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
        } catch (IOException e) {
            cfg = loader.createEmptyNode();
        }
    }

    protected boolean save() {
        try {
            loader.save(cfg);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            cfg = loader.createEmptyNode();
            return false;
        }
    }
}
