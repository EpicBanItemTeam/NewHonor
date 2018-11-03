package com.github.euonmyoji.newhonor.hook;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.configuration.PlayerConfig;
import me.clip.placeholderapi.PlaceholderHook;
import org.bukkit.entity.Player;
//fixme: 包呢 库呢 没库写什么🐎
public class PAPIHook extends PlaceholderHook {

    @Override
    public String onPlaceholderRequest(Player p, String params) {
        if ("text".equals(params)) {
            try {
                PlayerConfig config = PlayerConfig.get(p.getUniqueId());
                if (config.isUseHonor() && config.getUsingHonorID() != null) {
                    return NewHonor.honorConfig.getHonor(config.getUsingHonorID()).getText();
                }
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }
}
