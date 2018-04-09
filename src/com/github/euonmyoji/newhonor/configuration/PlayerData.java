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
import java.util.function.Consumer;

public class PlayerData {
    private final UUID uuid;
    private final CommentedConfigurationNode cfg;
    private final TypeToken<String> type = new TypeToken<String>() {
    };
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    public UUID getUUID() {
        return this.uuid;
    }

    public PlayerData(User user) {
        uuid = user.getUniqueId();
        loader = HoconConfigurationLoader.builder()
                .setPath((NewHonor.plugin.cfgDir.resolve("PlayerData")).resolve(user.getUniqueId().toString() + ".conf")).build();
        cfg = load();
    }

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
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
        if (HonorData.getHonorText(id).isPresent() && honors.isPresent() && honors.get().stream().noneMatch(id::equals)) {
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

    public void displayhonor(boolean display) {
        cfg.getNode("displayhonor").setValue(display);
        save();
    }

    public void enableEffects(boolean enable) {
        cfg.getNode("enableEffects").setValue(enable);
        save();
    }

    public boolean isEnableEffects() {
        return cfg.getNode("enableEffects").getBoolean(true);
    }

    public boolean setUse(String id) {
        if ((getHonors().orElse(Collections.emptyList()).stream().anyMatch(id::equals) || id.equals("default")) && HonorData.getHonorText(id).isPresent()) {
            cfg.getNode("using").setValue(id);
            return save();
        }
        return false;
    }

    public String getUse() {
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

    private Optional<Text> getHonor() {
        return Optional.ofNullable(cfg.getNode("using").getString("default")).flatMap(HonorData::getHonorText);
    }

    public boolean init() {
        give("default");
        setUse(getUse());
        return save();
    }

    public PlayerData ifShowHonor(Consumer<Optional<Text>> f) {
        if (isShowHonor()) {
            f.accept(getHonor());
        }
        return this;
    }

    public void orElse(Runnable r) {
        if (!isShowHonor()) {
            r.run();
        }
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
