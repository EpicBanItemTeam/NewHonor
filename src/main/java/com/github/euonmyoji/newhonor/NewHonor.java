package com.github.euonmyoji.newhonor;

import com.github.euonmyoji.newhonor.api.event.NewHonorReloadEvent;
import com.github.euonmyoji.newhonor.command.HonorCommand;
import com.github.euonmyoji.newhonor.configuration.*;
import com.github.euonmyoji.newhonor.listener.NewHonorMessageListener;
import com.github.euonmyoji.newhonor.listener.UltimateChatEventListener;
import com.github.euonmyoji.newhonor.task.EffectsOffer;
import com.github.euonmyoji.newhonor.task.HaloEffectsOffer;
import com.github.euonmyoji.newhonor.task.TaskManager;
import com.google.common.base.Charsets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.plugin.meta.version.ComparableVersion;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static com.github.euonmyoji.newhonor.configuration.NewHonorConfig.*;

/**
 * @author yinyangshi
 */
@Plugin(id = "newhonor", name = "New Honor", version = NewHonor.VERSION, authors = "yinyangshi", description = "NewHonor plugin",
        dependencies = {@Dependency(id = "ultimatechat", optional = true), @Dependency(id = "placeholderapi", optional = true),
                @Dependency(id = "nucleus", optional = true)})
public class NewHonor {
    public static final String VERSION = "2.0.0-pre-b13";
    public static final NewHonorMessageChannel M_MESSAGE = new NewHonorMessageChannel();
    @Inject
    @ConfigDir(sharedRoot = false)
    private Path cfgDir;

    @Inject
    public Logger logger;
    @Inject
    private PluginContainer pluginContainer;

    public static NewHonor plugin;
    public final HashMap<UUID, Text> honorTextCache = new HashMap<>();
    public final HashMap<UUID, String> playerUsingEffectCache = new HashMap<>();
    private static final Object CACHE_LOCK = new Object();

    private final UltimateChatEventListener UChatListener = new UltimateChatEventListener();
    private final NewHonorMessageListener NewHonorListener = new NewHonorMessageListener();

    private boolean enabledPlaceHolderAPI = false;
    private boolean hookedNucleus = false;
    private boolean hookedUChat = false;

