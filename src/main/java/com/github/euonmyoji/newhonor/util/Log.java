package com.github.euonmyoji.newhonor.util;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.configuration.PluginConfig;
import org.spongepowered.api.scheduler.Task;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * @author yinyangshi
 */
public class Log {
    private static final Path PATH = PluginConfig.cfgDir.resolve("logs");
    private static final Object LOCK = new Object();

    static {
        if (Files.notExists(PATH)) {
            try {
                Files.createDirectory(PATH);
            } catch (IOException e) {
                NewHonor.logger.warn("create log dir error", e);
            }
        }
    }

    public static void info(String msg) {
        Task.builder().async().execute(() -> {
            synchronized (LOCK) {
                try {
                    try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(PATH.resolve(getFileName()),
                            StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
                        out.println(getTime() + msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    NewHonor.logger.debug("记录头衔info异常", e);
                }
            }
        }).submit(NewHonor.plugin);
    }

    private static String getTime() {
        return String.format("[%s]", LocalTime.now());
    }

    private static String getFileName() {
        return LocalDate.now() + ".log";
    }
}
