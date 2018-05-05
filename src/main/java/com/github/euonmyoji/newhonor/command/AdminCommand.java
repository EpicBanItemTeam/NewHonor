package com.github.euonmyoji.newhonor.command;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.ScoreBoardManager;
import com.github.euonmyoji.newhonor.configuration.EffectsData;
import com.github.euonmyoji.newhonor.configuration.HonorData;
import com.github.euonmyoji.newhonor.configuration.PlayerData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.pagination.PaginationList;

import java.nio.file.Files;
import java.util.Collection;
import java.util.stream.Collectors;

import static com.github.euonmyoji.newhonor.configuration.LanguageManager.getText;
import static com.github.euonmyoji.newhonor.configuration.LanguageManager.langBuilder;
import static org.spongepowered.api.text.Text.of;
import static org.spongepowered.api.text.serializer.TextSerializers.FORMATTING_CODE;

@SuppressWarnings("ConstantConditions")
class AdminCommand {
    private static NewHonor plugin = NewHonor.plugin;

    static CommandSpec refresh = CommandSpec.builder()
            .executor((src, args) -> {
                refreshCache(src);
                return CommandResult.success();
            })
            .build();

    static CommandSpec give = CommandSpec.builder()
            .arguments(GenericArguments.user(of("user")),
                    GenericArguments.string(of("id")))
            .executor((src, args) -> {
                Collection<User> users = args.getAll(of("user"));
                Collection<String> ids = args.getAll(of("id"));
                if (users.isEmpty() || ids.isEmpty()) {
                    src.sendMessage(of("[NewHonor]users is empty:" + users.isEmpty() + ",honorID is empty:" + ids.isEmpty()));
                    src.sendMessage(of("[NewHonor]give honor failed"));
                    return CommandResult.empty();
                }
                Task.builder().execute(() -> users.forEach(user -> {
                    PlayerData pd = new PlayerData(user);
                    ids.forEach(id -> {
                        if (pd.give(id)) {
                            src.sendMessage(of("[NewHonor]gave user " + user.getName() + "honor" + id + "successful"));
                            plugin.logger.info(src.getName() + "gave" + user.getName() + "honor:" + id + "successful");
                        } else {
                            src.sendMessage(of("[NewHonor]gave user " + user.getName() + "honor" + id + "failed"));
                            plugin.logger.info(src.getName() + "gave " + user.getName() + "honor:" + id + "failed");
                        }
                    });
                })).async().name("newhonor - Give Users Honors").submit(NewHonor.plugin);
                return CommandResult.success();
            })
            .build();

    static CommandSpec take = CommandSpec.builder()
            .arguments(GenericArguments.user(of("user")),
                    GenericArguments.string(of("id")))
            .executor((src, args) -> {
                Collection<User> users = args.getAll(of("user"));
                Collection<String> ids = args.getAll(of("id"));
                Task.builder().execute(() -> {
                    users.forEach(user -> {
                        PlayerData pd = new PlayerData(user);
                        ids.forEach(id -> {
                            if (pd.take(id)) {
                                src.sendMessage(of("[NewHonor]took user " + user.getName() + "honor" + id + "successful"));
                                plugin.logger.info(src.getName() + "took" + user.getName() + "honor:" + id + "successful");
                            } else {
                                src.sendMessage(of("[NewHonor]took user " + user.getName() + "honor" + id + "failed"));
                                plugin.logger.info(src.getName() + "took" + user.getName() + "honor:" + id + "failed");
                            }
                        });
                    });
                    refreshCache(src);
                }).async().name("newhonor - take users honors").submit(NewHonor.plugin);
                return CommandResult.success();
            })
            .build();

