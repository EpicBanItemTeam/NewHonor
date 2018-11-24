package com.github.euonmyoji.newhonor.manager;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.configuration.PluginConfig;
import com.github.euonmyoji.newhonor.util.Util;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.euonmyoji.newhonor.manager.LanguageManager.getString;


/**
 * @author yinyangshi
 */
public class SpongeLanguageManager {
    private static String lang;
    private static Path langFile;

    private SpongeLanguageManager() {
        throw new UnsupportedOperationException();
    }

    public static Text getText(String key) {
        return Util.toText(getString(key));
    }

    public static Text getText(String key, String def) {
        return Util.toText(getString(key, def));
    }

    public static void init() {
        try {
            Files.createDirectories(PluginConfig.cfgDir.resolve("lang"));
        } catch (IOException e) {
            NewHonor.logger.warn("create lang dir error", e);
        }
        for (String lang : new String[]{"lang/en_US.lang", "lang/zh_CN.lang"}) {
            Sponge.getAssetManager().getAsset(NewHonor.plugin, lang)
                    .ifPresent(asset -> {
                        try {
                            asset.copyToFile(PluginConfig.cfgDir.resolve(lang));
                        } catch (IOException e) {
                            NewHonor.logger.warn("copy language file error", e);
                        }
                    });
        }
    }

    private static void check() {
        try {
            Path langFolder = PluginConfig.cfgDir.resolve("lang");
            if (Files.notExists(langFolder)) {
                Files.createDirectory(langFolder);
            }
            try {
                if (Files.notExists(langFile)) {
                    Sponge.getAssetManager().getAsset(NewHonor.plugin, "lang/" + lang + ".lang")
                            .orElseThrow(() -> new FileNotFoundException("asset didn't found language file!"))
                            .copyToFile(langFile);
                }
            } catch (FileNotFoundException ignore) {
                NewHonor.logger.info("locale language file not found");
                langFile = PluginConfig.cfgDir.resolve("lang/en_US.lang");
                Sponge.getAssetManager().getAsset(NewHonor.plugin, "lang/en_US.lang")
                        .orElseThrow(() -> new IOException("asset didn't found language file!"))
                        .copyToFile(langFile);
            }
        } catch (IOException e) {
            NewHonor.logger.error("IOE", e);
        }
    }

    public static void reload() {
        try {
            lang = PluginConfig.getUsingLang();
            langFile = PluginConfig.cfgDir.resolve("lang/" + lang + ".lang");
            check();
            LanguageManager.reload(langFile);
        } catch (IOException e) {
            NewHonor.logger.error("reload language file error!", e);
        }
    }
}
