package com.github.euonmyoji.newhonor;

import com.github.euonmyoji.newhonor.data.HonorValueData;
import me.rojo8399.placeholderapi.*;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;

import javax.annotation.Nullable;

/**
 * @author yinyangshi
 */
@Listening
public class PlaceHolderManager {
    private static PlaceHolderManager instance;

    static void create() {
        if (instance == null) {
            instance = new PlaceHolderManager();
        }
    }

    @Placeholder(id = NewHonor.NEWHONOR_ID)
    public Object getNewHonorText(@Source User user, @Nullable @Token String token) {
        final String valueKey = "value";
        final String strValue = "strValue";
        final String usingID = "usingID";
        HonorValueData value = NewHonor.plugin.honorTextCache.get(user.getUniqueId());
        if (value != null) {
            if (token == null || valueKey.equals(token)) {
                return value.getValue();
            }
            if (usingID.equals(token)) {
                return value.getId();
            }
            if (strValue.equals(token)) {
                return value.getStrValue();
            }
        }
        return null;
    }

    private PlaceHolderManager() {
        PlaceholderService service = Sponge.getServiceManager().provideUnchecked(PlaceholderService.class);
        service.loadAll(this, NewHonor.plugin).forEach(builder -> {
            if (NewHonor.NEWHONOR_ID.equals(builder.getId())) {
                try {
                    builder.description("newhonor text").version("1.5").author("yinyangshi").plugin(NewHonor.plugin)
                            .buildAndRegister();
                } catch (Exception e) {
                    NewHonor.logger.warn("offer PAPI failed", e);
                }
            }
        });
    }

}
