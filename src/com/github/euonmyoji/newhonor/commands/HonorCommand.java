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
import org.spongepowered.api.service.pagination.PaginationList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.spongepowered.api.text.Text.builder;
import static org.spongepowered.api.text.Text.of;
import static org.spongepowered.api.text.action.TextActions.*;

/**
 * @author yinyangshi
 */
public class HonorCommand {
    private static String ADMIN_PERMISSION = "newhonor.admin";

    @SuppressWarnings("ConstantConditions")
    private static CommandSpec use = CommandSpec.builder()
            .arguments(GenericArguments.string(of("id")))
            .executor((src, args) -> {
                if (src instanceof Player) {
                    Task.builder().execute(() -> {
                        PlayerData pd = new PlayerData((User) src);
                        if (pd.setUse(args.<String>getOne(of("id")).get())) {
                            src.sendMessage(of("[头衔插件]修改使用头衔成功"));
                        } else {
                            src.sendMessage(of("[头衔插件]修改使用头衔失败，可能原因:[头衔未拥有或不存在,储存数据时异常]"));
                            pd.setUse("default");
                            src.sendMessage(of("[头衔插件]已修改使用头衔为默认头衔default"));
                        }
                        NewHonor.doSomething(pd);
                    }).async().name("newhonor - Player Change Using Honor").submit(NewHonor.plugin);
                } else {
                    src.sendMessage(of("[头衔插件]未知发送者,目前该指令近支持玩家自己发送指令修改自己设置。"));
                }
                return CommandResult.success();
            })
            .build();

    @SuppressWarnings("ConstantConditions")
    private static CommandSpec list = CommandSpec.builder()
            .arguments(GenericArguments.onlyOne(GenericArguments.userOrSource(of("user"))))
            .executor((src, args) -> {
                User user = args.<User>getOne(of("user")).get();
                if (user.getName().equals(src.getName()) || src.hasPermission(ADMIN_PERMISSION)) {
                    Task.builder().execute(() -> {
                        PlayerData pd = new PlayerData(user);
                        Optional<List<String>> honors = pd.getHonors();
                        if (honors.isPresent()) {
                            PaginationList.Builder builder = PaginationList.builder();
                            builder.title(of("拥有的头衔")).padding(of("-"));
                            HonorData.getHonorText(pd.getUse())
                                    .ifPresent(text -> builder.header(of(String.format("%s正在使用的头衔:", user.getName()), text)));
                            builder.contents(honors.get().stream()
                                    .map(HonorData::getHonorText)
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .collect(Collectors.toList()));
                            builder.build().sendTo(src);
                            Task.builder().async().name("NewHonor - check" + user.getName() + "has honors")
                                    .execute(() -> honors.get().forEach(s -> {
                                        if (!HonorData.getHonorText(s).isPresent()) {
                                            pd.take(s);
                                        }
                                    })).submit(NewHonor.plugin);
                        } else {
                            src.sendMessage(of("[头衔插件]你目前没有任何头衔"));
                        }
                    }).async().name("newhonor - List Player Honors").submit(NewHonor.plugin);
                    return CommandResult.success();
                } else {
                    src.sendMessage(of("你没有权限查看别人所拥有的权限[权限节点:newhonor.admin]"));
                }
                return CommandResult.empty();
            })
            .build();

    private static CommandSpec settings = CommandSpec.builder()
            .permission("newhonor.settings")
            .executor((src, args) -> {
                src.sendMessage(of("-------------------------------------"));
                src.sendMessage(of("#true为开启 false为关闭"));
                src.sendMessage(builder().append(of("/honor settings usehonor true/false  是否使用头衔"))
                        .onClick(suggestCommand("/honor settings usehonor ")).onHover(showText(of("左键后输入true或者false更改设置"))).build());
                src.sendMessage(builder().append(of("/honor settings enableeffects true/false  启用头衔药水效果"))
                        .onClick(suggestCommand("/honor settings enableeffects ")).onHover(showText(of("左键后输入true或者false更改设置"))).build());
                src.sendMessage(of("-------------------------------------"));
                return CommandResult.success();
            })
            .child(SettingsArgs.usehonor, "usehonor")
            .child(SettingsArgs.enableEffects, "enableeffects")
            .build();

