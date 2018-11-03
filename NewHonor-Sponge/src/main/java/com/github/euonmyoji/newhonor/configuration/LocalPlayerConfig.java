package com.github.euonmyoji.newhonor.configuration;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.configuration.BasePlayerConfig;
import com.github.euonmyoji.newhonor.api.event.PlayerGetHonorEvent;
import com.github.euonmyoji.newhonor.api.event.PlayerLoseHonorEvent;
import com.github.euonmyoji.newhonor.enums.ListHonorStyle;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author yinyangshi
 */
public class LocalPlayerConfig extends BasePlayerConfig {
    private final CommentedConfigurationNode cfg;
    private final TypeToken<String> type = TypeToken.of(String.class);
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    public LocalPlayerConfig(UUID uuid) {
        this.uuid = uuid;
        loader = HoconConfigurationLoader.builder()
                .setPath((PluginConfig.cfgDir.resolve("PlayerData")).resolve(uuid.toString() + ".conf")).build();
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
        List<String> defaultHonors = PluginConfig.getDefaultOwnHonors();
        if (defaultHonors != null && !defaultHonors.isEmpty()) {
            defaultHonors.forEach(this::noSaveGive);
            if (save()) {
                //如果玩家没有使用中的头衔就用默认的第一个
                setUseHonor(cfg.getNode(USING_KEY).getString(defaultHonors.get(0)));
            }
        }
    }

    @Override
    public ListHonorStyle getListHonorStyle() {
        return ListHonorStyle.valueOf(cfg.getNode(LIST_HONOR_STYLE_KEY).getString(PluginConfig.defaultListHonorStyle().toString()));
    }

    @Override
    public boolean isUseHonor() {
        return cfg.getNode(USEHONOR_KEY).getBoolean(true);
    }

    @Override
    public boolean takeHonor(String... ids) {
        boolean took = false;
        List<String> honors = getOwnHonors().orElse(null);
        if (honors != null) {
            for (String id : ids) {
                if (honors.remove(id)) {
                    took = true;
                }
            }
        }
        if (took) {
            cfg.getNode(HONORS_KEY).setValue(honors);
            try {
                checkUsingHonor();
            } catch (SQLException e) {
                NewHonor.logger.debug("why sql error?", e);
            }
            PlayerLoseHonorEvent event = new PlayerLoseHonorEvent(Cause.builder().append(NewHonor.plugin).build(EventContext.empty()), uuid, ids);
            return !Sponge.getEventManager().post(event) && save();
        }
        return false;
    }

    @Override
    public boolean giveHonor(String id) {
        PlayerGetHonorEvent event = new PlayerGetHonorEvent(Cause.builder().append(NewHonor.plugin).build(EventContext.empty()), uuid, id);
        return !Sponge.getEventManager().post(event) && noSaveGive(id) && save();
    }

    @Override
    public void setWhetherUseHonor(boolean use) {
        cfg.getNode(USEHONOR_KEY).setValue(use);
        save();
    }

    @Override
    public void setListHonorStyle(ListHonorStyle style) {
        cfg.getNode(LIST_HONOR_STYLE_KEY).setValue(style.toString());
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
        try {
            boolean isEmpty = "".equals(id);
            boolean isRight = isOwnHonor(id) && !HonorConfig.isVirtual(id);
            if (isRight || isEmpty) {
                cfg.getNode(USING_KEY).setValue(id);
                return save();
            }
        } catch (SQLException e) {
            NewHonor.logger.info("Why sql error?", e);
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
            NewHonor.logger.warn("Player data " + uuid + " is wrong!", e);
            return Optional.empty();
        }
    }

    private boolean save() {
        try {
            loader.save(cfg);
            return true;
        } catch (IOException e) {
            NewHonor.logger.warn("IOE when saving player data!", e);
        }
        return false;
    }

    private CommentedConfigurationNode load() {
        try {
            return loader.load();
        } catch (IOException e) {
            NewHonor.logger.warn("get player data error, creating new ong", e);
            return loader.createEmptyNode(ConfigurationOptions.defaults());
        }
    }

    private boolean noSaveGive(String id) {
        Optional<List<String>> honors = getOwnHonors();
        if (!HonorConfig.isVirtual(id) && honors.isPresent() && !honors.get().contains(id)) {
            honors.get().add(id);
            cfg.getNode(HONORS_KEY).setValue(honors.get());
            Sponge.getServer().getPlayer(uuid).map(Player::getName).ifPresent(name ->
                    HonorConfig.getGetMessage(id, name).ifPresent(Sponge.getServer().getBroadcastChannel()::send));
            if (isEnabledAutoChange()) {
                setUseHonor(id);
                NewHonor.updateCache(this);
            }
            return true;
        }
        return false;
    }
}
