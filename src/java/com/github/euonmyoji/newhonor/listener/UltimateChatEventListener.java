package com.github.euonmyoji.newhonor.listener;

import br.net.fabiozumbi12.UltimateChat.Sponge.API.SendChannelMessageEvent;
import com.github.euonmyoji.newhonor.NewHonor;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.UUID;

/**
 * @author yinyangshi
 */
public class UltimateChatEventListener {

    @Listener
    public void sendCME(SendChannelMessageEvent event) {
        CommandSource source = event.getSender();
        if (source instanceof Player) {
            UUID uuid = ((Player) source).getUniqueId();
            if (NewHonor.HONOR_TEXT_CACHE.containsKey(uuid)) {
                event.addTag("{newhonor}",
                        TextSerializers.FORMATTING_CODE.serialize(NewHonor.HONOR_TEXT_CACHE.get(uuid)));
            }
        }
    }
}
