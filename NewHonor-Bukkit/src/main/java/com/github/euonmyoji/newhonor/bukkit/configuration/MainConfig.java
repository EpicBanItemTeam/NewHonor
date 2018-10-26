package com.github.euonmyoji.newhonor.bukkit.configuration;

import com.github.euonmyoji.newhonor.bukkit.NewHonor;
import com.github.euonmyoji.newhonor.bukkit.mung.configuration.MungConfig;

import java.io.IOException;
import java.util.Locale;

/**
 * @author MungSoup
 */
public class MainConfig extends MungConfig {
    private final String LANGUAGE_NODE = "lang";

    public MainConfig() throws IOException {
        super(NewHonor.instance, "config", "conf");
    }

    @Override
    public void saveDefault() {
        setDefault(LANGUAGE_NODE, Locale.getDefault().toString());
    }

    public String getLanguage() {
        return getString(LANGUAGE_NODE);
    }
}
