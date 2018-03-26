package com.github.euonmyoji.newhonor;

import com.github.euonmyoji.newhonor.commands.HonorCommand;
import com.github.euonmyoji.newhonor.configuration.HonorData;
import com.github.euonmyoji.newhonor.configuration.PlayerData;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

@Plugin(id = "newhonor", name = "New Honor", version = "1.2", authors = "yinyangshi")
public class NewHonor {
    private NewHonorMessageManage mMessage = new NewHonorMessageManage();
    @Inject
    @ConfigDir(sharedRoot = false)
    public Path cfgDir;

    @Inject
    public Logger logger;

    public static NewHonor plugin;
    public static HonorData hd;
    public static HashMap<UUID, Text> usinghonor = new HashMap<>();

    @Listener
    public void onStarting(GameStartingServerEvent event) {
        try {
            if (!Files.exists(cfgDir)) {
                Files.createDirectory(cfgDir);
            }
            if (!Files.exists(cfgDir.resolve("PlayerData"))) {
                Files.createDirectories(cfgDir.resolve("PlayerData"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Listener
    public void onStarted(GameStartedServerEvent event) {
        plugin = this;
        hd = new HonorData();
        Sponge.getCommandManager().register(this, HonorCommand.honor, "honor");
        logger.info("NewHonor插件作者邮箱:1418780411@qq.com");
    }

    @Listener
    public void onClientConnectionJoin(ClientConnectionEvent.Join event) {
        Player p = event.getTargetEntity();
        PlayerData pd = new PlayerData(p);
        Task.builder().execute(() -> {
            if (!pd.init()) {
                logger.error("初始化玩家" + p.getName() + "," + p.getUniqueId() + ",头衔数据失败！");
            }
            if (pd.isShowHonor()) {
                pd.getHonor().ifPresent(text -> usinghonor.put(p.getUniqueId(), text));
            }
        }).async().delayTicks(20).name("newhonor - init PlayerData").submit(this);
        MessageChannel originalChannel = event.getOriginalChannel();
        MessageChannel newChannel = MessageChannel.combined(p.getMessageChannel(), originalChannel,
                mMessage);
        p.setMessageChannel(newChannel);
    }

    @Listener
    public void onPlayerDie(RespawnPlayerEvent event) {
        Player p = event.getTargetEntity();
        PlayerData pd = new PlayerData(p);
        Task.builder().execute(() -> {
            if (pd.isShowHonor()) {
                pd.getHonor().ifPresent(text -> usinghonor.put(p.getUniqueId(), text));
            }
        }).async().name("newhonor - init PlayerData").submit(this);
        MessageChannel newChannel = MessageChannel.combined(p.getMessageChannel(),
                mMessage);
        p.setMessageChannel(newChannel);
    }
}
