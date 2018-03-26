package com.github.euonmyoji.newhonor.commands;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.configuration.PlayerData;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

@SuppressWarnings("ConstantConditions")
public class SettingsChildCommand {

    static CommandSpec showhonor = CommandSpec.builder()
            .arguments(GenericArguments.bool(Text.of("boolean")))
            .executor((src, args) -> {
                if (src instanceof User) {
                    boolean show = args.<Boolean>getOne(Text.of("boolean")).get();
                    PlayerData pd = new PlayerData((User) src);
                    pd.showhonor(show);
                    NewHonor.doSomething(pd);
                    src.sendMessage(Text.of("[头衔插件]修改设置成功"));
                    return CommandResult.success();
                }
                src.sendMessage(Text.of("[头衔插件]未知发送者,目前该指令近支持玩家自己发送指令修改自己设置。"));
                return CommandResult.empty();
            })
            .build();

    static CommandSpec displayhonor = CommandSpec.builder()
            .arguments(GenericArguments.bool(Text.of("boolean")))
            .executor((src, args) -> {
                if (src instanceof User) {
                    boolean show = args.<Boolean>getOne(Text.of("boolean")).get();
                    PlayerData pd = new PlayerData((User) src);
                    pd.displayhonor(show);
                    NewHonor.doSomething(pd);
                    src.sendMessage(Text.of("[头衔插件]修改设置成功"));
                    return CommandResult.success();
                }
                src.sendMessage(Text.of("[头衔插件]未知发送者,目前该指令近支持玩家自己发送指令修改自己设置。"));
                return CommandResult.empty();
            })
            .build();

    static CommandSpec enableEffects = CommandSpec.builder()
            .arguments(GenericArguments.bool(Text.of("boolean")))
            .executor((src, args) -> {
                if (src instanceof User) {
                    boolean enable = args.<Boolean>getOne(Text.of("boolean")).get();
                    PlayerData pd = new PlayerData((User) src);
                    pd.enableEffects(enable);
                    NewHonor.doSomething(pd);
                    src.sendMessage(Text.of("[头衔插件]修改设置成功"));
                    return CommandResult.success();
                }
                src.sendMessage(Text.of("[头衔插件]未知发送者,目前该指令近支持玩家自己发送指令修改自己设置。"));
                return CommandResult.empty();
            })
            .build();
}
