package com.github.euonmyoji.newhonor.command;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.configuration.PlayerConfig;
import com.github.euonmyoji.newhonor.configuration.PluginConfig;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.scheduler.Task;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.spongepowered.api.text.Text.of;

/**
 * @author yinyangshi
 */
final class SqlCommand {

    static CommandSpec updateToSql = CommandSpec.builder()
            .executor((src, args) -> {
                src.sendMessage(of("[NewHonor]start updating"));
                Task.builder().async().execute(() -> {
                    try {
                        List<String> list = Files.list(PluginConfig.cfgDir.resolve("PlayerData"))
                                .map(Path::getFileName).map(Path::toString)
                                .map(s -> s.replace(".conf", ""))
                                .collect(Collectors.toList());
                        for (String s : list) {
                            UUID uuid = UUID.fromString(s);
                            PlayerConfig sqlCfg = PlayerConfig.getOf("mysql", uuid);
                            PlayerConfig localCfg = PlayerConfig.getOf("local", uuid);
                            localCfg.checkUsingHonor();
                            sqlCfg.setUseHonor(localCfg.getUsingHonorID());
                            Optional<List<String>> optHonors = localCfg.getOwnHonors();
                            if (optHonors.isPresent()) {
                                sqlCfg.giveHonor(optHonors.get().stream().reduce((s1, s2) -> s1 + "," + s2)
                                        .orElseThrow(NoSuchFieldError::new));
                            }
                            sqlCfg.setWhetherEnableEffects(localCfg.isEnabledEffects());
                            sqlCfg.setWhetherUseHonor(localCfg.isUseHonor());
                            sqlCfg.setListHonorStyle(localCfg.getListHonorStyle());
                        }
                        src.sendMessage(of("[NewHonor] update finished successful"));
                    } catch (Exception e) {
                        src.sendMessage(of("[NewHonor] error!"));
                        NewHonor.logger.warn("update to sql error", e);
                    }
                }).name("NewHonor - update player cfg to sql").submit(NewHonor.plugin);
                return CommandResult.success();
            })
            .build();

    static CommandSpec downloadFromSql = CommandSpec.builder()
            .executor((src, args) -> {
                src.sendMessage(of("[NewHonor]start downloading"));
                Task.builder().async().execute(() -> {
                    try {
                        List<String> list = Files.list(PluginConfig.cfgDir.resolve("PlayerData"))
                                .map(Path::getFileName).map(Path::toString)
                                .map(s -> s.replace(".conf", ""))
                                .collect(Collectors.toList());
                        for (String s : list) {
                            UUID uuid = UUID.fromString(s);
                            PlayerConfig sqlCfg = PlayerConfig.getOf("mysql", uuid);
                            PlayerConfig localCfg = PlayerConfig.getOf("local", uuid);
                            sqlCfg.checkUsingHonor();
                            localCfg.setUseHonor(sqlCfg.getUsingHonorID());
                            sqlCfg.getOwnHonors().ifPresent(strings -> strings.forEach(s1 -> {
                                try {
                                    localCfg.giveHonor(s1);
                                } catch (SQLException e) {
                                    NewHonor.logger.warn("Why sql with local cfg?", e);
                                }
                            }));
                            localCfg.setWhetherEnableEffects(sqlCfg.isEnabledEffects());
                            localCfg.setWhetherUseHonor(sqlCfg.isUseHonor());
                            localCfg.setListHonorStyle(sqlCfg.getListHonorStyle());
                        }
                        src.sendMessage(of("[NewHonor] download finished successful"));
                    } catch (Exception e) {
                        src.sendMessage(of("[NewHonor] error!"));
                        NewHonor.logger.warn("update to sql error", e);
                    }
                }).name("NewHonor - update player cfg to sql").submit(NewHonor.plugin);
                return CommandResult.success();
            })
            .build();

    private SqlCommand() {
        throw new UnsupportedOperationException();
    }
}
