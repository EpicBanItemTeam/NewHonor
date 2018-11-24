package com.github.euonmyoji.newhonor.configuration;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.configuration.PlayerConfig;
import com.github.euonmyoji.newhonor.enums.ListHonorStyle;
import com.github.euonmyoji.newhonor.manager.MysqlManager;
import com.github.euonmyoji.newhonor.manager.SpongeLanguageManager;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.github.euonmyoji.newhonor.manager.LanguageManager.getString;


/**
 * @author yinyangshi
 */
public final class PluginConfig {
    public static final String DISPLAY_HONOR = "displayHonor";
    public static final String FORCE_ENABLE_DEFAULT_LISTENER = "force-enable-default-listener";
    private static final TypeToken<List<String>> LIST_STRING_TYPE = new TypeToken<List<String>>() {
    };
    private static final String DATA_DIR = "data-dir-path";
    private static final String CHECK_UPDATE = "check-update";
    private static final String LANGUAGE = "lang";
    private static final String DEFAULT_HONORS = "honors";
    private static final String DEFAULT_HONORS_SETTINGS = "default-honors-settings";
    private static final String INTERVAL_TICKS = "effects-check-interval-ticks";
    private static final String PARALLEL_GOAL = "parallel-goal";
    private static final String PERMISSION_MANAGE = "permission-manage";
    public static int parallelGoal = 16;
    public static CommentedConfigurationNode cfg;
    public static CommentedConfigurationNode generalNode;
    public static Path cfgDir;
    public static Path defaultCfgDir;
    private static ConfigurationLoader<CommentedConfigurationNode> loader;

    private PluginConfig() {
        throw new UnsupportedOperationException();
    }

    public static void init() {
        loader = HoconConfigurationLoader.builder()
                .setPath(defaultCfgDir.resolve("config.conf")).build();
        loadNode();

        generalNode.getNode(DATA_DIR).getString("default");
        generalNode.getNode(CHECK_UPDATE).getBoolean(false);
        generalNode.getNode(LANGUAGE).getString(Locale.getDefault().toString());
        generalNode.getNode("default-list-style").getString("TEXT");

        cfg.getNode(DEFAULT_HONORS_SETTINGS, "enable").getBoolean(true);

        String path = generalNode.getNode(DATA_DIR).getString("default");
        cfgDir = "default".equals(path) ? defaultCfgDir : Paths.get(path);

        SpongeLanguageManager.init();
        SpongeLanguageManager.reload();

        CommentedConfigurationNode extraNode = cfg.getNode("extra");
        extraNode.getNode(INTERVAL_TICKS).getInt(8);
        extraNode.getNode(PARALLEL_GOAL).getInt(16);

        try {
            if (cfg.getNode(DEFAULT_HONORS_SETTINGS, DEFAULT_HONORS).isVirtual()) {
                cfg.getNode(DEFAULT_HONORS_SETTINGS, DEFAULT_HONORS).setValue(LIST_STRING_TYPE, new ArrayList<String>() {{
                    add("default");
                }});
            }
        } catch (ObjectMappingException e) {
            NewHonor.logger.error("Exception while set default-own-honors!", e);
        }

        //the hook init(?)
        //已经不在使用的N个配置文件node
        cfg.removeChild("usePAPI");
        cfg.removeChild("compatibleUChat");
        cfg.removeChild(DISPLAY_HONOR);
        cfg.removeChild(PERMISSION_MANAGE);
        cfg.removeChild(DATA_DIR);
        cfg.removeChild(CHECK_UPDATE);
        cfg.removeChild(LANGUAGE);
        cfg.removeChild(FORCE_ENABLE_DEFAULT_LISTENER);

        generalNode.getNode(DISPLAY_HONOR).getBoolean(false);
        generalNode.getNode(FORCE_ENABLE_DEFAULT_LISTENER).getBoolean(false);
        generalNode.getNode(PERMISSION_MANAGE).getBoolean(false);

        //comments
        generalNode.getNode(PERMISSION_MANAGE).setComment(generalNode.getNode(PERMISSION_MANAGE).getComment()
                .orElse(getString("newhonor.configuration.permissionmanage.comment", "If you enable this, the honor must be given by permission" +
                        "\n(The player who doesn't have the permission of honor, the player won't use it any longer." +
                        "\neg: The honor's id is 'honorid' then you should give player permission:'newhonor.honor.honorid'.")));
        generalNode.getNode(DISPLAY_HONOR).setComment(generalNode.getNode(DISPLAY_HONOR).getComment()
                .orElse(getString("newhonor.configuration.displayhonor.comment", "Display honor in the tab & head." +
                        "\nIf you installed nucleus, you may change something in the nucleus configuration." +
                        "\nThe display value can not longer than 16 chars!(including color code: &?)")));
        generalNode.getNode(DATA_DIR).setComment(generalNode.getNode(DATA_DIR).getComment()
                .orElse(getString("newhonor.configuration.datadirpath.comment",
                        "Change the data dir (player data&honor data&effects data)")));
        generalNode.getNode(FORCE_ENABLE_DEFAULT_LISTENER).setComment(generalNode.getNode(FORCE_ENABLE_DEFAULT_LISTENER).getComment()
                .orElse(getString("newhonor.configuration.forceenabledefaultlistener.comment",
                        "force enable the default listener when installed nucleus or enabled displayHonor to show honor in the chat")));
        generalNode.getNode(LANGUAGE).setComment(generalNode.getNode(LANGUAGE).getComment()
                .orElse(getString("newhonor.configuration.lang.comment",
                        "set the plugin language (if supported)")));
        generalNode.getNode(CHECK_UPDATE).setComment(generalNode.getNode(CHECK_UPDATE).getComment()
                .orElse(getString("newhonor.configuration.checkupdate.comment",
                        "check plugin update when starting the server (async)")));
        generalNode.getNode("default-list-style").setComment(generalNode.getNode("default-list-style").getComment()
                .orElse(getString("newhonor.configuration.defaultliststyle.comment",
                        "The default list honors style(TEXT OR ITEM)")));
        extraNode.setComment(extraNode.getComment().orElse(getString("newhonor.configuration.extra.comment"
                , "the extra settings, you can ignore this node")));
        extraNode.getNode(INTERVAL_TICKS).setComment(extraNode.getNode(INTERVAL_TICKS).getComment().orElse(getString(
                "newhonor.configuration.extra.intervalticks.comment"
                , "多少tick检查一次效果组是否该刷新了 默认为8tick")));
        extraNode.getNode(PARALLEL_GOAL).setComment(extraNode.getNode(PARALLEL_GOAL).getComment().orElse(getString(
                "newhonor.configuration.extra.parallelgoal.comment"
                , "If the operation's size is bigger than this goal, it will be parallel. (if supported). default: 16")));

        MysqlManager.init();
        save();
        reload();
    }

