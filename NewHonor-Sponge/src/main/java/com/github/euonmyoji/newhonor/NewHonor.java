package com.github.euonmyoji.newhonor;

import com.github.euonmyoji.newhonor.api.configuration.PlayerConfig;
import com.github.euonmyoji.newhonor.api.event.NewHonorReloadEvent;
import com.github.euonmyoji.newhonor.command.HonorCommand;
import com.github.euonmyoji.newhonor.configuration.EffectsConfig;
import com.github.euonmyoji.newhonor.configuration.HonorConfig;
import com.github.euonmyoji.newhonor.configuration.PluginConfig;
import com.github.euonmyoji.newhonor.data.HonorData;
import com.github.euonmyoji.newhonor.listener.NewHonorMessageListener;
import com.github.euonmyoji.newhonor.listener.UltimateChatEventListener;
import com.github.euonmyoji.newhonor.manager.*;
import com.github.euonmyoji.newhonor.task.EffectsOfferTask;
import com.github.euonmyoji.newhonor.task.HaloEffectsOfferTask;
import com.google.common.base.Charsets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import javassist.bytecode.annotation.NoSuchClassError;
import org.bstats.sponge.Metrics2;
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
import org.spongepowered.api.text.Text;
import org.spongepowered.plugin.meta.version.ComparableVersion;

import javax.management.RuntimeErrorException;
import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static com.github.euonmyoji.newhonor.configuration.PluginConfig.DISPLAY_HONOR;
import static com.github.euonmyoji.newhonor.configuration.PluginConfig.FORCE_ENABLE_DEFAULT_LISTENER;

/**
 * @author yinyangshi
 */
@Plugin(id = NewHonor.NEWHONOR_ID, name = "New Honor", version = NewHonor.VERSION, authors = "yinyangshi", description = "NewHonor plugin",
        dependencies = {@Dependency(id = NewHonor.UCHAT_ID, optional = true), @Dependency(id = NewHonor.PAPI_ID, optional = true),
                @Dependency(id = NewHonor.NUCLEUS_ID, optional = true)})
public final class NewHonor {
    public static final String NEWHONOR_ID = "newhonor";
    static final String NUCLEUS_ID = "nucleus";
    static final String PAPI_ID = "placeholderapi";
    static final String UCHAT_ID = "ultimatechat";

    public static final String VERSION = "@spongeVersion@";
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
    public final HashMap<UUID, HonorData> honorTextCache = new HashMap<>();
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
            PluginConfig.defaultCfgDir = defaultCfgDir;
            Files.createDirectories(defaultCfgDir);
            PluginConfig.init();
            Files.createDirectories(PluginConfig.cfgDir.resolve("PlayerData"));
            if (PluginConfig.isCheckUpdate()) {
                Task.builder().async().name("NewHonor - check for update").execute(this::checkUpdate).submit(this);
            } else {
                logger.info("§2check update was canceled");
            }

            HonorConfig.init();
            EffectsConfig.init();

