package com.github.euonmyoji.newhonor.configuration;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.util.*;

/**
 * @author yinyangshi
 */
public class PlayerData {
    private final UUID uuid;
    private final CommentedConfigurationNode cfg;
    private final TypeToken<String> type = TypeToken.of(String.class);
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    public UUID getUUID() {
        return this.uuid;
    }

    public PlayerData(User user) {
        uuid = user.getUniqueId();
        loader = HoconConfigurationLoader.builder()
                .setPath((NewHonorConfig.cfgDir.resolve("PlayerData")).resolve(user.getUniqueId().toString() + ".conf")).build();
        cfg = load();
    }

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        loader = HoconConfigurationLoader.builder()
                .setPath((NewHonorConfig.cfgDir.resolve("PlayerData")).resolve(uuid.toString() + ".conf")).build();
        cfg = load();
    }

    private CommentedConfigurationNode load() {
        try {
            return loader.load();
        } catch (IOException e) {
            return loader.createEmptyNode(ConfigurationOptions.defaults());
        }
    }

    public boolean isUseHonor() {
        return cfg.getNode("usehonor").getBoolean(true);
    }

    public boolean take(String... ids) {
        boolean took = false;
        for (String id : ids) {
            Optional<List<String>> honors = getHonors();
            if (honors.isPresent() && honors.get().stream().anyMatch(id::equals)) {
                honors.get().remove(id);
                cfg.getNode("honors").setValue(honors.get());
                took = true;
            }
        }
        return took && save();
    }

    public boolean give(String id) {
        Optional<List<String>> honors = getHonors();
        if (HonorData.getHonorText(id).isPresent() && honors.isPresent() && honors.get().stream().noneMatch(id::equals)) {
            honors.get().add(id);
            cfg.getNode("honors").setValue(honors.get());
            Sponge.getServer().getPlayer(uuid).map(Player::getName).ifPresent(name ->
                    HonorData.getGetMessage(id, name).ifPresent(Sponge.getServer().getBroadcastChannel()::send));
            return save();
        }
        return false;
    }

    public void usehonor(boolean use) {
        cfg.getNode("usehonor").setValue(use);
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
        if (hasHonor(id) && HonorData.getHonorText(id).isPresent()) {
            cfg.getNode("using").setValue(id);
            return save();
        }
        return false;
    }

    private boolean hasHonor(String id) {
        return getHonors().orElse(Collections.emptyList()).stream().anyMatch(s -> Objects.equals(s, id));
    }

    public String getUsingHonorID() {
        return cfg.getNode("using").getString();
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
        return Optional.ofNullable(getUsingHonorID()).flatMap(HonorData::getHonorText);
    }

    public boolean init() {
        Optional<List<String>> defaultHonors = NewHonorConfig.getDefaultOwnHonors();
        if (defaultHonors.isPresent()) {
            defaultHonors.get().forEach(this::give);
            return setUse(defaultHonors.get().get(0));
        }
        return true;
    }

    public void checkUsing() {
        if (!hasHonor(getUsingHonorID())) {
            Optional<List<String>> list = NewHonorConfig.getDefaultOwnHonors();
            if (list.isPresent()) {
                setUse(list.get().get(0));
            } else {
                setUse("");
            }
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
