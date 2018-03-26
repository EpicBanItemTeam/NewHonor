package com.github.euonmyoji.newhonor.commands;

import com.github.euonmyoji.newhonor.configuration.PlayerData;
import com.github.euonmyoji.newhonor.NewHonor;
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
                    if (show) {
                        pd.getHonor().ifPresent(text -> NewHonor.usinghonor.put(((User) src).getUniqueId(), text));
                    } else {
                        NewHonor.usinghonor.remove(((User) src).getUniqueId());
                    }
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
                    if (show) {
                        pd.getHonor().ifPresent(text -> ((User) src).offer(Keys.DISPLAY_NAME, Text.of(text, src.getName())));
                    } else {
                        ((User) src).offer(Keys.DISPLAY_NAME, Text.of(src.getName()));
                    }
                    src.sendMessage(Text.of("[头衔插件]修改设置成功"));
                    return CommandResult.success();
                }
                src.sendMessage(Text.of("[头衔插件]未知发送者,目前该指令近支持玩家自己发送指令修改自己设置。"));
                return CommandResult.empty();
            })
            .build();
}
