package com.github.euonmyoji.newhonor;

import me.rojo8399.placeholderapi.Listening;
import me.rojo8399.placeholderapi.Placeholder;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.Source;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

/**
 * @author yinyangshi
 */
@Listening
public class PlaceHolderManager {
    private static final String HONOR_ID = "newhonor";
    private static PlaceHolderManager instance;

    static void create() throws RuntimeException {
        if (instance == null) {
            instance = new PlaceHolderManager();
        }
    }

    @Placeholder(id = "newhonor")
    public Text getNewHonorText(@Source Player p) {
        return NewHonor.plugin.honorTextCache.get(p.getUniqueId()).getValue();
    }

    private PlaceHolderManager() throws RuntimeException {
        PlaceholderService service = Sponge.getServiceManager().provideUnchecked(PlaceholderService.class);
        service.loadAll(this, NewHonor.plugin).forEach(builder -> {
            if (HONOR_ID.equals(builder.getId())) {
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
