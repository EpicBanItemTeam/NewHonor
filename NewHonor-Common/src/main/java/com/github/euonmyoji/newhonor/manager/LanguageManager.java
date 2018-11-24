package com.github.euonmyoji.newhonor.manager;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * @author yinyangshi
 */
public class LanguageManager {
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


    public static String getString(String key, String def) {
        String s = getString(key);
        //noinspection StringEquality  就是看引用是不是一样的()
        return key == s ? def : s;
    }

    public static String getString(String key) {
        try {
            return res.getString(key);
        } catch (Exception e) {
            return key;
        }
    }

    public static void reload(Path path) throws IOException {
        res = new PropertyResourceBundle(Files.newBufferedReader(path, Charset.forName("UTF8")));
    }

    public static class Builder {
        private final String key;
        private String value;

        private Builder(String key) {
            this.key = key;
            this.value = getString(key);
        }

        public Builder replace(String old, String instead) {
            value = value.replace(old, instead);
            return this;
        }

        public Builder replaceName(String name) {
            value = value.replace("%player%", name);
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

        public String build() {
            return value;
        }
    }
}
