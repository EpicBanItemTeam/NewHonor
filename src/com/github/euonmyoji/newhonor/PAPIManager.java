package com.github.euonmyoji.newhonor;

import me.rojo8399.placeholderapi.Listening;
import me.rojo8399.placeholderapi.Placeholder;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.Source;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

@Listening
public class PAPIManager {

    PAPIManager() {
        PlaceholderService service = Sponge.getServiceManager().provideUnchecked(PlaceholderService.class);
        service.loadAll(this, this).stream().peek(builder -> {
            if ("newhonor".equals(builder.getId())) {
                builder.description("newhonor text").version("1.5").author("yinyangshi");
            }
        }).forEach(builder -> {
            try {
                builder.buildAndRegister();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Placeholder(id = "newhonor")
    public Object getNewHonorText(@Source Player p) {
        return NewHonor.honorTextCache.get(p.getUniqueId());
    }
}