    @Listener
    public void onStarting(GameStartingServerEvent event) {
        plugin = this;
        try {
            NewHonorConfig.defaultCfgDir = cfgDir;
            if (Files.notExists(cfgDir)) {
                Files.createDirectory(cfgDir);
            }
            cfgDir = null;
            NewHonorConfig.init();
            if (Files.notExists(NewHonorConfig.cfgDir)) {
                Files.createDirectory(NewHonorConfig.cfgDir);
            }
            final String playerData = "PlayerData";
            if (Files.notExists(NewHonorConfig.cfgDir.resolve(playerData))) {
                Files.createDirectory(NewHonorConfig.cfgDir.resolve(playerData));
            }
            if (NewHonorConfig.isCheckUpdate()) {
                checkUpdate();
            } else {
                logger.info("check update was canceled");
            }

            //已经不在使用的N个配置文件node
            NewHonorConfig.getCfg().removeChild(OLD_USE_PAPI_NODE_PATH);
            NewHonorConfig.getCfg().removeChild(OLD_COMPATIBLE_UCHAT_NODE_PATH);
            NewHonorConfig.getCfg().removeChild("nucleus-placeholder");

            NewHonorConfig.getCfg().getNode(DISPLAY_HONOR_NODE_PATH)
                    .setValue(NewHonorConfig.getCfg().getNode(DISPLAY_HONOR_NODE_PATH).getBoolean(false));
            NewHonorConfig.getCfg().getNode(FORCE_ENABLE_DEFAULT_LISTENER)
                    .setValue(NewHonorConfig.getCfg().getNode(FORCE_ENABLE_DEFAULT_LISTENER).getBoolean(false));
            NewHonorConfig.getCfg().getNode(PERMISSION_MANAGE)
                    .setValue(NewHonorConfig.getCfg().getNode(PERMISSION_MANAGE).getBoolean(false));
            NewHonorConfig.save();
            LanguageManager.reload();
            SqlManager.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 几乎不会被使用的代码 检查更新
     */
    private void checkUpdate() {
        Task.builder().async().name("NewHonor - check for update").execute(() -> {
            try {
                URL url = new URL("https://api.github.com/repos/euOnmyoji/NewHonor-plugin-for-sponge/releases");
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.getResponseCode();
                try (InputStreamReader reader = new InputStreamReader(connection.getInputStream(), Charsets.UTF_8)) {
                    JsonObject jsonObject = new JsonParser().parse(reader).getAsJsonArray().get(0).getAsJsonObject();
                    String version = jsonObject.get("tag_name").getAsString().replace("v", "")
                            .replace("version", "");
                    int c = new ComparableVersion(version).compareTo(new ComparableVersion(VERSION));
                    if (c > 0) {
                        logger.info("found a latest version:" + version + ".Your version now:" + VERSION);
                    } else if (c < 0) {
                        logger.info("the latest version in com.github:" + version + "[Your version:" + VERSION + "]");
                    }
                }
            } catch (Exception e) {
                logger.info("check for updating failed");
            }
        }).submit(this);
    }

    @Inject
    private Metrics metrics;

    @Listener
    public void onStarted(GameStartedServerEvent event) {
        Sponge.getCommandManager().register(this, HonorCommand.honor, "honor", "honour");
        logger.info("NewHonor author email:1418780411@qq.com");
        hook();
        try {
            TaskManager.update();
        } catch (IOException e) {
            logger.warn("Task init error", e);
        }
        metrics.addCustomChart(new Metrics.SimplePie("useeffects", () -> EffectsOffer.TASK_DATA.size() > 0 ? "true" : "false"));
        metrics.addCustomChart(new Metrics.SimplePie("displayhonor", () -> ScoreBoardManager.enable ? "true" : "false"));
        metrics.addCustomChart(new Metrics.SimplePie("usepapi",
                () -> enabledPlaceHolderAPI ? "true" : "false"));
        metrics.addCustomChart(new Metrics.SimplePie("usehaloeffects", () -> HaloEffectsOffer.TASK_DATA.size() > 0 ?
                "true" : "false"));
        metrics.addCustomChart(new Metrics.SimplePie("usenucleus", () -> hookedNucleus ? "true" : "false"));
    }

    @Listener
    public void onClientConnectionJoin(ClientConnectionEvent.Join event) {
        Player p = event.getTargetEntity();
        Task.builder().execute(() -> {
            try {
                PlayerConfig pd = PlayerConfig.get(p);
                pd.init();
                doSomething(pd);
            } catch (Throwable e) {
                logger.error("error while init player", e);
            }
        }).async().name("newhonor - init Player" + p.getName()).submit(this);
    }

    /**
     * 清掉插件缓存 任务缓存
     */
    public static void clearCaches() {
        synchronized (CACHE_LOCK) {
            plugin.honorTextCache.clear();
            plugin.playerUsingEffectCache.clear();
        }
        synchronized (EffectsOffer.TASK_DATA) {
            EffectsOffer.TASK_DATA.clear();
        }
        synchronized (HaloEffectsOffer.TASK_DATA) {
            HaloEffectsOffer.TASK_DATA.clear();
        }
    }

    /**
     * 探测插件 添加变量
     */
    private void hook() {
        EventManager eventManager = Sponge.getEventManager();
        eventManager.unregisterListeners(UChatListener);
        eventManager.unregisterListeners(NewHonorListener);
        ScoreBoardManager.enable = false;
        ScoreBoardManager.clear();

        boolean enableDefault = true;
        //hook nucleus
        try {
            Class.forName("io.github.nucleuspowered.nucleus.api.NucleusAPI");
            NucleusManager.doIt();
            enableDefault = false;
            if (!hookedNucleus) {
                logger.info("hooked nucleus");
                logger.info("default listener is disabling, please use {{pl:newhonor:newhonor}} to show honor in chat");
                logger.info("发现nucleus插件，请在nucleus配置里面的chat里面使用变量{{pl:newhonor:newhonor}}来在聊天栏显示头衔");
            }
            hookedNucleus = true;
        } catch (ClassNotFoundException ignore) {
        }

        //hook PAPI
        try {
            PlaceHolderManager.create();
            if (!enableDefault) {
                logger.info("hooked PAPI, you can use '%newhonor%' now.");
            }
            enabledPlaceHolderAPI = true;
        } catch (RuntimeException ignore) {
        }

        //hook UChat
        try {
            Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.API.SendChannelMessageEvent");
            Sponge.getEventManager().registerListeners(this, UChatListener);
            if (!hookedUChat) {
                logger.info("hooked UChat");
                logger.info("please use {newhonor} in UChat config to show honor in the chat");
            }
            hookedUChat = true;
            enableDefault = false;
        } catch (ClassNotFoundException ignore) {
        }

        //display
        if (NewHonorConfig.getCfg().getNode(DISPLAY_HONOR_NODE_PATH).getBoolean(false)) {
            final String esbID = "de_yottaflops_easyscoreboard";
            if (Sponge.getPluginManager().getPlugin(esbID).isPresent()) {
                plugin.logger.warn("The plugin easyscoreboard is updating scoreboard, please uninstall it to use 'displayHonor' (trying cancel it), or Do not use 'displayHonor'");
                plugin.logger.warn("请卸载EasyScoreBoard来保证displayHonor功能正常, 或者不使用displayHonor功能");
                Sponge.getServer().getBroadcastChannel().send(Text.of(TextColors.RED, "'displayHonor' may work not correctly, because plugin[EasyScoreboard]?"));
            }
            ScoreBoardManager.enable = true;
            ScoreBoardManager.init();
            logger.info("displayHonor mode enabled");
            logger.info("if you find honor shows twice, try to change config in nucleus(overwrite-early-prefix = true)");
            logger.info("如果发现头衔重复显示，请尝试在nucleus配置文件里面将overwrite-early-prefix设置为true");
            if (NewHonorConfig.getCfg().getNode(FORCE_ENABLE_DEFAULT_LISTENER).getBoolean() && enableDefault) {
                Sponge.getEventManager().registerListeners(this, NewHonorListener);
            }
        } else if (enableDefault) {
            Sponge.getEventManager().registerListeners(this, NewHonorListener);
        }
    }

    public void reload() {
        Sponge.getEventManager().post(new NewHonorReloadEvent());
        NewHonorConfig.reload();
        LanguageManager.reload();
        HonorConfig.reload();
        try {
            TaskManager.update();
        } catch (IOException e) {
            logger.warn("reload error!", e);
        }
        NewHonor.plugin.hook();
    }

    /**
     * 对玩家配置进行检查 然后更新缓存
     *
     * @param pd 玩家配置
     */
    public static void doSomething(PlayerConfig pd) {
        Runnable r = () -> {
            try {
                synchronized (CACHE_LOCK) {
                    pd.checkUsingHonor();
                    plugin.playerUsingEffectCache.remove(pd.getUUID());
                    plugin.honorTextCache.remove(pd.getUUID());
                    if (pd.isUseHonor()) {
                        pd.getUsingHonorText().ifPresent(text -> plugin.honorTextCache.put(pd.getUUID(), text));
                        if (pd.isEnableEffects()) {
                            HonorConfig.getEffectsID(pd.getUsingHonorID()).ifPresent(s -> plugin.playerUsingEffectCache.put(pd.getUUID(), s));
                        }
                    }
                }
            } catch (Exception e) {
                plugin.logger.error("error about data!", e);
            }

        };
        Optional<Runnable> r2 = Sponge.getServer().getPlayer(pd.getUUID()).map(player -> () -> ScoreBoardManager.initPlayer(player));

        //r为插件数据修改 异步(有mysql) r2为玩家自身数据修改 可能不存在需要运行的 需要同步
        if (Sponge.getServer().isMainThread()) {
            Task.builder().execute(r).async().name("NewHonor - do something with playerdata " + pd.hashCode()).submit(plugin);
            r2.ifPresent(Runnable::run);
        } else {
            r.run();
            r2.ifPresent(runnable -> Task.builder().execute(runnable).submit(plugin));
        }
    }

    static PluginContainer getContainer() {
        return plugin.pluginContainer;
    }
}
