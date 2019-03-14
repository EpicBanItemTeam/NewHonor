package com.github.euonmyoji.newhonor.command;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.configuration.PlayerConfig;
import com.github.euonmyoji.newhonor.command.args.HonorIDArg;
import com.github.euonmyoji.newhonor.command.args.SettingsChild;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.util.Identifiable;

import java.util.HashMap;
import java.util.UUID;

import static com.github.euonmyoji.newhonor.manager.SpongeLanguageManager.getText;
import static org.spongepowered.api.text.Text.of;
import static org.spongepowered.api.text.action.TextActions.runCommand;
import static org.spongepowered.api.text.action.TextActions.showText;

/**
 * @author yinyangshi
 */
public final class HonorCommand {
    private static final HashMap<UUID, Integer> USE_CD = new HashMap<>();
    public static String ADMIN_PERMISSION = "newhonor.admin";
    private static String ID_KEY = "id";
    private static CommandSpec data = CommandSpec.builder()
            .permission(ADMIN_PERMISSION)
            .executor((src, args) -> {
                src.sendMessage(of("/honor data updateToSql   Update PlayerData to Sql"));
                src.sendMessage(of("/honor data downloadFromSql   Download PlayerData from Sql"));
                return CommandResult.success();
            })
            .child(SqlCommand.downloadFromSql, "downloadFromSql")
            .child(SqlCommand.updateToSql, "updateToSql")
            .build();

    private static CommandSpec use = CommandSpec.builder()
            .arguments(new HonorIDArg(of(ID_KEY)))
            .executor((src, args) -> {
                if (!(src instanceof Identifiable)) {
                    src.sendMessage(getText("newhonor.changehonor.unknownsource"));
                    return CommandResult.empty();
                }
                if (!src.hasPermission(ADMIN_PERMISSION) && USE_CD.containsKey(((Identifiable) src).getUniqueId())) {
                    int cd = USE_CD.get(((Identifiable) src).getUniqueId());
                    src.sendMessage(of("[NewHonor]You should wait " + cd + " second(s) to change use honor"));
                    return CommandResult.empty();
                }
                Task.builder().execute(() -> {
                    try {
                        PlayerConfig pd = PlayerConfig.get((User) src);
                        pd.checkPermission();
                        if (pd.setUseHonor(args.<String>getOne(of(ID_KEY)).orElseThrow(NoSuchFieldError::new))) {
                            src.sendMessage(getText("newhonor.changehonor.succeed"));
                        } else {
                            pd.setUseHonor("");
                            pd.init();
                            src.sendMessage(getText("newhonor.changehonor.failed"));
                        }
                        NewHonor.updateCache(pd);
                        if (!src.hasPermission(ADMIN_PERMISSION)) {
                            USE_CD.put(((Identifiable) src).getUniqueId(), 9);
                        }
                    } catch (Exception e) {
                        src.sendMessage(getText("[NewHonor] error!"));
                        NewHonor.logger.warn("error while changing using honor", e);
                    }
                }).async().name("newhonor - Player Change Using Honor").submit(NewHonor.plugin);
                return CommandResult.success();
            })
            .build();

    private static CommandSpec list = CommandSpec.builder()
            .arguments(GenericArguments.optional(GenericArguments.user(of("user"))))
            .executor(ListHonorCommand::execute)
            .build();

    private static CommandSpec settings = CommandSpec.builder()
            .permission("newhonor.settings")
            .executor((src, args) -> {
                src.sendMessage(of("-------------------------------------"));
                src.sendMessage(Text.builder().append(of("/honor settings usehonor true/false -", getText("newhonor.command.describe.settings.usehonor")))
                        .onClick(TextActions.suggestCommand("/honor settings usehonor ")).onHover(showText(getText("newhonor.hovermessage.settings.usehonor"))).build());

                src.sendMessage(Text.builder().append(of("/honor settings enableeffects true/false -", getText("newhonor.command.describe.settings.enableeffects")))
                        .onClick(TextActions.suggestCommand("/honor settings enableeffects ")).onHover(showText(getText("newhonor.hovermessage.settings.enableeffects"))).build());

                src.sendMessage(Text.builder().append(of("/honor settings autochange true/false -", getText("newhonor.command.describe.settings.autochange")))
                        .onClick(TextActions.suggestCommand("/honor settings autochange ")).onHover(showText(getText("newhonor.hovermessage.settings.autochange"))).build());

                src.sendMessage(Text.builder().append(of("/honor settings liststyle <style> -", getText("newhonor.command.describe.settings.liststyle")))
                        .onClick(TextActions.suggestCommand("/honor settings liststyle ")).onHover(showText(getText("newhonor.hovermessage.settings.liststyle"))).build());
                src.sendMessage(of("-------------------------------------"));
                return CommandResult.success();
            })
            .child(SettingsChild.usehonor, "usehonor")
            .child(SettingsChild.enableEffects, "enableeffects")
            .child(SettingsChild.autochange, "autochange")
            .child(SettingsChild.listStyle, "liststyle")
            .build();

