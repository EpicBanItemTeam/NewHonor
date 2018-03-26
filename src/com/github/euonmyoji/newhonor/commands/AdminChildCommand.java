package com.github.euonmyoji.newhonor.commands;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.configuration.HonorData;
import com.github.euonmyoji.newhonor.configuration.PlayerData;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.HashMap;

@SuppressWarnings("ConstantConditions")
class AdminChildCommand {
    private static NewHonor plugin = NewHonor.plugin;

    static CommandSpec refresh = CommandSpec.builder()
            .executor((src, args) -> {
                updateHonor(src);
                return CommandResult.success();
            })
            .build();

    static CommandSpec give = CommandSpec.builder()
            .arguments(GenericArguments.user(Text.of("user")),
                    GenericArguments.string(Text.of("id")))
            .executor((src, args) -> {
                Collection<User> users = args.getAll(Text.of("user"));
                Collection<String> ids = args.getAll(Text.of("id"));
                Task.builder().execute(() -> users.forEach(user -> {
                    PlayerData pd = new PlayerData(user);
                    ids.forEach(id -> {
                        if (pd.give(id)) {
                            src.sendMessage(Text.of("[头衔插件]成功:给予" + user.getName() + "头衔" + id));
                            plugin.logger.info(src.getName() + "成功:给予" + user.getName() + "头衔:" + id);
                        } else {
                            src.sendMessage(Text.of("[头衔插件]失败:给予" + user.getName() + "头衔" + id));
                            plugin.logger.info(src.getName() + "失败:给予" + user.getName() + "头衔:" + id);
                        }
                    });
                })).async().name("newhonor - Give Users Honors").submit(NewHonor.plugin);
                return CommandResult.success();
            })
            .build();

    static CommandSpec take = CommandSpec.builder()
            .arguments(GenericArguments.user(Text.of("user")),
                    GenericArguments.string(Text.of("id")))
            .executor((src, args) -> {
                Collection<User> users = args.getAll(Text.of("user"));
                Collection<String> ids = args.getAll(Text.of("id"));
                Task.builder().execute(() -> {
                    users.forEach(user -> {
                        PlayerData pd = new PlayerData(user);
                        ids.forEach(id -> {
                            if (pd.take(id)) {
                                src.sendMessage(Text.of("[头衔插件]成功:移除" + user.getName() + "头衔" + id));
                                plugin.logger.info(src.getName() + "成功:移除" + user.getName() + "头衔:" + id);
                            } else {
                                src.sendMessage(Text.of("[头衔插件]失败:移除" + user.getName() + "头衔" + id));
                                plugin.logger.info(src.getName() + "失败:移除" + user.getName() + "头衔:" + id);
                            }
                        });
                    });
                    updateHonor(src);
                }).async().name("newhonor - take users honors").submit(NewHonor.plugin);
                return CommandResult.success();
            })
            .build();

    static CommandSpec list = CommandSpec.builder()
            .executor((src, args) -> {
                src.sendMessage(Text.of("[头衔插件]插件作者技术原因该功能未开放，请自己去配置文件人工查询.jpg"));
                return CommandResult.success();
            })
            .build();

    static CommandSpec set = CommandSpec.builder()
            .arguments(GenericArguments.string(Text.of("honorID")),
                    GenericArguments.string(Text.of("honor")))
            .executor((src, args) -> {
                String id = args.<String>getOne(Text.of("honorID")).get();
                String honor = args.<String>getOne(Text.of("honor")).get();
                if (HonorData.set(id, honor)) {
                    plugin.logger.info(src.getName() + "设置了头衔" + id);
                    src.sendMessage(Text.of("[头衔插件]设置头衔成功(刷新缓存)"));
                    updateHonor(src);
                    return CommandResult.success();
                }
                src.sendMessage(Text.of("[头衔插件]设置头衔失败"));
                return CommandResult.empty();
            })
            .build();

    static CommandSpec delete = CommandSpec.builder()
            .arguments(GenericArguments.string(Text.of("honorID")))
            .executor((src, args) -> {
                String id = args.<String>getOne(Text.of("honorID")).get();
                if (HonorData.delete(id)) {
                    plugin.logger.info(src.getName() + "删除了头衔" + id);
                    src.sendMessage(Text.of("[头衔插件]删除头衔成功(刷新缓存)"));
                    updateHonor(src);
                    return CommandResult.success();
                }
                src.sendMessage(Text.of("[头衔插件]删除头衔失败"));
                return CommandResult.empty();
            })
            .build();

    static CommandSpec add = CommandSpec.builder()
            .arguments(GenericArguments.string(Text.of("honorID")),
                    GenericArguments.string(Text.of("honor")))
            .executor((src, args) -> {
                String id = args.<String>getOne(Text.of("honorID")).get();
                String honor = args.<String>getOne(Text.of("honor")).get();
                if (HonorData.add(id, honor)) {
                    plugin.logger.info(src.getName() + "添加了头衔" + id);
                    src.sendMessage(Text.of("[头衔插件]添加头衔成功"));
                    return CommandResult.success();
                }
                return CommandResult.empty();
            })
            .build();

    static CommandSpec reload = CommandSpec.builder()
            .executor((src, args) -> {
                HonorData.reload();
                updateHonor(src);
                return CommandResult.success();
            })
            .build();

    /**
     * 删掉已经不再使用的缓存并更新所有缓存的text
     *
     * @param src 谁发起的刷新
     */
    private static void updateHonor(CommandSource src) {
        Task.builder().execute(() -> {
            new HashMap<>(NewHonor.honorTextCache).forEach((uuid, v) -> {
                PlayerData pd = new PlayerData(uuid);
                if (pd.isShowHonor() && pd.getHonor().isPresent()) {
                    if (pd.getHonor().isPresent()) {
                        NewHonor.honorTextCache.put(uuid, pd.getHonor().get());
                    } else {
                        pd.setUse("default");
                        pd.getHonor().ifPresent(text -> NewHonor.honorTextCache.put(uuid, text));
                    }
                } else {
                    NewHonor.honorTextCache.remove(uuid);
                }
            });
            src.sendMessage(Text.of("[头衔插件]缓存刷新完毕"));
        }).async().name("newhonor - 更新缓存").submit(NewHonor.plugin);
    }
}
