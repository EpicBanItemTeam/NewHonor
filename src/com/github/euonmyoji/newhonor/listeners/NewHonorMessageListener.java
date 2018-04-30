package com.github.euonmyoji.newhonor.listeners;

import com.github.euonmyoji.newhonor.NewHonor;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.channel.MessageChannel;

public class NewHonorMessageListener {

    @Listener(order = Order.LATE)
    public void onChat(MessageChannelEvent.Chat event) {
        MessageChannel originalChannel = event.getOriginalChannel();
        MessageChannel newChannel = MessageChannel.combined(originalChannel,
                NewHonor.M_MESSAGE);
        event.setChannel(newChannel);
    }
}
