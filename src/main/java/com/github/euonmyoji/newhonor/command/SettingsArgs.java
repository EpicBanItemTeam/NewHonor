package com.github.euonmyoji.newhonor.command;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.configuration.PlayerData;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;

import static org.spongepowered.api.text.Text.of;

@SuppressWarnings("ConstantConditions")
class SettingsArgs {

    static CommandSpec usehonor = CommandSpec.builder()
            .arguments(GenericArguments.bool(of("boolean")))
            .executor((src, args) -> {
                if (src instanceof User) {
                    boolean use = args.<Boolean>getOne(of("boolean")).get();
                    PlayerData pd = new PlayerData((User) src);
                    pd.usehonor(use);
                    NewHonor.doSomething(pd);
                    src.sendMessage(of("[NewHonor]change settings successful"));
                    return CommandResult.success();
                }
                src.sendMessage(of("[NewHonor]You are not a user"));
                return CommandResult.empty();
            })
            .build();

    static CommandSpec enableEffects = CommandSpec.builder()
            .arguments(GenericArguments.bool(of("boolean")))
            .executor((src, args) -> {
                if (src instanceof User) {
                    boolean enable = args.<Boolean>getOne(of("boolean")).get();
                    PlayerData pd = new PlayerData((User) src);
                    pd.enableEffects(enable);
                    NewHonor.doSomething(pd);
                    src.sendMessage(of("[NewHonor]change settings successful"));
                    return CommandResult.success();
                }
                src.sendMessage(of("[NewHonor]You are not a user"));
                return CommandResult.empty();
            })
            .build();
}
