package com.github.euonmyoji.newhonor.bukkit.command.admin;

import com.github.euonmyoji.newhonor.bukkit.NewHonor;
import com.github.euonmyoji.newhonor.bukkit.mung.command.SubCommand;
import com.github.euonmyoji.newhonor.manager.LanguageManager;
import org.bukkit.command.CommandSender;

import java.io.IOException;

/**
 * @author MungSoup
 */
public class ReloadCommand {

    @SubCommand(
            command = "Admin Reload",
            description = "重载配置",
            permission = "honor.admin"
    )
    public void execute(CommandSender sender) {
        try {
            LanguageManager.reload(NewHonor.langPath);
            NewHonor.mainConfig.reload();
            sender.sendMessage(NewHonor.prefix + "成功重载配置文件!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
