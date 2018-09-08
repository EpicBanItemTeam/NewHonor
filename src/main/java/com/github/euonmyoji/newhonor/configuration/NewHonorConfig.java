package com.github.euonmyoji.newhonor.configuration;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.configuration.PlayerConfig;
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
public final class NewHonorConfig {
    public static CommentedConfigurationNode cfg;
    private static final TypeToken<List<String>> LIST_STRING_TYPE = new TypeToken<List<String>>() {
    };
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    public static Path cfgDir;
    public static Path defaultCfgDir;
    private static final String DATA_PATH_NODE = "data-dir-path";
    private static final String CHECK_UPDATE_NODE_PATH = "check-update";
    private static final String LANGUAGE = "lang";
    private static final String DEFAULT_HONORS = "honors";
    private static final String DEFAULT_HONORS_SETTINGS = "default-honors-settings";

    public static final String OLD_COMPATIBLE_UCHAT_NODE_PATH = "compatibleUChat";
    public static final String OLD_USE_PAPI_NODE_PATH = "usePAPI";
    public static final String DISPLAY_HONOR_NODE_PATH = "displayHonor";
    public static final String FORCE_ENABLE_DEFAULT_LISTENER = "force-enable-default-listener";
    public static final String PERMISSION_MANAGE = "permission-manage";

    public static void init() {
        loader = HoconConfigurationLoader.builder()
                .setPath(defaultCfgDir.resolve("config.conf")).build();
        cfg = load();
        cfg.getNode(DATA_PATH_NODE).setValue(cfg.getNode(DATA_PATH_NODE).getString("default"));
        cfg.getNode(CHECK_UPDATE_NODE_PATH).setValue(cfg.getNode(CHECK_UPDATE_NODE_PATH).getBoolean(false));
        cfg.getNode(LANGUAGE).setValue(cfg.getNode(LANGUAGE).getString(Locale.getDefault().toString()));
        cfg.getNode(DEFAULT_HONORS_SETTINGS, "enable").setValue(cfg.getNode(DEFAULT_HONORS_SETTINGS, "enable").getBoolean(true));
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
        String path = cfg.getNode(DATA_PATH_NODE).getString("default");
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
            e.printStackTrace();
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

    private static CommentedConfigurationNode load() {
        try {
            return loader.load();
        } catch (IOException e) {
            return loader.createEmptyNode(ConfigurationOptions.defaults());
        }
    }

    private NewHonorConfig() {
        throw new UnsupportedOperationException();
    }
}