package com.github.euonmyoji.newhonor;

import com.github.euonmyoji.newhonor.commands.HonorCommand;
import com.github.euonmyoji.newhonor.configuration.EffectsData;
import com.github.euonmyoji.newhonor.configuration.HonorData;
import com.github.euonmyoji.newhonor.configuration.NewHonorConfig;
import com.github.euonmyoji.newhonor.configuration.PlayerData;
import com.github.euonmyoji.newhonor.listeners.NewHonorMessageListener;
import com.github.euonmyoji.newhonor.listeners.UChatEventListener;
import com.google.common.base.Charsets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import org.spongepowered.plugin.meta.version.ComparableVersion;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Plugin(id = "newhonor", name = "New Honor", version = NewHonor.VERSION, authors = "yinyangshi", description = "NewHonor plugin")
public class NewHonor {
    static final String VERSION = "1.4.2";
    public static final NewHonorMessageChannel mMessage = new NewHonorMessageChannel();
    @Inject
    @ConfigDir(sharedRoot = false)
    private Path cfgDir;

    @Inject
    public Logger logger;

    public static NewHonor plugin;
    public static final HashMap<UUID, Text> honorTextCache = new HashMap<>();
    private static final HashMap<UUID, String> playerUsingEffectCache = new HashMap<>();
    public static final HashMap<String, List<PotionEffect>> effectsCache = new HashMap<>();

    @Listener
    public void onStarting(GameStartingServerEvent event) {
        plugin = this;
        try {
            NewHonorConfig.defaultCfgDir = cfgDir;
            if (!Files.exists(cfgDir)) {
                Files.createDirectory(cfgDir);
            }
            cfgDir = null;
            NewHonorConfig.init();
            if (!Files.exists(NewHonorConfig.cfgDir)) {
                Files.createDirectory(NewHonorConfig.cfgDir);
            }
            if (!Files.exists(NewHonorConfig.cfgDir.resolve("PlayerData"))) {
                Files.createDirectory(NewHonorConfig.cfgDir.resolve("PlayerData"));
            }
            checkUpdate();
            NewHonorConfig.getCfg().getNode("compatibleUChat").setValue(NewHonorConfig.getCfg().getNode("compatibleUChat").getBoolean(false));
            NewHonorConfig.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkUpdate() {
        Task.builder().async().name("NewHonor - check for update").execute(() -> {
            try {
                URL url = new URL("https://api.github.com/repos/euOnmyoji/NewHonor-SpongePlugin/releases");
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.getResponseCode();
                try (InputStreamReader reader = new InputStreamReader(connection.getInputStream(), Charsets.UTF_8)) {
                    JsonObject jsonObject = new JsonParser().parse(reader).getAsJsonArray().get(0).getAsJsonObject();
                    String version = jsonObject.get("tag_name").getAsString();
                    int c = new ComparableVersion(version).compareTo(new ComparableVersion(VERSION));
                    if (c > 0) {
                        logger.info("found a latest version:" + version + ".Your version now:" + VERSION);
                    } else if (c < 0) {
                        logger.info("the latest version in github:" + version + "[Your:" + VERSION + "]");
                    }
                }
            } catch (Exception e) {
                logger.info("check for updating failed");
            }
        }).submit(this);
    }

    @Listener
    public void onStarted(GameStartedServerEvent event) {
        Sponge.getCommandManager().register(this, HonorCommand.honor, "honor");
        Task.builder().execute(() -> playerUsingEffectCache.forEach((uuid, s) -> Sponge.getServer().getPlayer(uuid)
                .ifPresent(player -> {
                    if (effectsCache.containsKey(s)) {
                        PotionEffectData effects = player.getOrCreate(PotionEffectData.class).orElseThrow(UNKNOWN::new);
                        List<PotionEffect> list = effectsCache.get(s);
                        list.forEach(effects::addElement);
                        player.offer(effects);
                    }
                }))).name("newhonor - givePlayerEffects").intervalTicks(20).submit(this);
        logger.info("NewHonor插件作者邮箱:1418780411@qq.com");
        if (NewHonorConfig.getCfg().getNode("compatibleUChat").getBoolean(false)) {
            Sponge.getEventManager().registerListeners(this, new UChatEventListener());
        } else {
            Sponge.getEventManager().registerListeners(this, new NewHonorMessageListener());
        }
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
    }

    @Listener
    public void onPlayerDie(RespawnPlayerEvent event) {
        Player p = event.getTargetEntity();
        PlayerData pd = new PlayerData(p);
        Task.builder().execute(() -> doSomething(pd)).async().name("newhonor - (die) init Player" + p.getName()).submit(this);
    }


    public static void clearCaches() {
        honorTextCache.clear();
        effectsCache.clear();
        playerUsingEffectCache.clear();
    }

    public static void doSomething(PlayerData pd) {
        pd.checkUsing();
        pd.ifShowHonor(text -> {
            text.ifPresent(t -> honorTextCache.put(pd.getUUID(), t));
            if (pd.isEnableEffects()) {
                HonorData.getEffectsID(pd.getUse()).ifPresent(s -> {
                    try {
                        effectsCache.put(s, new EffectsData(s).getEffects());
                        playerUsingEffectCache.put(pd.getUUID(), s);
                    } catch (ObjectMappingException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                playerUsingEffectCache.remove(pd.getUUID());
            }
        }).orElse(() -> {
            honorTextCache.remove(pd.getUUID());
            playerUsingEffectCache.remove(pd.getUUID());
        });
    }
}
