package com.github.euonmyoji.newhonor.hook;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.configuration.PlayerConfig;
import com.github.euonmyoji.newhonor.data.Honor;
import me.clip.placeholderapi.PlaceholderHook;
import org.bukkit.entity.Player;

public class PAPIHook extends PlaceholderHook {

    @Override
    public String onPlaceholderRequest(Player p, String params) {
        try {
            PlayerConfig config = PlayerConfig.get(p.getUniqueId());
            if (config.isUseHonor() && config.getUsingHonorID() != null) {
                Honor honor = NewHonor.honorConfig.getHonor(config.getUsingHonorID());
                switch (params) {
                    case "text":
                        return honor.getText();
                    case "click_value":
                        return honor.getClickValue();
                    case "hover_value":
                        return honor.getHoverValue();
                }
            }
        } catch (Exception e) {
            return "";
        }
        return "";
    }
}
