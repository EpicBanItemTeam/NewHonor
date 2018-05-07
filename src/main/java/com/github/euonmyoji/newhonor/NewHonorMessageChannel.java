package com.github.euonmyoji.newhonor;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * @author yinyangshi
 */
@NonnullByDefault
public class NewHonorMessageChannel implements MessageChannel {
    @Override
    public Optional<Text> transformMessage(@Nullable Object sender, MessageReceiver recipient, Text text, ChatType type) {
        if (sender instanceof Player && NewHonor.plugin.honorTextCache.containsKey(((Player) sender).getUniqueId())) {
            Player p = (Player) sender;
            return Optional.of(Text.of(NewHonor.plugin.honorTextCache.get(p.getUniqueId()), text));
        }
        return Optional.of(text);
    }

    @Override
    public Collection<MessageReceiver> getMembers() {
        return Collections.emptyList();
    }
}
