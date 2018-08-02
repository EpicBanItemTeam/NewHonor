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
public class LocalPlayerConfig implements PlayerConfig {
    private final UUID uuid;
    private final CommentedConfigurationNode cfg;
    private final TypeToken<String> type = TypeToken.of(String.class);
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    public LocalPlayerConfig(UUID uuid) {
        this.uuid = uuid;
        loader = HoconConfigurationLoader.builder()
                .setPath((NewHonorConfig.cfgDir.resolve("PlayerData")).resolve(uuid.toString() + ".conf")).build();
        cfg = load();

        //为了兼容以前版本cfg 更新代码
        final String oldUseKey = "using";
        if (!cfg.getNode(oldUseKey).isVirtual()) {
            cfg.getNode(USING_KEY).setValue(cfg.getNode(oldUseKey).getString());
            cfg.removeChild(oldUseKey);
            save();
        }
    }

    @Override
    public void init() {
        Optional<List<String>> defaultHonors = NewHonorConfig.getDefaultOwnHonors();
        if (defaultHonors.isPresent()) {
            defaultHonors.get().forEach(this::noSaveGive);
            if (save()) {
                //如果玩家没有使用中的头衔就用默认的第一个
                setUseHonor(cfg.getNode(USING_KEY).getString(defaultHonors.get().get(0)));
            }
        }
    }

    @Override
    public boolean isUseHonor() {
        return cfg.getNode(USEHONOR_KEY).getBoolean(true);
    }

    @Override
    public boolean takeHonor(String... ids) {
        boolean took = false;
        Optional<List<String>> honors = getOwnHonors();
        for (String id : ids) {
            if (honors.isPresent() && honors.get().remove(id)) {
                took = true;
            }
        }
        if (took) {
            cfg.getNode(HONORS_KEY).setValue(honors.get());
            PlayerLoseHonorEvent event = new PlayerLoseHonorEvent(Cause.builder().append(NewHonor.plugin).build(EventContext.empty()), uuid, ids);
            return !Sponge.getEventManager().post(event) && save();
        }
        return false;
    }

    @Override
    public boolean giveHonor(String id) {
        PlayerGetHonorEvent event = new PlayerGetHonorEvent(Cause.builder().append(NewHonor.plugin).build(EventContext.empty()), uuid, id);
        Sponge.getEventManager().post(event);
        return !event.isCancelled() && noSaveGive(id) && save();
    }

    @Override
    public void setWhetherUseHonor(boolean use) {
        cfg.getNode(USEHONOR_KEY).setValue(use);
        save();
    }

    @Override
    public void setWhetherEnableEffects(boolean enable) {
        cfg.getNode(ENABLE_EFFECTS_KEY).setValue(enable);
        save();
    }

    @Override
    public void enableAutoChange(boolean auto) {
        cfg.getNode(AUTO_CHANGE_KEY).setValue(auto);
        save();
    }

    @Override
    public boolean isEnabledAutoChange() {
        return cfg.getNode(AUTO_CHANGE_KEY).getBoolean(true);
    }

    @Override
    public boolean isEnabledEffects() {
        return cfg.getNode(ENABLE_EFFECTS_KEY).getBoolean(true);
    }

    @Override
    public boolean setUseHonor(String id) {
        if (isOwnHonor(id) && HonorConfig.getHonorText(id).isPresent()) {
            cfg.getNode(USING_KEY).setValue(id);
            return save();
        }
        return false;
    }

    @Override
    public String getUsingHonorID() {
        return cfg.getNode(USING_KEY).getString();
    }

    @Override
    public Optional<List<String>> getOwnHonors() {
        try {
            return Optional.of(cfg.getNode(HONORS_KEY).getList(type, ArrayList::new));
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

    public boolean save() {
        try {
            loader.save(cfg);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isOwnHonor(String id) {
        return getOwnHonors().orElse(Collections.emptyList()).contains(id);
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

    private boolean noSaveGive(String id) {
        Optional<List<String>> honors = getOwnHonors();
        if (HonorConfig.getHonorText(id).isPresent() && honors.isPresent() && !honors.get().contains(id)) {
            honors.get().add(id);
            cfg.getNode(HONORS_KEY).setValue(honors.get());
            Sponge.getServer().getPlayer(uuid).map(Player::getName).ifPresent(name ->
                    HonorConfig.getGetMessage(id, name).ifPresent(Sponge.getServer().getBroadcastChannel()::send));
            if (isEnabledAutoChange()) {
                setUseHonor(id);
                NewHonor.doSomething(this);
            }
            return true;
        }
        return false;
    }
}
