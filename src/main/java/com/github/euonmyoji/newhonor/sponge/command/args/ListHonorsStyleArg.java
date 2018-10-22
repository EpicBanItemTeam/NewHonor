package com.github.euonmyoji.newhonor.sponge.command.args;

import com.github.euonmyoji.newhonor.common.enums.ListHonorStyle;
import com.github.euonmyoji.newhonor.sponge.NewHonor;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author yinyangshi
 */
@NonnullByDefault
public class ListHonorsStyleArg extends CommandElement {
    ListHonorsStyleArg(@Nullable Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected ListHonorStyle parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        return ListHonorStyle.valueOf(args.next().toUpperCase());
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        if (args.hasNext()) {
            try {
                String s = args.next().toUpperCase();
                return Stream.of(ListHonorStyle.values()).map(Object::toString).filter(s1 -> s1.startsWith(s))
                        .collect(Collectors.toList());
            } catch (ArgumentParseException e) {
                NewHonor.logger.debug("unknown error", e);
            }
        }
        return Stream.of(ListHonorStyle.values()).map(Object::toString).collect(Collectors.toList());
    }
}
