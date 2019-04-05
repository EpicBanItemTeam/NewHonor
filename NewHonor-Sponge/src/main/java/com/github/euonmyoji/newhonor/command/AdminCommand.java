package com.github.euonmyoji.newhonor.command;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.ArgLevel;
import com.github.euonmyoji.newhonor.api.configuration.PlayerConfig;
import com.github.euonmyoji.newhonor.command.args.HonorIDArg;
import com.github.euonmyoji.newhonor.configuration.EffectsConfig;
import com.github.euonmyoji.newhonor.configuration.HonorConfig;
import com.github.euonmyoji.newhonor.manager.TaskManager;
import com.github.euonmyoji.newhonor.util.Log;
import com.github.euonmyoji.newhonor.util.Util;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;

import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.github.euonmyoji.newhonor.manager.LanguageManager.langBuilder;
import static com.github.euonmyoji.newhonor.manager.SpongeLanguageManager.getText;
import static org.spongepowered.api.text.Text.of;

final class AdminCommand {
    static CommandSpec refresh = CommandSpec.builder()
            .executor((src, args) -> {
                refreshCache(src);
                return CommandResult.success();
            })
            .build();
    static CommandSpec give = CommandSpec.builder()
            .arguments(GenericArguments.user(of("user")),
                    new HonorIDArg(of("id")))
            .executor((src, args) -> {
                Collection<User> users = args.getAll(of("user"));
                Collection<String> ids = args.getAll(of("id"));
                if (users.isEmpty() || ids.isEmpty()) {
                    src.sendMessage(of("[NewHonor]users is empty:" + users.isEmpty() + ",honorID is empty:" + ids.isEmpty()));
                    src.sendMessage(of("[NewHonor]give honor failed"));
                    return CommandResult.empty();
                }
                Task.builder().execute(() -> users.forEach(user -> {
                    try {
                        PlayerConfig pd = PlayerConfig.get(user);
                        ids.forEach(id -> {
                            try {
                                if (pd.giveHonor(id)) {
                                    src.sendMessage(of("[NewHonor]gave user " + user.getName() + " honor " + id + " successful"));
                                    Log.info(src.getName() + " gave " + user.getName() + " honor " + id + " successful");
                                } else {
                                    src.sendMessage(of("[NewHonor]gave user " + user.getName() + " honor " + id + " failed"));
                                    Log.info(src.getName() + " gave " + user.getName() + " honor " + id + " failed");
                                }
                            } catch (SQLException e) {
                                src.sendMessage(of("[NewHonor]gave user " + user.getName() + " honor " + id + " failed(error!)"));
                                NewHonor.logger.warn("error while giving honor", e);
                            }
                        });
                    } catch (Throwable e) {
                        src.sendMessage(of("[NewHonor]gave user " + user.getName() + " honor " + " failed(error!)"));
                        NewHonor.logger.warn("error while giving honor", e);
                    }
                })).async().name("newhonor - Give Users Honors").submit(NewHonor.plugin);
                return CommandResult.success();
            })
            .build();
    static CommandSpec take = CommandSpec.builder()
            .arguments(GenericArguments.user(of("user")),
                    new HonorIDArg(of("id")))
            .executor((src, args) -> {
                Collection<User> users = args.getAll(of("user"));
                Collection<String> ids = args.getAll(of("id"));
                Task.builder().execute(() -> {
                    users.forEach(user -> {
                        try {
                            PlayerConfig pd = PlayerConfig.get(user);
                            ids.forEach(id -> {
                                try {
                                    if (pd.takeHonor(id)) {
                                        src.sendMessage(of("[NewHonor]took user " + user.getName() + " honor " + id + " successful"));
                                        Log.info(src.getName() + " took " + user.getName() + " honor " + id + " successful");
                                    } else {
                                        src.sendMessage(of("[NewHonor]took user " + user.getName() + " honor " + id + " failed"));
                                        Log.info(src.getName() + " took " + user.getName() + " honor " + id + " failed");
                                    }
                                } catch (Exception e) {
                                    src.sendMessage(of("[NewHonor]took user " + user.getName() + " honor " + id + " failed(error!)"));
                                    NewHonor.logger.warn("error while taking honor", e);
                                }
                            });
                        } catch (Throwable e) {
                            src.sendMessage(of("[NewHonor]took user " + user.getName() + " honor " + " failed(error!)"));
                            NewHonor.logger.warn("error while taking honor", e);
                        }
                    });
                    refreshCache(src);
                }).async().name("newhonor - take users honors").submit(NewHonor.plugin);
                return CommandResult.success();
            })
            .build();
    static CommandSpec set = CommandSpec.builder()
            .arguments(new HonorIDArg(of("honorID"), true, ArgLevel.WARNING),
                    GenericArguments.string(of("honor")))
            .executor((src, args) -> {
                String id = args.<String>getOne(of("honorID")).orElseThrow(NoSuchFieldError::new);
                String honor = args.<String>getOne(of("honor")).orElseThrow(NoSuchFieldError::new);
                if (HonorConfig.setHonor(id, honor)) {
                    Log.info(src.getName() + " set a honor:" + id);
                    src.sendMessage(of("[NewHonor]set a honor successful(start refresh)"));
                    refreshCache(src);
                    return CommandResult.success();
                }
                src.sendMessage(of("[NewHonor]set a honor failed"));
                return CommandResult.empty();
            })
            .build();
    static CommandSpec delete = CommandSpec.builder()
            .arguments(new HonorIDArg(of("honorID")))
            .executor((src, args) -> {
                String id = args.<String>getOne(of("honorID")).orElseThrow(NoSuchFieldError::new);
                if (HonorConfig.deleteHonor(id)) {
                    Log.info(src.getName() + " deleted a honor:" + id);
                    src.sendMessage(of("[NewHonor]deleted a honor successful(start refresh)"));
                    refreshCache(src);
                    return CommandResult.success();
                }
                src.sendMessage(of("[NewHonor]delete a honor failed"));
                return CommandResult.empty();
            })
            .build();
    static CommandSpec add = CommandSpec.builder()
            .arguments(new HonorIDArg(of("honorID"), false, ArgLevel.ERROR),
                    GenericArguments.string(of("honor")))
            .executor((src, args) -> {
                String id = args.<String>getOne(of("honorID")).orElseThrow(NoSuchFieldError::new);
                String honor = args.<String>getOne(of("honor")).orElseThrow(NoSuchFieldError::new);
                if (HonorConfig.addHonor(id, honor)) {
                    Log.info(src.getName() + " added an honor:" + id);
                    src.sendMessage(of("[NewHonor]add an honor successful"));
                    return CommandResult.success();
                } else {
                    src.sendMessage(Text.of("[NewHonor]add an honor failed!"));
                }
                return CommandResult.empty();
            })
            .build();
    static CommandSpec effects = CommandSpec.builder()
            .arguments(new HonorIDArg(of("honorID")),
                    GenericArguments.string(of("effectsID")))
            .executor((src, args) -> {
                String id = args.<String>getOne(of("honorID")).orElseThrow(NoSuchFieldError::new);
                String effectsID = args.<String>getOne(of("effectsID")).orElseThrow(NoSuchFieldError::new);
                if (Files.exists(EffectsConfig.getPath(effectsID))) {
                    if (HonorConfig.setHonorEffects(id, effectsID)) {
                        src.sendMessage(of("[NewHonor]set honor effects successful"));
                        return CommandResult.success();
                    }
                } else {
                    src.sendMessage(of("[NewHonor]Effects:" + effectsID + " is not found"));
                }
                src.sendMessage(of("[NewHonor]set honor effects failed"));
                return CommandResult.empty();
            })
            .build();
    static CommandSpec reload = CommandSpec.builder()
            .executor((src, args) -> {
                src.sendMessage(of("[NewHonor]start reload"));
                long startTime = System.currentTimeMillis();
                NewHonor.plugin.reload();
                long endTime = System.currentTimeMillis();
                src.sendMessage(of("[NewHonor]reloaded successfully in " + (endTime - startTime) + " ms"));
                refreshCache(src);
                return CommandResult.success();
            })
            .build();
    private static NewHonor plugin = NewHonor.plugin;
    static CommandSpec list = CommandSpec.builder()
            .executor((src, args) -> {
                Task.builder().async().execute(() -> {
                    PaginationList.Builder builder = PaginationList.builder().title(getText("newhonor.listcreatedhonors.title")).padding(of("-"));
                    builder.contents(HonorConfig.getAllCreatedHonors().stream().map(id -> Util.toText(langBuilder("newhonor.listcreatedhonors.contexts")
                            .replace("%honorid%", id)
                            .replace("%honor%", HonorConfig.getHonorData(id).getStrValue())
                            .replace("%effectsID%", HonorConfig.getEffectsID(id).orElse("null"))
                            .build()))
                            .filter(text -> !text.toString().contains("there is something wrong"))
                            .collect(Collectors.toList()));
                    builder.build().sendTo(src);
                }).submit(plugin);
                return CommandResult.success();
            })
            .build();

    private AdminCommand() {
        throw new UnsupportedOperationException();
    }

    /**
     * 删掉已经不再使用的缓存并更新所有缓存的text
     *
     * @param src 谁发起的刷新
     */
    private static void refreshCache(CommandSource src) {
        Task.builder().execute(() -> {
            src.sendMessage(of("[NewHonor]start refresh"));
            long startTime = System.currentTimeMillis();
            NewHonor.clearCaches();
            try {
                TaskManager.update();
            } catch (Exception e) {
                NewHonor.logger.warn("Update Task error!", e);
            }
            Sponge.getServer().getOnlinePlayers().stream()
                    .map(player -> {
                        try {
                            return PlayerConfig.get(player.getUniqueId());
                        } catch (Throwable e) {
                            NewHonor.logger.error("error about sql!", e);
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .forEach(NewHonor::updateCache);
            long endTime = System.currentTimeMillis();
            src.sendMessage(of("[NewHonor]refreshed successfully in " + (endTime - startTime) + " ms"));
        }).async().name("newhonor - refresh").submit(NewHonor.plugin);
    }
}