    public static String getUsingLang() {
        return generalNode.getNode(LANGUAGE).getString(Locale.getDefault().toString());
    }

    public static boolean isCheckUpdate() {
        return generalNode.getNode(CHECK_UPDATE).getBoolean(false);
    }

    public static void reload() {
        loadNode();

        parallelGoal = cfg.getNode("extra", PARALLEL_GOAL).getInt(16);

        String path = generalNode.getNode(DATA_DIR).getString("default");
        cfgDir = "default".equals(path) ? defaultCfgDir : Paths.get(path);
        NewHonor.logger.info("using data dir path:" + cfgDir);
        MysqlManager.reloadSQLInfo();
        String mysql = "mysql";
        String local = "local";
        if (!MysqlManager.enable && mysql.equals(PlayerConfig.getDefaultConfigType())) {
            PlayerConfig.setDefaultConfigType(local);
        } else if (MysqlManager.enable && local.equals(PlayerConfig.getDefaultConfigType())) {
            PlayerConfig.setDefaultConfigType(mysql);
        }
    }

    public static void save() {
        try {
            loader.save(cfg);
        } catch (IOException e) {
            NewHonor.logger.warn("error when saving plugin config", e);
        }
    }

    public static List<String> getDefaultOwnHonors() {
        try {
            return cfg.getNode(DEFAULT_HONORS_SETTINGS, "enable").getBoolean(true) ?
                    cfg.getNode(DEFAULT_HONORS_SETTINGS, DEFAULT_HONORS).getValue(LIST_STRING_TYPE) : null;
        } catch (ObjectMappingException e) {
            NewHonor.logger.error("default own honor is error!", e);
        }
        return null;
    }

    public static ListHonorStyle defaultListHonorStyle() {
        return ListHonorStyle.valueOf(generalNode.getNode("default-list-style").getString("TEXT").toUpperCase());
    }

    public static int getIntervalTicks() {
        return cfg.getNode("extra", INTERVAL_TICKS).getInt(8);
    }

    public static boolean permissionManageHonors() {
        return generalNode.getNode(PERMISSION_MANAGE).getBoolean(false);
    }

    private static void loadNode() {
        try {
            cfg = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
        } catch (IOException e) {
            NewHonor.logger.warn("load plugin config failed, creating new one", e);
            cfg = loader.createEmptyNode(ConfigurationOptions.defaults());
        }
        generalNode = cfg.getNode("general");
    }
}