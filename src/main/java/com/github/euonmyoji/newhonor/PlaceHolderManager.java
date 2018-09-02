package com.github.euonmyoji.newhonor;

import com.github.euonmyoji.newhonor.data.HonorValueData;
import me.rojo8399.placeholderapi.Listening;
import me.rojo8399.placeholderapi.Placeholder;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.Source;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Identifiable;

/**
 * @author yinyangshi
 */
@Listening
public class PlaceHolderManager {
    private static PlaceHolderManager instance;

    static void create() throws RuntimeException {
        if (instance == null) {
            instance = new PlaceHolderManager();
        }
    }

    @Placeholder(id = NewHonor.NEWHONOR_ID)
    public Text getNewHonorText(@Source Identifiable i) {
        HonorValueData value = NewHonor.plugin.honorTextCache.get(i.getUniqueId());
        return value == null ? Text.of("") : value.getValue();
    }

    private PlaceHolderManager() throws RuntimeException {
        PlaceholderService service = Sponge.getServiceManager().provideUnchecked(PlaceholderService.class);
        service.loadAll(this, NewHonor.plugin).forEach(builder -> {
            if (NewHonor.NEWHONOR_ID.equals(builder.getId())) {
                try {
                    builder.description("newhonor text").version("1.5").author("yinyangshi").plugin(NewHonor.plugin)
                            .buildAndRegister();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
