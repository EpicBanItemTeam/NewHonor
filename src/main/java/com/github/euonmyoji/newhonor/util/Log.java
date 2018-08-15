package com.github.euonmyoji.newhonor.util;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.configuration.NewHonorConfig;
import org.spongepowered.api.scheduler.Task;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static java.time.temporal.ChronoField.*;

/**
 * @author yinyangshi
 */
public class Log {
    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2).toFormatter();
    private static final Path PATH = NewHonorConfig.cfgDir.resolve("logs");
    private static final Object LOCK = new Object();

    static {
        if (Files.notExists(PATH)) {
            try {
                Files.createDirectory(PATH);
            } catch (IOException e) {
                NewHonor.plugin.logger.warn("create log dir error", e);
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
                    NewHonor.plugin.logger.debug("记录头衔info异常", e);
                }
            }
        }).submit(NewHonor.plugin);
    }

    private static String getTime() {
        return String.format("[%s] ", LocalTime.now().format(FORMATTER));
    }

    private static String getFileName() {
        return LocalDate.now() + ".log";
    }
}
