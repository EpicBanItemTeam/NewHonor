package com.github.euonmyoji.newhonor.listener;

import br.net.fabiozumbi12.UltimateChat.Sponge.API.SendChannelMessageEvent;
import com.github.euonmyoji.newhonor.NewHonor;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Identifiable;

import java.util.UUID;

/**
 * @author yinyangshi
 */
public class UltimateChatEventListener {

    @Listener
    public void sendCME(SendChannelMessageEvent event) {
        CommandSource source = event.getSender();
        if (source instanceof Identifiable) {
            UUID uuid = ((Identifiable) source).getUniqueId();
            if (NewHonor.plugin.honorTextCache.containsKey(uuid)) {
                event.addTag("{newhonor}",
                        TextSerializers.FORMATTING_CODE.serialize(NewHonor.plugin.honorTextCache.get(uuid)));
            }
        }
    }
}
