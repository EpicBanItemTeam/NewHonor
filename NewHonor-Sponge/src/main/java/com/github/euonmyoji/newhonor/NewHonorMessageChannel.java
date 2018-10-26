package com.github.euonmyoji.newhonor;

import com.github.euonmyoji.newhonor.data.HonorData;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.util.Identifiable;
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
        if (sender instanceof Identifiable) {
            HonorData v = NewHonor.plugin.honorTextCache.get(((Identifiable) sender).getUniqueId());
            if (v != null) {
                return Optional.of(Text.of(v.getValue(), text));
            }
        }
        return Optional.of(text);
    }

    @Override
    public Collection<MessageReceiver> getMembers() {
        return Collections.emptyList();
    }
}
