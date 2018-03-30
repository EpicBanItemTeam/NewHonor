package com.github.euonmyoji.newhonor.commands;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.configuration.HonorData;
import com.github.euonmyoji.newhonor.configuration.PlayerData;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;

public class HonorCommand {
    @SuppressWarnings("ConstantConditions")
    private static CommandSpec use = CommandSpec.builder()
            .arguments(GenericArguments.string(Text.of("id")))
            .executor((src, args) -> {
                if (src instanceof Player) {
                    Task.builder().execute(() -> {
                        PlayerData pd = new PlayerData((User) src);
                        if (pd.setUse(args.<String>getOne(Text.of("id")).get())) {
                            src.sendMessage(Text.of("[头衔插件]修改使用头衔成功"));
                            NewHonor.doSomething(pd);
                        } else {
                            src.sendMessage(Text.of("[头衔插件]修改使用头衔失败，可能原因:[头衔未拥有或不存在,储存数据时异常]"));
                            pd.setUse("default");
                            if (pd.isShowHonor()) {
                                pd.getHonor().ifPresent(text -> NewHonor.honorTextCache.put(((User) src).getUniqueId(), text));
                            } else {
                                NewHonor.honorTextCache.remove(((User) src).getUniqueId());
                            }
                            src.sendMessage(Text.of("[头衔插件]已修改使用头衔为默认头衔default"));
                        }
                    }).async().name("newhonor - Player Change Using Honor").submit(NewHonor.plugin);
                } else {
                    src.sendMessage(Text.of("[头衔插件]未知发送者,目前该指令近支持玩家自己发送指令修改自己设置。"));
                }
                return CommandResult.success();
            })
            .build();

    @SuppressWarnings("ConstantConditions")
    private static CommandSpec list = CommandSpec.builder()
            .arguments(GenericArguments.onlyOne(GenericArguments.userOrSource(Text.of("user"))))
            .executor((src, args) -> {
                User user = args.<User>getOne(Text.of("user")).get();
                if (user.getName().equals(src.getName()) || src.hasPermission("newhonor.admin")) {
                    Task.builder().execute(() -> {
                        PlayerData pd = new PlayerData(user);
                        Optional<List<String>> honors = pd.getHonors();
                        if (honors.isPresent()) {
                            src.sendMessage(Text.of("---" + user.getName() + "拥有的头衔---"));
                            honors.get().forEach(id -> {
                                if (HonorData.getHonor(id).isPresent()) {
                                    src.sendMessage(Text.of("头衔id:" + id + ",效果为:", HonorData.getHonor(id).get()));
                                } else {
                                    src.sendMessage(Text.of("你的头衔id:" + id + ",已被服务器删除"));
                                    pd.take(id);
                                    pd.setUse("default");
                                }
                            });
                        } else {
                            src.sendMessage(Text.of("[头衔插件]你目前没有任何头衔"));
                        }
                    }).async().name("newhonor - List Player Honors").submit(NewHonor.plugin);
                    return CommandResult.success();
                } else {
                    src.sendMessage(Text.of("你没有权限查看别人所拥有的权限[权限节点:newhonor.admin"));
                }
                return CommandResult.empty();
            })
            .build();

    private static CommandSpec settings = CommandSpec.builder()
            .permission("newhonor.settings")
            .executor((src, args) -> {
                src.sendMessage(Text.of("-------------------------------------"));
                src.sendMessage(Text.of(""));
                src.sendMessage(Text.of("/honor settings showhonor true/false  显示头衔在聊天栏"));
                src.sendMessage(Text.of("/honor settings displayhonor true/false  显示头衔在头顶[名字前]"));
                src.sendMessage(Text.of("-------------------------------------"));
                return CommandResult.success();
            })
            .child(SettingsChildCommand.showhonor, "showhonor")
            .child(SettingsChildCommand.displayhonor, "displayhonor")
            .build();

    private static CommandSpec admin = CommandSpec.builder()
            .permission("newhonor.admin")
            .executor((src, args) -> {
                src.sendMessage(Text.of("-------------------------------------"));
                src.sendMessage(Text.of(""));
                src.sendMessage(Text.of("/honor admin effects <honorID> <effectsID>  给头衔设置药水效果"));
                src.sendMessage(Text.of("/honor admin add <honorID> <效果>      添加头衔"));
                src.sendMessage(Text.of("/honor admin set <honorID> <效果>      设置头衔"));
                src.sendMessage(Text.of("/honor admin delete <honorID>          删除头衔"));
                src.sendMessage(Text.of("/honor admin give <玩家(们)> <honorID> 给予玩家(们)头衔 "));
                src.sendMessage(Text.of("/honor admin take <玩家(们)> <honorID> 拿走玩家(们)头衔 "));
                src.sendMessage(Text.of("/honor admin list              [假功能]显示全部已添加头衔"));
                src.sendMessage(Text.of("/honor admin reload            重载配置文件并更新缓存"));
                src.sendMessage(Text.of("/honor admin refresh           更新缓存"));
                src.sendMessage(Text.of("-------------------------------------"));
                return CommandResult.success();
            })
            .child(AdminChildCommand.list, "list")
            .child(AdminChildCommand.add, "add")
            .child(AdminChildCommand.delete, "delete")
            .child(AdminChildCommand.set, "set")
            .child(AdminChildCommand.give, "give")
            .child(AdminChildCommand.take, "take")
            .child(AdminChildCommand.refresh, "refresh")
            .child(AdminChildCommand.reload, "reload")
            .child(AdminChildCommand.effects, "effects")
            .build();

    private static CommandSpec effects = CommandSpec.builder()
            .permission("newhonor.admin")
            .executor((src, args) -> {
                src.sendMessage(Text.of("/honor effects delete <effectsID>  删除一个药水效果组"));
                src.sendMessage(Text.of("/honor effects set <effectsID> <effectID> <level> 给一个效果组设置一个药水效果"));
                src.sendMessage(Text.of("/honor effects remove <effectID> <effectsID>        移除一个效果组的药水效果"));
                src.sendMessage(Text.of("/honor effects info <effectsID>   查看一个药水效果组信息"));
                src.sendMessage(Text.of("/honor effects list 查看所有可用药水效果id"));
                return CommandResult.success();
            })
            .child(EffectsCommand.delete, "delete")
            .child(EffectsCommand.set, "set")
            .child(EffectsCommand.remove, "remove")
            .child(EffectsCommand.info, "info")
            .child(EffectsCommand.list, "list")
            .build();

    public static CommandSpec honor = CommandSpec.builder()
            .permission("newhonor.use")
            .executor((src, args) -> {
                src.sendMessage(Text.of("-------------------------------------"));
                src.sendMessage(Text.of("插件已发布在http://www.mcbbs.net/thread-785478-1-1.html"));
                src.sendMessage(Text.of(""));
                src.sendMessage(Text.of("/honor admin           管理员用指令"));
                src.sendMessage(Text.of("/honor list [用户]     列出拥有的头衔"));
                src.sendMessage(Text.of("/honor use <honorID>  使用头衔"));
                src.sendMessage(Text.of("/honor settings        修改设置"));
                src.sendMessage(Text.of("/honor effects        头衔药水效果"));
                src.sendMessage(Text.of("-------------------------------------"));
                return CommandResult.success();
            })
            .child(settings, "settings")
            .child(admin, "admin")
            .child(use, "use")
            .child(list, "list")
            .child(effects, "effects")
            .build();

}
