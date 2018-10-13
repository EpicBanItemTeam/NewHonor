package com.github.euonmyoji.newhonor.sponge.manager;

import com.github.euonmyoji.newhonor.sponge.NewHonor;
import com.github.euonmyoji.newhonor.sponge.data.HonorData;
import me.rojo8399.placeholderapi.Placeholder;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.Source;
import me.rojo8399.placeholderapi.Token;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;

import javax.annotation.Nullable;

/**
 * @author yinyangshi
 */
public final class PlaceHolderManager {
    private static PlaceHolderManager instance;
    private static final String VALUE_T = "value";
    private static final String STR_T = "strvalue";
    private static final String ID_T = "usingid";

    public static void create() {
        if (instance == null) {
            instance = new PlaceHolderManager();
        }
    }

    @Placeholder(id = NewHonor.NEWHONOR_ID)
    public Object getNewHonorText(@Source User user, @Nullable @Token(fix = true) String token) {
        HonorData value = NewHonor.plugin.honorTextCache.get(user.getUniqueId());
        if (value != null) {
            if (token == null) {
                return value.getValue();
            }
            switch (token) {
                case ID_T: {
                    return value.getId();
                }
                case STR_T: {
                    return value.getStrValue();
                }
                case VALUE_T: {
                    return value.getValue();
                }
                default: {
                    //not to offer value though default
                    break;
                }
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
                            .addTokens(VALUE_T, STR_T, ID_T)
                            .buildAndRegister();
                } catch (Exception e) {
                    NewHonor.logger.warn("offer PAPI failed", e);
                }
            }
        });
    }

}