    private static CommandSpec stats = CommandSpec.builder()
            .permission(ADMIN_PERMISSION)
            .executor((src, args) -> {
                src.sendMessage(of("/honor stats allHonors"));
                return CommandResult.success();
            })
            .child(StatsCommand.allHonors, "allHonors")
            .build();

    private static CommandSpec admin = CommandSpec.builder()
            .permission(ADMIN_PERMISSION)
            .executor((src, args) -> {
                src.sendMessage(of("-------------------------------------"));
                src.sendMessage(of(""));
                src.sendMessage(of("/honor admin effects <honorID> <effectsID>  给头衔设置药水效果"));
                src.sendMessage(of("/honor admin add <honorID> <效果>      添加头衔"));
                src.sendMessage(of("/honor admin set <honorID> <效果>      设置头衔"));
                src.sendMessage(of("/honor admin delete <honorID>          删除头衔"));
                src.sendMessage(of("/honor admin give <玩家(们)> <honorID> 给予玩家(们)头衔 "));
                src.sendMessage(of("/honor admin take <玩家(们)> <honorID> 拿走玩家(们)头衔 "));
                src.sendMessage(of("/honor admin list              [假功能]显示全部已添加头衔"));
                src.sendMessage(of("/honor admin reload            重载配置文件并更新缓存"));
                src.sendMessage(of("/honor admin refresh           更新缓存"));
                src.sendMessage(of("-------------------------------------"));
                return CommandResult.success();
            })
            .child(AdminCommand.list, "list")
            .child(AdminCommand.add, "add")
            .child(AdminCommand.delete, "delete")
            .child(AdminCommand.set, "set")
            .child(AdminCommand.give, "give")
            .child(AdminCommand.take, "take")
            .child(AdminCommand.refresh, "refresh")
            .child(AdminCommand.reload, "reload")
            .child(AdminCommand.effects, "effects")
            .build();

    private static CommandSpec effects = CommandSpec.builder()
            .permission(ADMIN_PERMISSION)
            .executor((src, args) -> {
                src.sendMessage(of("/honor effects delete <effectsID>  删除一个药水效果组"));
                src.sendMessage(of("/honor effects set <effectsID> <effectID> <level> 给一个效果组设置一个药水效果"));
                src.sendMessage(of("/honor effects remove <effectID> <effectsID>        移除一个效果组的药水效果"));
                src.sendMessage(of("/honor effects info <effectsID>   查看一个药水效果组信息"));
                src.sendMessage(of("/honor effects list 查看所有可用药水效果id"));
                return CommandResult.success();
            })
            .child(EffectsCommand.delete, "delete")
            .child(EffectsCommand.set, "set")
            .child(EffectsCommand.remove, "remove")
            .child(EffectsCommand.info, "info")
            .child(EffectsCommand.listAllPotionEffects, "listAllPotionEffects")
            .child(EffectsCommand.listAllCreatedEffects, "listAllCreatedEffects")
            .build();

    public static CommandSpec honor = CommandSpec.builder()
            .permission(ADMIN_PERMISSION)
            .executor((src, args) -> {
                src.sendMessage(of("-------------------------------------"));
                src.sendMessage(of(""));
                src.sendMessage(of("/honor admin           管理员用指令"));
                src.sendMessage(builder().append(of("/honor list [用户]     列出拥有的头衔")).onClick(runCommand("/honor list")).onHover(showText(of("点击显示自己拥有的头衔"))).build());
                src.sendMessage(builder().append(of("/honor settings        修改设置")).onClick(runCommand("/honor settings")).onHover(showText(of("点击执行/honor settings"))).build());
                src.sendMessage(of("/honor effects        头衔药水效果"));
                src.sendMessage(of("/honor stats          统计(同步)一些数据 [卡服警告]"));
                src.sendMessage(of("-------------------------------------"));
                return CommandResult.success();
            })
            .child(settings, "settings")
            .child(admin, "admin")
            .child(use, "use")
            .child(list, "list")
            .child(effects, "effects")
            .child(stats, "stats")
            .build();

}
