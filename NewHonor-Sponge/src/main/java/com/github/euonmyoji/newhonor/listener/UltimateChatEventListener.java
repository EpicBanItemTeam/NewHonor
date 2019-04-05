package com.github.euonmyoji.newhonor.listener;

import br.net.fabiozumbi12.UltimateChat.Sponge.API.SendChannelMessageEvent;
import com.github.euonmyoji.newhonor.api.data.HonorData;
import com.github.euonmyoji.newhonor.api.manager.HonorManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.util.Identifiable;

import java.util.UUID;

/**
 * @author yinyangshi
 */
public final class UltimateChatEventListener {

    @Listener
    public void sendCME(SendChannelMessageEvent event) {
        CommandSource source = event.getSender();
        if (source instanceof Identifiable) {
            UUID uuid = ((Identifiable) source).getUniqueId();
            HonorData value = Sponge.getServiceManager().provideUnchecked(HonorManager.class).getUsingHonor(uuid);
            String tag = "{newhonor}";
            if (value == null) {
                event.addTag(tag, "");
            } else {
                event.addTag(tag, value.getStrValue());
            }
        }
    }
}
