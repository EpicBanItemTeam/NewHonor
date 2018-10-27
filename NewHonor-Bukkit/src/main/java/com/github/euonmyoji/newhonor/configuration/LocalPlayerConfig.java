package com.github.euonmyoji.newhonor.configuration;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.configuration.PlayerConfig;
import com.github.euonmyoji.newhonor.enums.ListHonorStyle;
import com.google.common.reflect.TypeToken;
import net.yeah.mungsoup.mung.configuration.MungConfig;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author MungSoup
 */
public class LocalPlayerConfig extends MungConfig implements PlayerConfig {
    private UUID uuid;

    public LocalPlayerConfig(UUID uuid) throws IOException {
        super(NewHonor.instance, uuid.toString(), "conf.tmp");
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public void init() {
        Optional<List<String>> defaultHonors = NewHonor.mainConfig.getDefaultOwnHonors();
        if (defaultHonors.isPresent()) {
            defaultHonors.get().forEach(this::noSaveGive);
            if (save()) {
                //如果玩家没有使用中的头衔就用默认的第一个
                setUseHonor(config.getNode(USING_KEY).getString(defaultHonors.get().get(0)));
            }
        }
    }

    private void noSaveGive(String id) {
        Optional<List<String>> honors = getOwnHonors();
        honors.ifPresent(strings -> {
            if (hasHonor(id)) {
                strings.add(id);
                config.getNode(HONORS_KEY).setValue(honors.get());
            }
        });
    }

    private boolean hasHonor(String id) {
        return NewHonor.honorConfig.hasHonor(id);
    }

    @Override
    public boolean isUseHonor() {
        return config.getNode(USEHONOR_KEY).getBoolean(false);
    }

    @Override
    public boolean takeHonor(String... ids) {
        for (String id : ids) {
            if (hasHonor(id)) {
                Optional<List<String>> honors = getOwnHonors();
                honors.ifPresent(strings -> strings.remove(id));
            }
        }
        return save();
    }

    @Override
    public boolean giveHonor(String id) {
        Optional<List<String>> honors = getOwnHonors();
        if (honors.isPresent() && !honors.get().contains(id) && hasHonor(id)) {
            honors.get().add(id);
            return save();
        }
        return false;
    }

    @Override
    public void setWhetherUseHonor(boolean use) {
        config.getNode(USEHONOR_KEY).setValue(use);
    }

    @Override
    public void setWhetherEnableEffects(boolean enable) {
        config.getNode(ENABLE_EFFECTS_KEY).setValue(enable);
    }

    @Override
    public boolean isEnabledEffects() {
        return config.getNode(ENABLE_EFFECTS_KEY).getBoolean(false);
    }

    @Override
    public boolean setUseHonor(String id) {
        if (hasHonor(id)) {
            config.getNode(USING_KEY).setValue(id);
            return save();
        }
        return false;
    }

    @Override
    public String getUsingHonorID() {
        return config.getNode(USING_KEY).getString();
    }

    @Override
    public Optional<List<String>> getOwnHonors() {
        try {
            return Optional.of(config.getNode(HONORS_KEY).getList(TypeToken.of(String.class)));
        } catch (ObjectMappingException e) {
            return Optional.empty();
        }
    }

    @Override
    public void enableAutoChange(boolean auto) {
        config.getNode(AUTO_CHANGE_KEY).setValue(auto);
        save();
    }

    @Override
    public boolean isEnabledAutoChange() {
        return config.getNode(AUTO_CHANGE_KEY).getBoolean(false);
    }

    @Override
    public ListHonorStyle getListHonorStyle() {
        return ListHonorStyle.valueOf(config.getNode(LIST_HONOR_STYLE_KEY).getString("ITEM"));
    }

    @Override
    public void setListHonorStyle(ListHonorStyle style) {
        config.getNode(LIST_HONOR_STYLE_KEY).setValue(style);
    }
}
