package com.github.euonmyoji.newhonor.configuration;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.configuration.PlayerConfig;
import com.github.euonmyoji.newhonor.enums.ListHonorStyle;
import com.google.common.reflect.TypeToken;
import net.yeah.mungsoup.mung.configuration.MungConfig;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.github.euonmyoji.newhonor.NewHonor.honorConfig;

/**
 * @author MungSoup
 */
public class LocalPlayerConfig extends MungConfig implements PlayerConfig {
    private UUID uuid;

    public LocalPlayerConfig(UUID uuid) throws IOException {
        super(NewHonor.plugin, "PlayerData/" + uuid.toString() + ".conf");
        this.uuid = uuid;
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public void init() {
        List<String> defaultHonors = NewHonor.mainConfig.getDefaultOwnHonors();
        if (defaultHonors != null && !defaultHonors.isEmpty()) {
            defaultHonors.forEach(this::noSaveGive);
            if (save()) {
                //如果玩家没有使用中的头衔就用默认的第一个
                setUseHonor(cfg.getNode(USING_KEY).getString(defaultHonors.get(0)));
            }
        }
    }

    private void noSaveGive(String id) {
        Optional<List<String>> honors = getOwnHonors();
        honors.ifPresent(strings -> {
            if (!honorConfig.notExist(id)) {
                strings.add(id);
                cfg.getNode(HONORS_KEY).setValue(honors.get());
            }
        });
    }

    @Override
    public boolean isUseHonor() {
        return cfg.getNode(USEHONOR_KEY).getBoolean(true);
    }

    @Override
    public boolean takeHonor(String... ids) {
        for (String id : ids) {
            if (!honorConfig.notExist(id)) {
                Optional<List<String>> honors = getOwnHonors();
                honors.ifPresent(strings -> strings.remove(id));
            }
        }
        return save();
    }

    @Override
    public boolean giveHonor(String id) {
        Optional<List<String>> honors = getOwnHonors();
        if (honors.isPresent() && !honors.get().contains(id) && !honorConfig.notExist(id)) {
            honors.get().add(id);
            cfg.getNode(HONORS_KEY).setValue(honors.get());
            setUseHonor(id);
            return save();
        }
        return false;
    }

    @Override
    public void setWhetherUseHonor(boolean use) {
        cfg.getNode(USEHONOR_KEY).setValue(use);
    }

    @Override
    public void setWhetherEnableEffects(boolean enable) {
        cfg.getNode(ENABLE_EFFECTS_KEY).setValue(enable);
    }

    @Override
    public boolean isEnabledEffects() {
        return cfg.getNode(ENABLE_EFFECTS_KEY).getBoolean(false);
    }

    @Override
    public boolean setUseHonor(String id) {
        //todo: 不应该判断 传参进来就应该保证存在
        if (!honorConfig.notExist(id)) {
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
            return Optional.of(cfg.getNode(HONORS_KEY).getList(TypeToken.of(String.class), ArrayList::new));
        } catch (ObjectMappingException e) {
            NewHonor.plugin.getLogger().info("Player data " + uuid + " is wrong!");
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public void enableAutoChange(boolean auto) {
        cfg.getNode(AUTO_CHANGE_KEY).setValue(auto);
        save();
    }

    @Override
    public boolean isEnabledAutoChange() {
        return cfg.getNode(AUTO_CHANGE_KEY).getBoolean(false);
    }

    @Override
    public ListHonorStyle getListHonorStyle() {
        return ListHonorStyle.valueOf(cfg.getNode(LIST_HONOR_STYLE_KEY).getString("ITEM"));
    }

    @Override
    public void setListHonorStyle(ListHonorStyle style) {
        cfg.getNode(LIST_HONOR_STYLE_KEY).setValue(style);
    }
}