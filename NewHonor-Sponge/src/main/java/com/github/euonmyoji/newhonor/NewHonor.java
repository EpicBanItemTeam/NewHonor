package com.github.euonmyoji.newhonor;

import com.github.euonmyoji.newhonor.api.configuration.PlayerConfig;
import com.github.euonmyoji.newhonor.api.data.HonorData;
import com.github.euonmyoji.newhonor.api.event.NewHonorReloadEvent;
import com.github.euonmyoji.newhonor.api.manager.HonorManager;
import com.github.euonmyoji.newhonor.command.HonorCommand;
import com.github.euonmyoji.newhonor.configuration.EffectsConfig;
import com.github.euonmyoji.newhonor.configuration.HonorConfig;
import com.github.euonmyoji.newhonor.configuration.PluginConfig;
import com.github.euonmyoji.newhonor.listener.NewHonorMessageListener;
import com.github.euonmyoji.newhonor.manager.*;
import com.github.euonmyoji.newhonor.task.EffectsOfferTask;
import com.github.euonmyoji.newhonor.task.HaloEffectsOfferTask;
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
    public static final String VERSION = "@spongeVersion@";
    public static final NewHonorMessageChannel M_MESSAGE = new NewHonorMessageChannel();
    static final String NUCLEUS_ID = "nucleus";
    static final String PAPI_ID = "placeholderapi";
    static final String UCHAT_ID = "ultimatechat";
    private static final Object CACHE_LOCK = new Object();
    public static Logger logger;
    public static NewHonor plugin;
    private final NewHonorMessageListener NewHonorListener = new NewHonorMessageListener();
    public boolean enabledPlaceHolderAPI = false;
    @Inject
    @ConfigDir(sharedRoot = false)
    private Path defaultCfgDir;
    private boolean hookedNucleus = false;

    /**
     * 清掉插件缓存 任务缓存
     */
    public static void clearCaches() {
        Sponge.getServiceManager().provideUnchecked(HonorManager.class).clear();
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
            Sponge.getServiceManager().provideUnchecked(HonorManager.class).remove(uuid);
        }
    }

    /**
     * 对玩家配置进行检查 然后更新缓存
     *
     * @param pd 玩家配置
     */
    public static void updateCache(PlayerConfig pd) {
        Runnable r = () -> {
            try {
                HonorManager honorManager = Sponge.getServiceManager().provideUnchecked(HonorManager.class);
                pd.checkUsingHonor();
                honorManager.remove(pd.getUUID());
                if (pd.isUseHonor()) {
                    HonorData data = pd.getUsingHonorValue();
                    if (data != null) {
                        honorManager.setUsingHonor(pd.getUUID(), data);
                    }
                    if (pd.isEnabledEffects()) {
                        HonorConfig.getEffectsID(pd.getUsingHonorID()).ifPresent(s -> honorManager.setUsingEffects(pd.getUUID(), s));
                    }
                }
            } catch (Exception e) {
                logger.error("error about data!", e);
            }
        };
        Optional<Runnable> r2 = Sponge.getServer().getPlayer(pd.getUUID()).map(player -> ScoreBoardManager::refresh);

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

    @Inject
    public void setLogger(Logger l) {
        logger = l;
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        plugin = this;
        Sponge.getServiceManager().setProvider(this, HonorManager.class, new HonorManagerImpl());
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

    @Listener
    public void onStarted(GameStartedServerEvent event) {
        //纽尊严? 过于真实        idea from sponge 咕咕咕 group
        Sponge.getCommandManager().register(this, HonorCommand.honor, "honor", "honour", "newhonor", "头衔", "称号", "纽尊严", "tx", "ch");
        hook();
        try {
            TaskManager.update();
        } catch (IOException e) {
            logger.warn("Task init error", e);
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

    public void reload() {
        try {
            Sponge.getEventManager().post(new NewHonorReloadEvent());
        } catch (NoSuchMethodError ignore) {

        }
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
            PlaceholderManager.getInstance();
            if (!enabledPlaceHolderAPI) {
                logger.info("hooked PAPI, you can use '%newhonor%' now.");
            }
            enabledPlaceHolderAPI = true;
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
        if (forcePass) {
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
