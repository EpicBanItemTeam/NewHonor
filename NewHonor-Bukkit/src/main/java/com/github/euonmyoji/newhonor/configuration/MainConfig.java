package com.github.euonmyoji.newhonor.configuration;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.manager.LanguageManager;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import net.yeah.mungsoup.mung.configuration.MungConfig;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * @author MungSoup
 */
public class MainConfig extends MungConfig {
    private final String GENERAL_NODE = "general";
    private final String SQL_NODE = "SQL-settings";
    private final String DEFAULT_HONOR_NODE = "default-honors-settings";


    public MainConfig() throws IOException {
        super(NewHonor.instance, "config", "conf");
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

    public String getLanguage() {
        return config.getNode(GENERAL_NODE, "language").getString();
    }

    Optional<List<String>> getDefaultOwnHonors() {
        try {
            return Optional.ofNullable(config.getNode(DEFAULT_HONOR_NODE, "honors").getList(TypeToken.of(String.class)));
        } catch (ObjectMappingException e) {
            return Optional.empty();
        }
    }

    public boolean isMySQLEnable() {
        return "address".equals(config.getNode(SQL_NODE, "address").getString("address"));
    }
}
