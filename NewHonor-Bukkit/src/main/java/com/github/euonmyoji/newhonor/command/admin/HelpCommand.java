package com.github.euonmyoji.newhonor.command.admin;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.mung.command.SubCommand;
import org.bukkit.command.CommandSender;

/**
 * @author MungSoup
 */
public class HelpCommand {

    @SubCommand(
            command = "Admin",
            description = "",
            permission = "newhonor.admin",
            console = true
    )
    public void execute(CommandSender sender) {
        NewHonor.registerer.setNoCommandMsg("Test");
    }
}
