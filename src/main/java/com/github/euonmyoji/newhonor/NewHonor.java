package com.github.euonmyoji.newhonor;

import com.github.euonmyoji.newhonor.api.event.NewHonorReloadEvent;
import com.github.euonmyoji.newhonor.command.HonorCommand;
import com.github.euonmyoji.newhonor.configuration.*;
import com.github.euonmyoji.newhonor.data.HonorValueData;
import com.github.euonmyoji.newhonor.listener.NewHonorMessageListener;
import com.github.euonmyoji.newhonor.listener.UltimateChatEventListener;
import com.github.euonmyoji.newhonor.task.DisplayHonorTaskManager;
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
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
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
@Plugin(id = NewHonor.NEWHONOR_ID, name = "New Honor", version = NewHonor.VERSION, authors = "yinyangshi", description = "NewHonor plugin",
        dependencies = {@Dependency(id = NewHonor.UCHAT_ID, optional = true), @Dependency(id = NewHonor.PAPI_ID, optional = true),
                @Dependency(id = NewHonor.NUCLEUS_ID, optional = true)})
public class NewHonor {
    static final String NEWHONOR_ID = "newhonor";
    static final String NUCLEUS_ID = "nucleus";
    static final String PAPI_ID = "placeholderapi";
    static final String UCHAT_ID = "ultimatechat";

    public static final String VERSION = "2.1.1";
    public static final NewHonorMessageChannel M_MESSAGE = new NewHonorMessageChannel();
    @Inject
    @ConfigDir(sharedRoot = false)
    private Path defaultCfgDir;

    public static Logger logger;

    @Inject
    public void setLogger(Logger l) {
        logger = l;
    }

    public static NewHonor plugin;
    public final HashMap<UUID, HonorValueData> honorTextCache = new HashMap<>();
    public final HashMap<UUID, String> playerUsingEffectCache = new HashMap<>();
    private static final Object CACHE_LOCK = new Object();

    private final UltimateChatEventListener UChatListener = new UltimateChatEventListener();
    private final NewHonorMessageListener NewHonorListener = new NewHonorMessageListener();

    private boolean enabledPlaceHolderAPI = false;
    private boolean hookedNucleus = false;
    private boolean hookedUChat = false;

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        plugin = this;
        try {
            NewHonorConfig.defaultCfgDir = defaultCfgDir;
            if (Files.notExists(defaultCfgDir)) {
                Files.createDirectory(defaultCfgDir);
            }
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
                logger.info("§2check update was canceled");
            }

            HonorConfig.init();
            EffectsConfig.init();

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
            logger.warn("init plugin IOE!", e);
        }
    }

    private void checkUpdate() {
        Task.builder().async().name("NewHonor - check for update").execute(() -> {
            try {
                final String u = "https://api.github.com/repos/euOnmyoji/NewHonor-plugin-for-sponge/releases";
                HttpsURLConnection con = (HttpsURLConnection) new URL(u)
                        .openConnection();
                con.setRequestMethod("GET");
                con.getResponseCode();
                try (InputStreamReader reader = new InputStreamReader(con.getInputStream(), Charsets.UTF_8)) {
                    JsonObject json = new JsonParser().parse(reader).getAsJsonArray().get(0).getAsJsonObject();
                    String v = json.get("tag_name").getAsString()
                            .replaceFirst("v", "")
                            .replaceAll("version", "");
                    String preKey = "pre";
                    if (!v.contains(preKey)) {
                        int c = new ComparableVersion(v).compareTo(new ComparableVersion(VERSION));
                        if (c > 0) {
                            logger.info("found a latest version:" + v + ".Your version now:" + VERSION);
                        } else if (c < 0) {
                            logger.info("the latest version in github.com:" + v + "[Your version:" + VERSION + "]");
                        }
                    }
                }
            } catch (Throwable e) {
                logger.info("check for updating failed");
                logger.debug("check update error", e);
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
        if (honorTextCache.containsKey(p.getUniqueId())) {
            ScoreBoardManager.initPlayer(p);
        } else {
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
        DisplayHonorTaskManager.clear();
    }

    /**
     * 探测插件 添加变量
     */
    private void hook() {
        EventManager eventManager = Sponge.getEventManager();
        eventManager.unregisterListeners(NewHonorListener);
        ScoreBoardManager.enable = false;
        ScoreBoardManager.clear();

        //hook nucleus
        if (Sponge.getPluginManager().getPlugin(NUCLEUS_ID).isPresent()) {
            NucleusManager.doIt();
            if (!hookedNucleus) {
                logger.info("hooked nucleus");
            }
            hookedNucleus = true;
        }

        //hook PAPI
        if (Sponge.getPluginManager().getPlugin(PAPI_ID).isPresent()) {
            PlaceHolderManager.create();
            if (!enabledPlaceHolderAPI) {
                logger.info("hooked PAPI, you can use '%newhonor%' now.");
            }
            enabledPlaceHolderAPI = true;
        }
        //hook UChat
        if (Sponge.getPluginManager().getPlugin(UCHAT_ID).isPresent()) {
            Sponge.getEventManager().registerListeners(this, UChatListener);
            if (!hookedUChat) {
                logger.info("hooked UChat");
            }
            hookedUChat = true;
        }
        //没用uchat  开了nucleus就必须force 或者不开直接用
        //displayHonor
        boolean displayHonor = NewHonorConfig.getCfg().getNode(DISPLAY_HONOR_NODE_PATH).getBoolean(false);
        if (displayHonor) {
            ScoreBoardManager.enable = true;
            ScoreBoardManager.init();
            logger.info("displayHonor mode enabled");
            if (hookedNucleus) {
                logger.info("DisplayHonor enabled");
            }
        }

        //default listener
        boolean forcePass = !(hookedNucleus || displayHonor) || NewHonorConfig.getCfg().getNode(FORCE_ENABLE_DEFAULT_LISTENER).getBoolean();
        if (forcePass && !hookedUChat) {
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
                        pd.getUsingHonorValue().ifPresent(data -> plugin.honorTextCache.put(pd.getUUID(), data));
                        if (pd.isEnabledEffects()) {
                            HonorConfig.getEffectsID(pd.getUsingHonorID()).ifPresent(s -> plugin.playerUsingEffectCache.put(pd.getUUID(), s));
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("error about data!", e);
            }

        };
        Optional<Runnable> r2 = Sponge.getServer().getPlayer(pd.getUUID()).map(player -> () -> ScoreBoardManager.initPlayer(player));

        //r为插件数据修改 异步(有mysql) r2为玩家自身数据修改 可能不存在需要运行的 需要同步
        //为了保证更新时缓存为最新 r需要先运行
        if (Sponge.getServer().isMainThread()) {
            Task.builder().execute(() -> {
                r.run();
                r2.ifPresent(runnable -> Task.builder().execute(runnable).submit(plugin));
            }).async().name("NewHonor - do something with playerdata " + pd.hashCode()).submit(plugin);
        } else {
            r.run();
            r2.ifPresent(runnable -> Task.builder().execute(runnable).submit(plugin));
        }
    }
}