            PluginConfig.save();
            MysqlManager.init();
        } catch (IOException e) {
            logger.warn("init plugin IOE!", e);
        }
    }

    @Inject
    private Metrics2 metrics;

    @Listener
    public void onStarted(GameStartedServerEvent event) {
        //纽尊严? 过于真实        idea from sponge 咕咕咕 group
        Sponge.getCommandManager().register(this, HonorCommand.honor, "honor", "honour", "newhonor", "头衔", "称号", "纽尊严");
        logger.info("NewHonor author email:1418780411@qq.com");
        hook();
        try {
            TaskManager.update();
        } catch (IOException e) {
            logger.warn("Task init error", e);
        }
        metrics.addCustomChart(new Metrics2.SimplePie("useeffects", () -> EffectsOfferTask.TASK_DATA.size() > 0 ? "true" : "false"));
        metrics.addCustomChart(new Metrics2.SimplePie("displayhonor", () -> ScoreBoardManager.enable ? "true" : "false"));
        metrics.addCustomChart(new Metrics2.SimplePie("usepapi",
                () -> enabledPlaceHolderAPI ? "true" : "false"));
        metrics.addCustomChart(new Metrics2.SimplePie("usehaloeffects", () -> HaloEffectsOfferTask.TASK_DATA.size() > 0 ?
                "true" : "false"));
        metrics.addCustomChart(new Metrics2.SimplePie("usenucleus", () -> hookedNucleus ? "true" : "false"));

        try {
            if (!Sponge.getMetricsConfigManager().areMetricsEnabled(this)) {
                Sponge.getServer().getConsole()
                        .sendMessage(Text.of("[NewHonor]If you think newhonor is a good plugin and want to support newhonor, please enable metrics, thanks!"));
            }
        } catch (NoClassDefFoundError | NoSuchMethodError ignore) {
            //do not spam the server (ignore)
        }
    }

    @Listener
    public void onClientConnectionJoin(ClientConnectionEvent.Join event) {
        Player p = event.getTargetEntity();
        Task.builder().execute(() -> {
            try {
                PlayerConfig pd = PlayerConfig.get(p);
                pd.init();
                pd.checkPermission();
                pd.checkUsingHonor();
                updateCache(pd);
            } catch (Throwable e) {
                logger.error("error while init player", e);
            }
        }).async().name("newhonor - init Player" + p.getName()).submit(this);

    }

    @Listener
    public void onQuit(ClientConnectionEvent.Disconnect event) {
        Task.builder().async().execute(() -> clearPlayerCache(event.getTargetEntity().getUniqueId())).submit(this);
    }

    /**
     * 清掉插件缓存 任务缓存
     */
    public static void clearCaches() {
        synchronized (CACHE_LOCK) {
            plugin.honorTextCache.clear();
            plugin.playerUsingEffectCache.clear();
        }
        synchronized (EffectsOfferTask.TASK_DATA) {
            EffectsOfferTask.TASK_DATA.clear();
        }
        synchronized (HaloEffectsOfferTask.TASK_DATA) {
            HaloEffectsOfferTask.TASK_DATA.clear();
        }
        DisplayHonorTaskManager.clear();
    }

    public static void clearPlayerCache(UUID uuid) {
        synchronized (CACHE_LOCK) {
            plugin.honorTextCache.remove(uuid);
            plugin.playerUsingEffectCache.remove(uuid);
        }
    }

    public void reload() {
        Sponge.getEventManager().post(new NewHonorReloadEvent());
        PluginConfig.reload();
        SpongeLanguageManager.reload();
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
    public static void updateCache(PlayerConfig pd) {
        Runnable r = () -> {
            try {
                synchronized (CACHE_LOCK) {
                    pd.checkUsingHonor();
                    plugin.playerUsingEffectCache.remove(pd.getUUID());
                    plugin.honorTextCache.remove(pd.getUUID());
                    if (pd.isUseHonor()) {
                        HonorData data = pd.getUsingHonorValue();
                        if (data != null) {
                            plugin.honorTextCache.put(pd.getUUID(), data);
                        }
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
            }).async().name("NewHonor - do something with player data " + pd.hashCode()).submit(plugin);
        } else {
            r.run();
            r2.ifPresent(runnable -> Task.builder().execute(runnable).submit(plugin));
        }
    }

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
            if (!hookedUChat) {
                Sponge.getEventManager().registerListeners(this, UChatListener);
                logger.info("hooked UChat");
            }
            hookedUChat = true;
        }
        //没用uchat  开了nucleus就必须force 或者不开直接用
        //displayHonor
        boolean displayHonor = PluginConfig.generalNode.getNode(DISPLAY_HONOR).getBoolean(false);
        if (displayHonor) {
            ScoreBoardManager.enable = true;
            ScoreBoardManager.init();
            logger.info("displayHonor mode enabled");
        }

        //default listener
        boolean forcePass = !(hookedNucleus || displayHonor) || PluginConfig.generalNode.getNode(FORCE_ENABLE_DEFAULT_LISTENER).getBoolean();
        if (forcePass && !hookedUChat) {
            Sponge.getEventManager().registerListeners(this, NewHonorListener);
        }
    }

    private void checkUpdate() {
        try {
            final String u = "https://api.github.com/repositories/121261530/releases";
            HttpsURLConnection con = (HttpsURLConnection) new URL(u)
                    .openConnection();
            con.setRequestMethod("GET");
            con.getResponseCode();
            try (InputStreamReader reader = new InputStreamReader(con.getInputStream(), Charsets.UTF_8)) {
                JsonObject json = new JsonParser().parse(reader).getAsJsonArray().get(0).getAsJsonObject();
                String v = json.get("tag_name").getAsString()
                        .replace("version", "")
                        .replace("build", "");
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
    }
}
