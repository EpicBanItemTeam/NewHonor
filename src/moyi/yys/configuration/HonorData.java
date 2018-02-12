package moyi.yys.configuration;

import moyi.yys.NewHonor;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.util.Optional;

public class HonorData {
    private CommentedConfigurationNode cfg;
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    public HonorData() {
        loader = HoconConfigurationLoader.builder()
                .setPath(NewHonor.plugin.cfgDir.resolve("honor.conf")).build();
        cfg = load();
        set("default", cfg.getNode("default").getString("[默认头衔]"));
    }

    public boolean add(String id, String honor) {
        if (cfg.getNode(id).isVirtual()) {
            cfg.getNode(id).setValue(honor);
            return save();
        }
        return false;
    }

    public Optional<Text> getHonor(String id) {
        return Optional.ofNullable(cfg.getNode(id).getString(null)).map(TextSerializers.FORMATTING_CODE::deserializeUnchecked);
    }

    public boolean set(String id, String honor) {
        cfg.getNode(id).setValue(honor);
        return save();
    }

    public boolean delete(String id) {
        return cfg.removeChild(id) && save();
    }

    private CommentedConfigurationNode load() {
        try {
            return loader.load();
        } catch (IOException e) {
            return loader.createEmptyNode(ConfigurationOptions.defaults());
        }
    }

    public void reload() {
        cfg = load();
    }

    private boolean save() {
        try {
            loader.save(cfg);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
