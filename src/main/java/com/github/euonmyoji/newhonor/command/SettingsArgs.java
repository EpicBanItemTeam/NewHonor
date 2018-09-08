package com.github.euonmyoji.newhonor.command;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.configuration.PlayerConfig;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;

import static org.spongepowered.api.text.Text.of;

class SettingsArgs {

    static CommandSpec usehonor = CommandSpec.builder()
            .arguments(GenericArguments.bool(of("boolean")))
            .executor((src, args) -> {
                if (src instanceof User) {
                    try {
                        boolean use = args.<Boolean>getOne(of("boolean")).orElseThrow(NoSuchFieldError::new);
                        PlayerConfig pd = PlayerConfig.get(((User) src));
                        pd.setWhetherUseHonor(use);
                        NewHonor.doSomething(pd);
                        src.sendMessage(of("[NewHonor]change settings successful"));
                        return CommandResult.success();
                    } catch (Exception e) {
                        src.sendMessage(of("[NewHonor]error!"));
                        e.printStackTrace();
                    }
                }
                src.sendMessage(of("[NewHonor]You are not a user"));
                return CommandResult.empty();
            })
            .build();

    static CommandSpec enableEffects = CommandSpec.builder()
            .arguments(GenericArguments.bool(of("boolean")))
            .executor((src, args) -> {
                if (src instanceof User) {
                    try {
                        boolean enable = args.<Boolean>getOne(of("boolean")).orElseThrow(NoSuchFieldError::new);
                        PlayerConfig pd = PlayerConfig.get(((User) src));
                        pd.setWhetherEnableEffects(enable);
                        NewHonor.doSomething(pd);
                        src.sendMessage(of("[NewHonor]change settings successful"));
                        return CommandResult.success();
                    } catch (Exception e) {
                        src.sendMessage(of("[NewHonor]error!"));
                        e.printStackTrace();
                        return CommandResult.empty();
                    }
                }
                src.sendMessage(of("[NewHonor]You are not a user"));
                return CommandResult.empty();
            })
            .build();

    static CommandSpec autochange = CommandSpec.builder()
            .arguments(GenericArguments.bool(of("boolean")))
            .executor((src, args) -> {
                if (src instanceof User) {
                    try {
                        boolean enable = args.<Boolean>getOne(of("boolean")).orElseThrow(NoSuchFieldError::new);
                        PlayerConfig pd = PlayerConfig.get(((User) src));
                        pd.enableAutoChange(enable);
                        NewHonor.doSomething(pd);
                        src.sendMessage(of("[NewHonor]change settings successful"));
                        return CommandResult.success();
                    } catch (Exception e) {
                        src.sendMessage(of("[NewHonor]error!"));
                        e.printStackTrace();
                        return CommandResult.empty();
                    }
                }
                src.sendMessage(of("[NewHonor]You are not a user"));
                return CommandResult.empty();
            })
            .build();

    private SettingsArgs() {
        throw new UnsupportedOperationException();
    }
}
