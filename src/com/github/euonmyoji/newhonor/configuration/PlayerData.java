package com.github.euonmyoji.newhonor.configuration;

import com.github.euonmyoji.newhonor.NewHonor;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.util.*;

public class PlayerData {
    private final CommentedConfigurationNode cfg;
    private final TypeToken<String> type = new TypeToken<String>() {
    };
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    public PlayerData(User user) {
        loader = HoconConfigurationLoader.builder()
                .setPath((NewHonor.plugin.cfgDir.resolve("PlayerData")).resolve(user.getUniqueId().toString() + ".conf")).build();
        cfg = load();
    }

    public PlayerData(UUID uuid) {
        loader = HoconConfigurationLoader.builder()
                .setPath((NewHonor.plugin.cfgDir.resolve("PlayerData")).resolve(uuid.toString() + ".conf")).build();
        cfg = load();
    }

    private CommentedConfigurationNode load() {
        try {
            return loader.load();
        } catch (IOException e) {
            return loader.createEmptyNode(ConfigurationOptions.defaults());
        }
    }

    public boolean isShowHonor() {
        return cfg.getNode("showhonor").getBoolean(true);
    }

    public boolean take(String id) {
        Optional<List<String>> honors = getHonors();
        if (honors.isPresent() && honors.get().stream().anyMatch(id::equals)) {
            honors.get().remove(id);
            cfg.getNode("honors").setValue(honors.get());
            return save();
        }
        return false;
    }

    public boolean give(String id) {
        Optional<List<String>> honors = getHonors();
        if (HonorData.getHonor(id).isPresent() && honors.isPresent() && honors.get().stream().noneMatch(id::equals)) {
            honors.get().add(id);
            cfg.getNode("honors").setValue(honors.get());
            return save();
        }
        return false;
    }

    public void showhonor(boolean show) {
        cfg.getNode("showhonor").setValue(show);
        save();
    }

    public void displayhonor(boolean show) {
        cfg.getNode("displayhonor").setValue(show);
        save();
    }

    public boolean setUse(String id) {
        if ((getHonors().orElse(Collections.emptyList()).stream().anyMatch(id::equals) || id.equals("default")) && HonorData.getHonor(id).isPresent()) {
            cfg.getNode("using").setValue(id);
            return save();
        }
        return false;
    }

    private String getUse() {
        return cfg.getNode("using").getString("default");
    }

    public Optional<List<String>> getHonors() {
        try {
            return Optional.of(cfg.getNode("honors").getList(type, ArrayList::new));
        } catch (ObjectMappingException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<Text> getHonor() {
        return Optional.ofNullable(cfg.getNode("using").getString("default")).flatMap(HonorData::getHonor);
    }

    public boolean init() {
        give("default");
        setUse(getUse());
        return save();
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
