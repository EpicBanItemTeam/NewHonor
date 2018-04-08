package com.github.euonmyoji.newhonor.commands;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.configuration.HonorData;
import com.github.euonmyoji.newhonor.configuration.PlayerData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.Collections;

class StatsCommand {
    static CommandSpec allHonors = CommandSpec.builder()
            .executor((src, args) -> {
                try {
                    Task.builder().async().execute(() -> {
                        Sponge.getServer().getOnlinePlayers().parallelStream()
                                .map(PlayerData::new)
                                .map(PlayerData::getHonors)
                                .map(strings -> strings.orElse(Collections.emptyList()))
                                .reduce((strings, strings2) -> {
                                    new ArrayList<>(strings).addAll(strings2);
                                    return strings;
                                }).ifPresent(strings -> {
                            if (!HonorData.check(strings)) {
                                src.sendMessage(Text.of("[头衔插件]统计时发生错误"));
                            }
                        });
                        src.sendMessage(Text.of("[头衔插件]统计结束"));
                    }).name("NewHonor - checkAllHonors").submit(NewHonor.plugin);
                } catch (Exception e) {
                    src.sendMessage(Text.of("[头衔插件]已经有统计任务在运行了！"));
                }
                return CommandResult.empty();
            })
            .build();
}