    static CommandSpec list = CommandSpec.builder()
            .executor((src, args) -> {
                Task.builder().async().execute(() -> {
                    PaginationList.Builder builder = PaginationList.builder().title(getText("newhonor.listcreatedhonors.title")).padding(of("-"));
                    builder.contents(HonorData.getAllCreatedHonors().stream().map(id -> langBuilder("newhonor.listcreatedhonors.contexts")
                            .replace("%honorid", id)
                            .replace("%honor%", FORMATTING_CODE.serialize(HonorData.getHonorText(id).orElse(of("there is something wrong"))))
                            .replace("%effectsID%", HonorData.getEffectsID(id).orElse("null"))
                            .build())
                            .filter(text -> !text.toString().contains("there is something wrong"))
                            .collect(Collectors.toList()));
                    builder.build().sendTo(src);
                }).submit(plugin);
                return CommandResult.success();
            })
            .build();

    static CommandSpec set = CommandSpec.builder()
            .arguments(GenericArguments.string(of("honorID")),
                    GenericArguments.string(of("honor")))
            .executor((src, args) -> {
                String id = args.<String>getOne(of("honorID")).get();
                String honor = args.<String>getOne(of("honor")).get();
                if (HonorData.set(id, honor)) {
                    plugin.logger.info(src.getName() + "set a honor" + id);
                    src.sendMessage(of("[NewHonor]set a honor successful(start refresh)"));
                    refreshCache(src);
                    return CommandResult.success();
                }
                src.sendMessage(of("[NewHonor]set a honor failed"));
                return CommandResult.empty();
            })
            .build();

    static CommandSpec delete = CommandSpec.builder()
            .arguments(GenericArguments.string(of("honorID")))
            .executor((src, args) -> {
                String id = args.<String>getOne(of("honorID")).get();
                if (HonorData.delete(id)) {
                    plugin.logger.info(src.getName() + "deleted a honor" + id);
                    src.sendMessage(of("[NewHonor]deleted a honor successful(start refresh)"));
                    refreshCache(src);
                    return CommandResult.success();
                }
                src.sendMessage(of("[NewHonor]delete a honor failed"));
                return CommandResult.empty();
            })
            .build();

    static CommandSpec add = CommandSpec.builder()
            .arguments(GenericArguments.string(of("honorID")),
                    GenericArguments.string(of("honor")))
            .executor((src, args) -> {
                String id = args.<String>getOne(of("honorID")).get();
                String honor = args.<String>getOne(of("honor")).get();
                if (HonorData.add(id, honor)) {
                    plugin.logger.info(src.getName() + "add a honor :" + id);
                    src.sendMessage(of("[NewHonor]add a honor successful"));
                    return CommandResult.success();
                }
                return CommandResult.empty();
            })
            .build();

    static CommandSpec effects = CommandSpec.builder()
            .arguments(GenericArguments.string(of("honorID")),
                    GenericArguments.string(of("effectsID")))
            .executor((src, args) -> {
                String id = args.<String>getOne(of("honorID")).get();
                String effectsID = args.<String>getOne(of("effectsID")).get();
                if (Files.exists(EffectsData.getPath(effectsID))) {
                    if (HonorData.effects(id, effectsID)) {
                        src.sendMessage(of("[NewHonor]set honor effects successful"));
                        return CommandResult.success();
                    }
                } else {
                    src.sendMessage(of("[NewHonor]Effects:" + effectsID + "not found"));
                }
                src.sendMessage(of("[NewHonor]set honor effects failed"));
                return CommandResult.empty();
            })
            .build();

    static CommandSpec reload = CommandSpec.builder()
            .executor((src, args) -> {
                src.sendMessage(of("[NewHonor]start reload"));
                NewHonor.plugin.reload();
                refreshCache(src);
                src.sendMessage(of("[NewHonor]reloaded successful"));
                return CommandResult.success();
            })
            .build();

    /**
     * 删掉已经不再使用的缓存并更新所有缓存的text
     *
     * @param src 谁发起的刷新
     */
    private static void refreshCache(CommandSource src) {
        Task.builder().execute(() -> {
            NewHonor.clearCaches();
            Sponge.getServer().getOnlinePlayers().stream().map(Player::getUniqueId)
                    .map(PlayerData::new)
                    .forEach(NewHonor::doSomething);
            NewHonor.plugin.choosePluginMode();
            ScoreBoardManager.init();
            src.sendMessage(of("[NewHonor]refresh finished"));
        }).async().name("newhonor - refresh").submit(NewHonor.plugin);
    }
}
