package com.github.euonmyoji.newhonor;

import me.rojo8399.placeholderapi.Placeholder;
import me.rojo8399.placeholderapi.Source;
import org.spongepowered.api.entity.living.player.Player;

public class PAPIManager {

    @Placeholder(id = "newhonor")
    public Object getNewHonorText(@Source Player p) {
        return NewHonor.honorTextCache.get(p.getUniqueId());
    }
}
