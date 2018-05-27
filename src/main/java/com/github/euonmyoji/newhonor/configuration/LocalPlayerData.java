package com.github.euonmyoji.newhonor.configuration;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.event.PlayerGetHonorEvent;
import com.github.euonmyoji.newhonor.api.event.PlayerLoseHonorEvent;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;

import java.io.IOException;
import java.util.*;

/**
 * @author yinyangshi
 */
public class LocalPlayerData implements PlayerData {
    private final UUID uuid;
    private final CommentedConfigurationNode cfg;
    private final TypeToken<String> type = TypeToken.of(String.class);
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    LocalPlayerData(UUID uuid) {
        this.uuid = uuid;
        loader = HoconConfigurationLoader.builder()
                .setPath((NewHonorConfig.cfgDir.resolve("PlayerData")).resolve(uuid.toString() + ".conf")).build();
        cfg = load();
    }

    @Override
    public void init() {
        Optional<List<String>> defaultHonors = NewHonorConfig.getDefaultOwnHonors();
        if (defaultHonors.isPresent()) {
            defaultHonors.get().forEach(this::noSaveGive);
            if (save()) {
                setUseHonor(cfg.getNode("using").getString(defaultHonors.get().get(0)));
            }
        }
    }

    @Override
    public boolean isUseHonor() {
        return cfg.getNode("usehonor").getBoolean(true);
    }

    @Override
    public boolean takeHonor(String... ids) {
        boolean took = false;
        for (String id : ids) {
            Optional<List<String>> honors = getOwnHonors();
            if (honors.isPresent() && honors.get().stream().anyMatch(id::equals)) {
                honors.get().remove(id);
                cfg.getNode("honors").setValue(honors.get());
                took = true;
            }
        }
        PlayerLoseHonorEvent event = new PlayerLoseHonorEvent(Cause.builder().append(NewHonor.plugin).build(EventContext.empty()), uuid, ids);
        Sponge.getEventManager().post(event);
        return !event.isCancelled() && took && save();
    }

    @Override
    public boolean giveHonor(String id) {
        PlayerGetHonorEvent event = new PlayerGetHonorEvent(Cause.builder().append(NewHonor.plugin).build(EventContext.empty()), uuid, id);
        Sponge.getEventManager().post(event);
        return !event.isCancelled() && noSaveGive(id) && save();
    }

    @Override
    public void setWhetherUseHonor(boolean use) {
        cfg.getNode("usehonor").setValue(use);
        save();
    }

    @Override
    public void setWhetherEnableEffects(boolean enable) {
        cfg.getNode("enableEffects").setValue(enable);
        save();
    }

    @Override
    public boolean isEnableEffects() {
        return cfg.getNode("enableEffects").getBoolean(true);
    }

    @Override
    public boolean setUseHonor(String id) {
        if (isOwnHonor(id) && HonorData.getHonorText(id).isPresent()) {
            return save();
        }
        return false;
    }

    @Override
    public String getUsingHonorID() {
        return cfg.getNode("using").getString();
    }

    @Override
    public Optional<List<String>> getOwnHonors() {
        try {
            return Optional.of(cfg.getNode("honors").getList(type, ArrayList::new));
        } catch (ObjectMappingException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public void checkUsingHonor() {
        String usingID = getUsingHonorID();
        if (usingID == null) {
            return;
        }
        if (!isOwnHonor(getUsingHonorID())) {
            Optional<List<String>> list = NewHonorConfig.getDefaultOwnHonors();
            if (list.isPresent()) {
                setUseHonor(list.get().get(0));
            } else {
                setUseHonor("");
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

    @Override
    public UUID getUUID() {
        return this.uuid;
    }


    private CommentedConfigurationNode load() {
        try {
            return loader.load();
        } catch (IOException e) {
            return loader.createEmptyNode(ConfigurationOptions.defaults());
        }
    }

    private boolean isOwnHonor(String id) {
        return getOwnHonors().orElse(Collections.emptyList()).stream().anyMatch(s -> Objects.equals(s, id));
    }

    private boolean noSaveGive(String id) {
        if (SqlManager.enable) {
            return false;
        }
        Optional<List<String>> honors = getOwnHonors();
        if (HonorData.getHonorText(id).isPresent() && honors.isPresent() && honors.get().stream().noneMatch(id::equals)) {
            honors.get().add(id);
            cfg.getNode("honors").setValue(honors.get());
            Sponge.getServer().getPlayer(uuid).map(Player::getName).ifPresent(name ->
                    HonorData.getGetMessage(id, name).ifPresent(Sponge.getServer().getBroadcastChannel()::send));
            return true;
        }
        return false;
    }
}
