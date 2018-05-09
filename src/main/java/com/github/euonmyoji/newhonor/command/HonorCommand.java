package com.github.euonmyoji.newhonor.command;

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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.euonmyoji.newhonor.configuration.LanguageManager.*;
import static org.spongepowered.api.text.Text.of;
import static org.spongepowered.api.text.action.TextActions.runCommand;
import static org.spongepowered.api.text.action.TextActions.showText;

/**
 * @author yinyangshi
 */
public class HonorCommand {
    private static String ADMIN_PERMISSION = "newhonor.admin";

    private static CommandSpec use = CommandSpec.builder()
            .arguments(GenericArguments.string(of("id")))
            .executor((src, args) -> {
                if (src instanceof Player) {
                    Task.builder().execute(() -> {
                        PlayerData pd = new PlayerData((User) src);
                        if (pd.setUse(args.<String>getOne(of("id")).orElseThrow(NoSuchFieldError::new))) {
                            src.sendMessage(getText("newhonor.changehonor.succeed"));
                        } else {
                            pd.setUse("");
                            pd.init();
                            src.sendMessage(getText("newhonor.changehonor.failed"));
                        }
                        NewHonor.doSomething(pd);
                    }).async().name("newhonor - Player Change Using Honor").submit(NewHonor.plugin);
                } else {
                    src.sendMessage(getText("newhonor.changehonor.unknownsource"));
                }
                return CommandResult.success();
            })
            .build();

    private static CommandSpec list = CommandSpec.builder()
            .arguments(GenericArguments.optional(GenericArguments.user(of("user"))))
            .executor((src, args) -> {
                Optional<User> optionalUser = args.getOne(of("user"));
                boolean typedUser = optionalUser.isPresent();
                boolean pass = src.getName().equals(optionalUser.map(User::getName).orElse(null))
                        || src.hasPermission(ADMIN_PERMISSION);
                User user = typedUser ? optionalUser.get()
                        : src instanceof User ? (User) src : null;
                boolean execute = typedUser ? pass : user != null;
                if (execute) {
                    Task.builder().execute(() -> {
                        PlayerData pd = new PlayerData(user);
                        Optional<List<String>> honors = pd.getHonors();
                        if (honors.isPresent()) {
                            if (honors.get().isEmpty()) {
                                src.sendMessage(getText("newhonor.listhonors.empty"));
                            }
                            PaginationList.Builder builder = PaginationList.builder()
                                    .title(langBuilder("newhonor.listhonors.title").replace("%ownername%", user.getName()).build()).padding(of("-"));
                            String usingID = pd.getUsingHonorID();
                            HonorData.getHonorRawText(usingID)
                                    .ifPresent(text -> builder.header(langBuilder("newhonor.listhonors.header")
                                            .replace("%ownername", user.getName())
                                            .replace("%honor%", text)
                                            .replace("%effectsID%", HonorData.getEffectsID(usingID).orElse("null"))
                                            .build()));
                            List<Text> texts = honors.get().stream()
                                    .map(id -> HonorData.getHonorRawText(id).map(honor -> Text.builder()
                                            //显示头衔 药水效果组
                                            .append(langBuilder("newhonor.listhonors.contexts")
                                                    .replace("%honorid%", id)
                                                    .replace("%honor%", honor)
                                                    .replace("%effectsID%", HonorData.getEffectsID(id).orElse("null"))
                                                    .build())
                                            .onHover(showText(langBuilder("newhonor.listhonors.clickuse")
                                                    .replace("%honor%", honor)
                                                    .replace("%honorid%", id)
                                                    .build()))
                                            .onClick(runCommand("/honor use " + id))
                                            .build()))
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .collect(Collectors.toList());
                            builder.contents(texts).build().sendTo(src);
                            Task.builder().async().name("NewHonor - check" + user.getName() + "has honors")
                                    .execute(() -> honors.get().forEach(s -> {
                                        if (!HonorData.getHonorRawText(s).isPresent()) {
                                            pd.take(s);
                                        }
                                    })).submit(NewHonor.plugin);
                        } else {
                            src.sendMessage(of("unknown error"));
                        }
                    }).async().name("newhonor - List Player" + user.getName() + " Honors").submit(NewHonor.plugin);
                    return CommandResult.success();
                } else {
                    src.sendMessage(getText("newhonor.listhonors.nopermission"));
                }
                return CommandResult.empty();
            })
            .build();

