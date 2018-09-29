package com.github.euonmyoji.newhonor.manager;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.configuration.PluginConfig;
import com.github.euonmyoji.newhonor.util.Util;
import com.google.common.base.Charsets;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

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

    public static Builder langBuilder(String key) {
        return new Builder(key);
    }

    public static Builder langBuilder(String key, String def) {
        Builder builder = new Builder(key);
        if (builder.value.equals(builder.key)) {
            builder.value = def;
        }
        return builder;
    }

    public static Text getText(String key) {
        return Util.toText(getStringSafely(key));
    }

    public static String getString(String key, String def) {
        String s = getStringSafely(key);
        return key.equals(s) ? def : s;
    }

    /**
     * @param commandName 命令名
     * @return the command describe
     */

    public static Text getCommandDescribe(String commandName) {
        return Util.toText(getStringSafely("newhonor.command.describe." + commandName));
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
            res = new PropertyResourceBundle(new InputStreamReader(Files.newInputStream(langFile), Charsets.UTF_8));
        } catch (IOException e) {
            NewHonor.logger.error("reload language file error!", e);
        }
    }

    private static String getStringSafely(String key) {
        try {
            return res.getString(key);
        } catch (Exception e) {
            return key;
        }
    }

    private LanguageManager() {
        throw new UnsupportedOperationException();
    }

    public static class Builder {
        private final String key;
        private String value;

        public Builder replace(String old, String instead) {
            value = value.replace(old, instead);
            return this;
        }

        public Builder replaceName(User user) {
            value = value.replace("%player%", user.getName());
            return this;
        }

        public Builder replaceHonorid(String honorid) {
            value = value.replace("%honorid%", honorid);
            return this;
        }

        public Builder replaceHonor(String honor) {
            value = value.replace("%honor%", honor);
            return this;
        }

        public Text build() {
            return Util.toText(value);
        }

        private Builder(String key) {
            this.key = key;
            this.value = getStringSafely(key);
        }
    }
}
