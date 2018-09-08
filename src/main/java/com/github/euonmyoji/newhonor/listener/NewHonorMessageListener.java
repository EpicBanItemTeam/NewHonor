package com.github.euonmyoji.newhonor.listener;

import com.github.euonmyoji.newhonor.NewHonor;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.channel.MessageChannel;

/**
 * @author yinyangshi
 */
public final class NewHonorMessageListener {

    @Listener(order = Order.LAST)
    public void onChat(MessageChannelEvent.Chat event) {
        MessageChannel originalChannel = event.getOriginalChannel();
        MessageChannel newChannel = MessageChannel.combined(originalChannel,
                NewHonor.M_MESSAGE);
        event.setChannel(newChannel);
    }
}