    private static CommandSpec settings = CommandSpec.builder()
            .permission("newhonor.settings")
            .executor((src, args) -> {
                src.sendMessage(of("-------------------------------------"));
                src.sendMessage(Text.builder().append(of("/honor settings usehonor true/false -", getCommandDescribe("settings.usehonor")))
                        .onClick(TextActions.suggestCommand("/honor settings usehonor -")).onHover(showText(getText("newhonor.hovermessage.settings.usehonor"))).build());
                src.sendMessage(Text.builder().append(of("/honor settings enableeffects true/false -", getCommandDescribe("settings.enableeffects")))
                        .onClick(TextActions.suggestCommand("/honor settings enableeffects -")).onHover(showText(getText("newhonor.hovermessage.settings.enableeffects"))).build());
                src.sendMessage(of("-------------------------------------"));
                return CommandResult.success();
            })
            .child(SettingsArgs.usehonor, "usehonor")
            .child(SettingsArgs.enableEffects, "enableeffects")
            .build();

    private static CommandSpec admin = CommandSpec.builder()
            .permission(ADMIN_PERMISSION)
            .executor((src, args) -> {
                src.sendMessage(of("-------------------------------------"));
                src.sendMessage(of(""));
                src.sendMessage(of("/honor admin effects <honorID> <effectsID> -", getCommandDescribe("admin.effects")));
                src.sendMessage(of("/honor admin add <honorID> <honor> -", getCommandDescribe("admin.add")));
                src.sendMessage(of("/honor admin set <honorID> <honor> -", getCommandDescribe("admin.set")));
                src.sendMessage(of("/honor admin delete <honorID> -", getCommandDescribe("admin.delete")));
                src.sendMessage(of("/honor admin give <user(s)> <honorID> -", getCommandDescribe("admin.give")));
                src.sendMessage(of("/honor admin take <user(s)> <honorID> -", getCommandDescribe("admin.take")));
                src.sendMessage(of("/honor admin list -", getCommandDescribe("admin.list")));
                src.sendMessage(of("/honor admin reload -", getCommandDescribe("admin.reload")));
                src.sendMessage(of("/honor admin refresh -", getCommandDescribe("admin.refresh")));
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
                src.sendMessage(of("/honor effects set <effectsID> <PotionEffectID> <level> -", getCommandDescribe("effects.set")));
                src.sendMessage(of("/honor effects delete <effectsID> -", getCommandDescribe("effects.delete")));
                src.sendMessage(of("/honor effects remove <PotionEffectID> <effectsID> -", getCommandDescribe("effects.remove")));
                src.sendMessage(of("/honor effects info <effectsID> -", getCommandDescribe("effects.info")));
                src.sendMessage(of("/honor effects listAllPotionEffects -", getCommandDescribe("effects.listAllPotionEffects")));
                src.sendMessage(of("/honor effects listAllCreatedEffects -", getCommandDescribe("effects.listAllCreatedEffects")));
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
            .permission("newhonor.use")
            .executor((src, args) -> {
                src.sendMessage(of("----------NewHonorV" + NewHonor.VERSION + "----------"));
                src.sendMessage(of("/honor admin  -", getCommandDescribe("admin")));
                src.sendMessage(Text.builder().append(of("/honor list [User]   -", getCommandDescribe("list")))
                        .onClick(runCommand("/honor list")).onHover(showText(getText("newhonor.command.hovermessage.list"))).build());
                src.sendMessage(Text.builder().append(of("/honor settings    -", getCommandDescribe("settings")))
                        .onClick(runCommand("/honor settings")).onHover(showText(getText("newhonor.command.hovermessage.settings"))).build());
                src.sendMessage(of("/honor effects    -", getCommandDescribe("effects")));
                src.sendMessage(of("-------------------------------------"));
                return CommandResult.success();
            })
            .child(settings, "settings")
            .child(admin, "admin")
            .child(use, "use")
            .child(list, "list")
            .child(effects, "effects")
            .build();
}
