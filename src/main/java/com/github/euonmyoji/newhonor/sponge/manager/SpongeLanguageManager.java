package com.github.euonmyoji.newhonor.sponge.manager;

import com.github.euonmyoji.newhonor.common.manager.LanguageManager;
import com.github.euonmyoji.newhonor.sponge.NewHonor;
import com.github.euonmyoji.newhonor.sponge.configuration.PluginConfig;
import com.github.euonmyoji.newhonor.sponge.util.Util;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static com.github.euonmyoji.newhonor.common.manager.LanguageManager.getString;

/**
 * @author yinyangshi
 */
public class SpongeLanguageManager {
    private static Locale locale;
    private static Path langFile;

    public static Text getText(String key) {
        return Util.toText(getString(key));
    }

    /**
     * @param commandName 命令名
     * @return the command describe
     */
    public static Text getCommandDescribe(String commandName) {
        return Util.toText(getString("newhonor.command.describe." + commandName));
    }

    private static void init() {
        try {
            Path langFolder = PluginConfig.cfgDir.resolve("lang");
            if (Files.notExists(langFolder)) {
                Files.createDirectory(langFolder);
            }
            try {
                if (Files.notExists(langFile)) {
                    Sponge.getAssetManager().getAsset(NewHonor.plugin, "lang/" + locale.toString() + ".lang")
                            .orElseThrow(() -> new FileNotFoundException("asset didn't found locale language file!"))
                            .copyToFile(langFile);
                }
            } catch (FileNotFoundException ignore) {
                NewHonor.logger.info("locale language file not found");
                langFile = PluginConfig.cfgDir.resolve("lang").resolve(Locale.US.toString() + ".lang");
                Sponge.getAssetManager().getAsset(NewHonor.plugin, "lang/" + Locale.US.toString() + ".lang")
                        .orElseThrow(() -> new IOException("asset didn't found language file!"))
                        .copyToFile(langFile);
            }
        } catch (IOException e) {
            NewHonor.logger.error("IOE", e);
        }
    }

    public static void reload() {
        try {
            locale = PluginConfig.getUsingLang();
            langFile = PluginConfig.cfgDir.resolve("lang").resolve(locale.toString() + ".lang");
            init();
            LanguageManager.reload(langFile);
        } catch (IOException e) {
            NewHonor.logger.error("reload language file error!", e);
        }
    }

    private SpongeLanguageManager() {
        throw new UnsupportedOperationException();
    }
}
