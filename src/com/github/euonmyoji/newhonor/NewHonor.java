package com.github.euonmyoji.newhonor;

import com.github.euonmyoji.newhonor.commands.HonorCommand;
import com.github.euonmyoji.newhonor.configuration.EffectsData;
import com.github.euonmyoji.newhonor.configuration.HonorData;
import com.github.euonmyoji.newhonor.configuration.PlayerData;
import com.google.inject.Inject;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.omg.CORBA.UNKNOWN;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
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
import java.util.List;
import java.util.UUID;

@Plugin(id = "newhonor", name = "New Honor", version = "1.3-beta", authors = "yinyangshi")
public class NewHonor {
    private NewHonorMessageManage mMessage = new NewHonorMessageManage();
    @Inject
    @ConfigDir(sharedRoot = false)
    public Path cfgDir;

    @Inject
    public Logger logger;

    public static NewHonor plugin;
    public static HashMap<UUID, Text> honorTextCache = new HashMap<>();
    private static HashMap<UUID, String> playerUsingEffectCache = new HashMap<>();
    public static HashMap<String, List<PotionEffect>> effectsCache = new HashMap<>();

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
        Sponge.getCommandManager().register(this, HonorCommand.honor, "honor");
        Task.builder().execute(() -> playerUsingEffectCache.forEach((uuid, s) -> Sponge.getServer().getPlayer(uuid).ifPresent(player -> {
            if (effectsCache.containsKey(s)) {
                PotionEffectData effects = player.getOrCreate(PotionEffectData.class).orElseThrow(UNKNOWN::new);
                List<PotionEffect> list = effectsCache.get(s);
                list.forEach(effects::addElement);
                player.offer(effects);
            }
        }))).name("newhonor - givePlayerEffects").delayTicks(20).submit(this);
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
            doSomething(pd);
        }).async().delayTicks(20).name("newhonor - init Player" + p.getName()).submit(this);
        MessageChannel originalChannel = event.getOriginalChannel();
        MessageChannel newChannel = MessageChannel.combined(p.getMessageChannel(), originalChannel,
                mMessage);
        p.setMessageChannel(newChannel);
    }

    @Listener
    public void onPlayerDie(RespawnPlayerEvent event) {
        Player p = event.getTargetEntity();
        PlayerData pd = new PlayerData(p);
        Task.builder().execute(() -> doSomething(pd)).async().name("newhonor - (die) init Player" + p.getName()).submit(this);
        MessageChannel newChannel = MessageChannel.combined(p.getMessageChannel(),
                mMessage);
        p.setMessageChannel(newChannel);
    }

    public static void clearCaches() {
        honorTextCache.clear();
        effectsCache.clear();
        playerUsingEffectCache.clear();
    }

    public static void doSomething(PlayerData pd) {
        if (pd.isShowHonor()) {
            pd.getHonor().ifPresent(text -> honorTextCache.put(pd.getUUID(), text));
            if (pd.isEnableEffects()) {
                HonorData.getEffectsID(pd.getUse()).ifPresent(s -> {
                    try {
                        effectsCache.put(s, new EffectsData(s).getEffects());
                        playerUsingEffectCache.put(pd.getUUID(), s);
                    } catch (ObjectMappingException e) {
                        e.printStackTrace();
                    }
                });
            }
        } else NewHonor.honorTextCache.remove(pd.getUUID());
    }
}
