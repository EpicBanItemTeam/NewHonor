package com.github.euonmyoji.newhonor.configuration;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.manager.LanguageManager;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import net.yeah.mungsoup.mung.configuration.MungConfig;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * @author MungSoup yinyangshi
 */
public class MainConfig extends MungConfig {
    private final String GENERAL_NODE = "general";
    private final String DEFAULT_HONOR_NODE = "default-honors-settings";

    public MainConfig() throws IOException {
        String fileName = "config.conf";
        Path dataFolder = NewHonor.plugin.getDataFolder().toPath();

        Path path = Files.createDirectories(dataFolder).resolve(fileName);
        loader = HoconConfigurationLoader.builder().setPath(path).build();
        reload();

        if (Files.notExists(path)) {
            reload();
            saveDefault();
            save();
        }
        reload();
    }

    @Override
    public void saveDefault() {
        setDefault(null, Locale.getDefault().toString(), GENERAL_NODE, "language");
        setDefault(null, true, DEFAULT_HONOR_NODE, "enable");
        setDefault(LanguageManager.getString("newhonor.configuration.defaultliststyle.comment"), ImmutableList.of("default"), DEFAULT_HONOR_NODE, "honors");
        setDefault(null, false, GENERAL_NODE, "check-update");
        setDefault(LanguageManager.getString("newhonor.configuration.datadirpath.comment"), "default", GENERAL_NODE, "data-dir-path");
        setDefault(null, "ITEM", GENERAL_NODE, "default-list-style");
        setDefault(LanguageManager.getString("newhonor.configuration.displayhonor.comment"), true, GENERAL_NODE, "displayHonor");
        setDefault(LanguageManager.getString("newhonor.configuration.permissionmanage.comment"), true, GENERAL_NODE, "permission-manage");
    }

    @Override
    public void reload() {
        super.reload();
        try {
            String usingLang = getLanguage() + ".lang";
            Path langPath = NewHonor.plugin.getDataFolder().toPath().resolve("lang/" + usingLang);
            if (Files.notExists(langPath)) {
                Files.copy(NewHonor.plugin.getResource("assets/newhonor/lang/" + usingLang), langPath);
            }
            LanguageManager.reload(langPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getLanguage() {
        return cfg.getNode(GENERAL_NODE, "language").getString(Locale.getDefault().toString());
    }

    List<String> getDefaultOwnHonors() {
        try {
            return cfg.getNode(DEFAULT_HONOR_NODE, "enable").getBoolean(true) ?
                    cfg.getNode(DEFAULT_HONOR_NODE, "honors").getList(TypeToken.of(String.class)) : null;
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }

        return null;
    }
}
