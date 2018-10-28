package com.github.euonmyoji.newhonor.configuration;

import com.github.euonmyoji.newhonor.NewHonor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author MungSoup
 */
public class Log {
    private Path path;

    public Log(OfflinePlayer player) {
        try {
            path = Files.createFile(Files.createDirectories(NewHonor.instance.getDataFolder().toPath().resolve("logs/" + player + ".log")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logGet(CommandSender giver, String honorID) {
        log(String.format("[%s给予]获得头衔%s", giver.getName(), honorID));
    }

    public void logLose(CommandSender sender, String honorID) {
        log(String.format("[%s撤销]失去头衔%s", sender.getName(), honorID));
    }

    public void logLose(String honorID) {
        log(String.format("[权限到期]失去头衔%s", honorID));
    }

    public void log(String log) {
        try {
            new PrintWriter(Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND))
                    .printf("%s %s", getDate(), log).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDate() {
        return String.format("[%s]", LocalDate.now().format(DateTimeFormatter.ofPattern("MM.dd kk:mm:ss")));
    }
}
