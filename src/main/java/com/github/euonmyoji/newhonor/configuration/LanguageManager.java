package com.github.euonmyoji.newhonor.configuration;

import com.github.euonmyoji.newhonor.NewHonor;
import com.google.common.base.Charsets;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * @author yinyangshi
 */
public class LanguageManager {
    private static Locale locale;
    private static Path langFile;
    private static ResourceBundle res;
    private String value;

    public LanguageManager replace(String old, String instead) {
        value = value.replace(old, instead);
        return this;
    }

    public Text build() {
        return TextSerializers.FORMATTING_CODE.deserialize(value);
    }

    public static LanguageManager langBuilder(String key) {
        return new LanguageManager(key);
    }

    public static Text getText(String key) {
        return TextSerializers.FORMATTING_CODE.deserialize(getStringSafely(key));
    }

    /**
     * @param commandName 命令名
     * @return the command describe
     */

    public static Text getCommandDescribe(String commandName) {
        return TextSerializers.FORMATTING_CODE.deserialize(getStringSafely("newhonor.command.describe." + commandName));
    }

    private static void init() {
        try {
            if (Files.notExists(NewHonorConfig.cfgDir.resolve("lang"))) {
                Files.createDirectory(NewHonorConfig.cfgDir.resolve("lang"));
            }
            try {
                if (Files.notExists(langFile)) {
                    Sponge.getAssetManager().getAsset(NewHonor.plugin, "lang/" + locale.toString() + ".lang")
                            .orElseThrow(() -> new FileNotFoundException("asset didn't found locale language file!"))
                            .copyToFile(langFile);
                }
            } catch (FileNotFoundException ignore) {
                NewHonor.plugin.logger.info("locale language file not found");
                langFile = NewHonorConfig.cfgDir.resolve("lang").resolve(Locale.US.toString() + ".lang");
                Sponge.getAssetManager().getAsset(NewHonor.plugin, "lang/" + Locale.US.toString() + ".lang")
                        .orElseThrow(() -> new IOException("asset didn't found language file!"))
                        .copyToFile(langFile);
            }
        } catch (IOException e) {
            NewHonor.plugin.logger.error("IOE", e);
        }
    }

    public static void reload() {
        try {
            locale = NewHonorConfig.getUsingLang();
            langFile = NewHonorConfig.cfgDir.resolve("lang").resolve(locale.toString() + ".lang");
            init();
            res = new PropertyResourceBundle(new InputStreamReader(Files.newInputStream(langFile), Charsets.UTF_8));
        } catch (IOException e) {
            NewHonor.plugin.logger.error("reload language file error!", e);
        }
    }

    private static String getStringSafely(String key) {
        try {
            return res.getString(key);
        } catch (Exception e) {
            return key;
        }
    }

    private LanguageManager(String key) {
        this.value = getStringSafely(key);
    }
}
