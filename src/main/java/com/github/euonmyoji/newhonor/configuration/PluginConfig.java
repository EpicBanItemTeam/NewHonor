package com.github.euonmyoji.newhonor.configuration;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.configuration.PlayerConfig;
import com.github.euonmyoji.newhonor.manager.LanguageManager;
import com.github.euonmyoji.newhonor.manager.MysqlManager;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author yinyangshi
 */
public final class PluginConfig {
    public static int parallelGoal = 16;

    public static CommentedConfigurationNode cfg;
    private static final TypeToken<List<String>> LIST_STRING_TYPE = new TypeToken<List<String>>() {
    };
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    public static Path cfgDir;
    public static Path defaultCfgDir;
    private static final String DATA_DIR = "data-dir-path";
    private static final String CHECK_UPDATE_NODE_PATH = "check-update";
    private static final String LANGUAGE = "lang";
    private static final String DEFAULT_HONORS = "honors";
    private static final String DEFAULT_HONORS_SETTINGS = "default-honors-settings";
    private static final String INTERVAL_TICKS = "effects-check-interval-ticks";
    private static final String PARALLEL_GOAL = "parallel-goal";

    public static final String OLD_COMPATIBLE_UCHAT_NODE = "compatibleUChat";
    public static final String OLD_USE_PAPI_NODE = "usePAPI";
    public static final String DISPLAY_HONOR_NODE = "displayHonor";
    public static final String FORCE_ENABLE_DEFAULT_LISTENER = "force-enable-default-listener";
    public static final String PERMISSION_MANAGE = "permission-manage";

    public static void init() {
        loader = HoconConfigurationLoader.builder()
                .setPath(defaultCfgDir.resolve("config.conf")).build();
        cfg = load();
        cfg.getNode(DATA_DIR).setValue(cfg.getNode(DATA_DIR).getString("default"));
        cfg.getNode(CHECK_UPDATE_NODE_PATH).setValue(cfg.getNode(CHECK_UPDATE_NODE_PATH).getBoolean(false));
        cfg.getNode(LANGUAGE).setValue(cfg.getNode(LANGUAGE).getString(Locale.getDefault().toString()));
        cfg.getNode(DEFAULT_HONORS_SETTINGS, "enable").setValue(cfg.getNode(DEFAULT_HONORS_SETTINGS, "enable").getBoolean(true));

        cfg.getNode(DISPLAY_HONOR_NODE).setComment(cfg.getNode(DISPLAY_HONOR_NODE).getComment()
                .orElse(LanguageManager.getString("newhonor.configuration.displayhonor.comment", "Display honor in the tab & head." +
                        "\nIf you installed nucleus, you may change something in the nucleus configuration." +
                        "\nThe display value can not longer than 16 chars!(including color code: &?)")));

        cfg.getNode(DATA_DIR).setComment(cfg.getNode(DATA_DIR).getComment()
                .orElse(LanguageManager.getString("newhonor.configuration.datadirpath.comment",
                        "Change the data dir (player data&honor data&effects data)")));

        CommentedConfigurationNode extraNode = cfg.getNode("extra");
        extraNode.getNode(INTERVAL_TICKS).setValue(extraNode.getNode(INTERVAL_TICKS).getInt(8));
        extraNode.getNode(PARALLEL_GOAL).setValue(extraNode.getNode(PARALLEL_GOAL).getInt(16));

        extraNode.setComment(extraNode.getComment().orElse(LanguageManager.getString("newhonor.configuration.extra.comment"
                , "the extra settings, you can ignore this node")));

        extraNode.getNode(INTERVAL_TICKS).setComment(extraNode.getComment().orElse(LanguageManager.getString("newhonor.configuration.extra.intervalticks.comment"
                , "多少tick检查一次效果组是否该刷新了 默认为8tick")));

        extraNode.getNode(PARALLEL_GOAL).setComment(extraNode.getComment().orElse(LanguageManager.getString("newhonor.configuration.extra.parallelgoal.comment"
                , "If the operation's size is bigger than this goal, it will be parallel; (if supported). default: 16")));
        try {
            if (cfg.getNode(DEFAULT_HONORS_SETTINGS, DEFAULT_HONORS).isVirtual()) {
                cfg.getNode(DEFAULT_HONORS_SETTINGS, DEFAULT_HONORS).setValue(LIST_STRING_TYPE, new ArrayList<String>() {{
                    add("default");
                }});
            }
        } catch (ObjectMappingException e) {
            NewHonor.logger.error("Exception while set default has honors!", e);
        }
        MysqlManager.init();
        save();
        reload();
    }

    public static Locale getUsingLang() {
        String[] args = cfg.getNode(LANGUAGE).getString(Locale.getDefault().toString()).split("_", 2);
        return new Locale(args[0], args[1]);
    }

    public static boolean isCheckUpdate() {
        return cfg.getNode(CHECK_UPDATE_NODE_PATH).getBoolean(false);
    }

    public static void reload() {
        cfg = load();

        parallelGoal = cfg.getNode("extra", PARALLEL_GOAL).getInt(16);

        String path = cfg.getNode(DATA_DIR).getString("default");
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

    public static CommentedConfigurationNode getCfg() {
        return cfg;
    }


    public static void save() {
        try {
            loader.save(cfg);
        } catch (IOException e) {
            NewHonor.logger.warn("error when saving plugin config", e);
        }
    }

    public static Optional<List<String>> getDefaultOwnHonors() {
        try {
            return cfg.getNode(DEFAULT_HONORS_SETTINGS, "enable").getBoolean(true) ?
                    Optional.ofNullable(cfg.getNode(DEFAULT_HONORS_SETTINGS, DEFAULT_HONORS).getValue(LIST_STRING_TYPE)) : Optional.empty();
        } catch (ObjectMappingException e) {
            NewHonor.logger.error("default own honor is error!", e);
            return Optional.of(Collections.emptyList());
        }
    }

    public static int getIntervalTicks() {
        return cfg.getNode("extra", INTERVAL_TICKS).getInt(8);
    }

    private static CommentedConfigurationNode load() {
        try {
            return loader.load();
        } catch (IOException e) {
            NewHonor.logger.warn("load plugin config failed, creating new one", e);
            return loader.createEmptyNode(ConfigurationOptions.defaults());
        }
    }

    private PluginConfig() {
        throw new UnsupportedOperationException();
    }
}