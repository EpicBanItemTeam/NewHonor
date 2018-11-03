package com.github.euonmyoji.newhonor.command.admin;

import com.github.euonmyoji.newhonor.NewHonor;
import net.yeah.mungsoup.mung.command.SubCommand;
import org.bukkit.entity.Player;

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
        //同时包含了语言文件的reload in mainConfig
        NewHonor.mainConfig.reload();
        p.sendMessage(NewHonor.prefix + "成功重载配置文件!");
    }
}
