package com.github.euonmyoji.newhonor.commands;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.configuration.HonorData;
import com.github.euonmyoji.newhonor.configuration.PlayerData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class StatsCommand {
    static CommandSpec allHonors = CommandSpec.builder()
            .executor((src, args) -> {
                try {
                    Task.builder().async().execute(() -> {
                        if (!HonorData.check(Sponge.getServer().getOnlinePlayers().parallelStream()
                                .map(PlayerData::new)
                                .map(PlayerData::getHonors)
                                .map(strings -> strings.orElse(Collections.emptyList()))
                                .filter(strings -> !strings.isEmpty())
                                .flatMap(List::stream)
                                .filter(HonorData::isVirtual)
                                .collect(Collectors.toList()))) {
                            src.sendMessage(Text.of("[头衔插件]统计时发生错误"));
                        }
                        src.sendMessage(Text.of("[头衔插件]统计结束"));
                    }).name("NewHonor - checkAllHonors").submit(NewHonor.plugin);
                } catch (Exception e) {
                    src.sendMessage(Text.of("[头衔插件]已经有统计任务在运行了！"));
                }
                return CommandResult.empty();
            })
            .build();
}