    private static CommandSpec admin = CommandSpec.builder()
            .permission(ADMIN_PERMISSION)
            .executor((src, args) -> {
                src.sendMessage(of("-------------------------------------"));
                src.sendMessage(of(""));
                src.sendMessage(of("/honor admin effects <honorID> <effectsID> -", getText("newhonor.command.describe.admin.effects")));
                src.sendMessage(of("/honor admin add <honorID> <honor> -", getText("newhonor.command.describe.admin.add")));
                src.sendMessage(of("/honor admin set <honorID> <honor> -", getText("newhonor.command.describe.admin.set")));
                src.sendMessage(of("/honor admin delete <honorID> -", getText("newhonor.command.describe.admin.delete")));
                src.sendMessage(of("/honor admin give <user(s)> <honorID> -", getText("newhonor.command.describe.admin.give")));
                src.sendMessage(of("/honor admin take <user(s)> <honorID> -", getText("newhonor.command.describe.admin.take")));
                src.sendMessage(of("/honor admin list -", getText("newhonor.command.describe.admin.list")));
                src.sendMessage(of("/honor admin reload -", getText("newhonor.command.describe.admin.reload")));
                src.sendMessage(of("/honor admin refresh -", getText("newhonor.command.describe.admin.refresh")));
                src.sendMessage(of("-------------------------------------"));
                return CommandResult.success();
            })
            .child(AdminCommand.list, "list", "l")
            .child(AdminCommand.add, "add", "create", "a")
            .child(AdminCommand.delete, "delete")
            .child(AdminCommand.set, "set")
            .child(AdminCommand.give, "give")
            .child(AdminCommand.take, "take")
            .child(AdminCommand.refresh, "refresh")
            .child(AdminCommand.reload, "reload", "r")
            .child(AdminCommand.effects, "effects")
            .build();

    private static CommandSpec effects = CommandSpec.builder()
            .permission(ADMIN_PERMISSION)
            .executor((src, args) -> {
                src.sendMessage(of("/honor effects set <effectsID> <PotionEffectID> <level> -", getText("newhonor.command.describe.effects.set")));
                src.sendMessage(of("/honor effects delete <effectsID> -", getText("newhonor.command.describe.effects.delete")));
                src.sendMessage(of("/honor effects remove <PotionEffectID> <effectsID> -", getText("newhonor.command.describe.effects.remove")));
                src.sendMessage(of("/honor effects info <effectsID> -", getText("newhonor.command.describe.effects.info")));
                src.sendMessage(of("/honor effects listAllPotionEffects -", getText("newhonor.command.describe.effects.listAllPotionEffects")));
                src.sendMessage(of("/honor effects listAllCreatedEffects -", getText("newhonor.command.describe.effects.listAllCreatedEffects")));
                return CommandResult.success();
            })
            .child(EffectsCommand.DELETE, "delete")
            .child(EffectsCommand.SET, "set")
            .child(EffectsCommand.REMOVE, "remove")
            .child(EffectsCommand.INFO, "info")
            .child(EffectsCommand.LIST_ALL_POTION_EFFECTS, "listAllPotionEffects")
            .child(EffectsCommand.LIST_ALL_CREATED_EFFECTS, "listAllCreatedEffects")
            .child(EffectsCommand.LIST_ALL_PARTICLE, "listAllParticle")
            .build();

    private static CommandSpec help = CommandSpec.builder()
            .executor((src, args) -> help(src))
            .build();

    public static CommandSpec honor = CommandSpec.builder()
            .permission("newhonor.use")
            .arguments(GenericArguments.optionalWeak(GenericArguments.onlyOne(GenericArguments.userOrSource(Text.of("user")))))
            .executor((src, args) -> {
                if (ListHonorCommand.execute(src, args) == CommandResult.empty()) {
                    src.sendMessage(Text.of("[NewHonor]Do you want get help? /honor help"));
                }
                return CommandResult.success();
            })
            .child(settings, "settings")
            .child(admin, "admin")
            .child(use, "use")
            .child(list, "list")
            .child(effects, "effects")
            .child(data, "data")
            .child(help, "help")
            .child(AdminCommand.reload, "reload")
            .child(AdminCommand.refresh, "refresh")
            .build();

    static {
        Task.builder().execute(() -> new HashMap<>(USE_CD).forEach((uuid, integer) -> {
            USE_CD.put(uuid, integer - 1);
            if (USE_CD.get(uuid) <= 0) {
                USE_CD.remove(uuid);
            }
        })).async().intervalTicks(20).submit(NewHonor.plugin);
    }

    //00 01 02 03 04 05 06 07 08
    //09 10 11 12 13 14 15 16 17
    //18 19 20 21 22 23 24 25 26
    //27 28 29 30 31 32 33 34 35
    //36 37 38 39 40 41 42 43 44
    //45 46 47 48 49 50 51 52 53

    private HonorCommand() {
        throw new UnsupportedOperationException();
    }

    private static CommandResult help(CommandSource src) {
        src.sendMessage(of("----------NewHonorV" + NewHonor.VERSION + "----------"));
        src.sendMessage(of("/honor admin  -", getText("newhonor.command.describe.admin")));
        src.sendMessage(Text.builder().append(of("/honor list [User]   -", getText("newhonor.command.describe.list")))
                .onClick(runCommand("/honor list")).onHover(showText(getText("newhonor.command.hovermessage.list"))).build());
        src.sendMessage(Text.builder().append(of("/honor settings    -", getText("newhonor.command.describe.settings")))
                .onClick(runCommand("/honor settings")).onHover(showText(getText("newhonor.command.hovermessage.settings"))).build());
        src.sendMessage(of("/honor effects    -", getText("newhonor.command.describe.effects")));
        src.sendMessage(of("-------------------------------------"));
        return CommandResult.success();
    }
}
