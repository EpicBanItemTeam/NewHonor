package com.github.euonmyoji.newhonor.command.admin;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.manager.LanguageManager;
import net.yeah.mungsoup.mung.command.SubCommand;
import org.bukkit.entity.Player;

import java.io.IOException;

/**
 * @author MungSoup
 */
public class ReloadCommand {

    @SubCommand(
            command = "Admin reload",
            description = "${newhonor.command.describe.admin.reload}",
            permission = "honor.admin"
    )
    public void execute(Player p) {
        try {
            LanguageManager.reload(NewHonor.langPath);
            NewHonor.mainConfig.reload();
            p.sendMessage(NewHonor.prefix + "成功重载配置文件!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
