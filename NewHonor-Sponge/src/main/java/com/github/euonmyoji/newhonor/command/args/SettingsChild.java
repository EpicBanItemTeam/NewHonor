package com.github.euonmyoji.newhonor.command.args;

import com.github.euonmyoji.newhonor.enums.ListHonorStyle;
import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.configuration.PlayerConfig;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import static org.spongepowered.api.text.Text.of;

/**
 * @author yinyangshi
 */
public final class SettingsChild {

    public static CommandSpec usehonor = CommandSpec.builder()
            .arguments(GenericArguments.bool(of("boolean")))
            .executor((src, args) -> {
                if (src instanceof User) {
                    try {
                        boolean use = args.<Boolean>getOne(of("boolean")).orElseThrow(NoSuchFieldError::new);
                        PlayerConfig pd = PlayerConfig.get(((User) src));
                        pd.setWhetherUseHonor(use);
                        NewHonor.updateCache(pd);
                        src.sendMessage(of("[NewHonor]change settings successful"));
                        return CommandResult.success();
                    } catch (Exception e) {
                        src.sendMessage(of("[NewHonor]error!"));
                        NewHonor.logger.warn("Change user " + src.getName() + "  settings but an error found", e);
                    }
                }
                src.sendMessage(of("[NewHonor]You are not a user"));
                return CommandResult.empty();
            })
            .build();

    public static CommandSpec listStyle = CommandSpec.builder()
            .arguments(new ListHonorsStyleArg(Text.of("style")))
            .executor((src, args) -> {
                if (src instanceof User) {
                    try {
                        ListHonorStyle style = args.<ListHonorStyle>getOne(of("style")).orElseThrow(NoSuchFieldError::new);
                        PlayerConfig pd = PlayerConfig.get(((User) src));
                        pd.setListHonorStyle(style);
                        src.sendMessage(of("[NewHonor]change settings successful"));
                        return CommandResult.success();
                    } catch (Exception e) {
                        src.sendMessage(of("[NewHonor]error!"));
                        NewHonor.logger.warn("Change user " + src.getName() + "  settings but an error found", e);
                    }
                    return CommandResult.empty();
                }
                src.sendMessage(Text.of("[NewHonor]You are not a user!"));
                return CommandResult.empty();
            })
            .build();

    public static CommandSpec enableEffects = CommandSpec.builder()
            .arguments(GenericArguments.bool(of("boolean")))
            .executor((src, args) -> {
                if (src instanceof User) {
                    try {
                        boolean enable = args.<Boolean>getOne(of("boolean")).orElseThrow(NoSuchFieldError::new);
                        PlayerConfig pd = PlayerConfig.get(((User) src));
                        pd.setWhetherEnableEffects(enable);
                        NewHonor.updateCache(pd);
                        src.sendMessage(of("[NewHonor]change settings successful"));
                        return CommandResult.success();
                    } catch (Exception e) {
                        src.sendMessage(of("[NewHonor]error!"));
                        NewHonor.logger.warn("Change user " + src.getName() + "  settings but an error found", e);
                        return CommandResult.empty();
                    }
                }
                src.sendMessage(of("[NewHonor]You are not a user"));
                return CommandResult.empty();
            })
            .build();

    public static CommandSpec autochange = CommandSpec.builder()
            .arguments(GenericArguments.bool(of("boolean")))
            .executor((src, args) -> {
                if (src instanceof User) {
                    try {
                        boolean enable = args.<Boolean>getOne(of("boolean")).orElseThrow(NoSuchFieldError::new);
                        PlayerConfig pd = PlayerConfig.get(((User) src));
                        pd.enableAutoChange(enable);
                        NewHonor.updateCache(pd);
                        src.sendMessage(of("[NewHonor]change settings successful"));
                        return CommandResult.success();
                    } catch (Exception e) {
                        src.sendMessage(of("[NewHonor]error!"));
                        NewHonor.logger.warn("Change user " + src.getName() + "  settings but an error found", e);
                        return CommandResult.empty();
                    }
                }
                src.sendMessage(of("[NewHonor]You are not a user"));
                return CommandResult.empty();
            })
            .build();

    private SettingsChild() {
        throw new UnsupportedOperationException();
    }
}
