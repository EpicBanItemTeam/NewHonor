package com.github.euonmyoji.newhonor.command.admin;

import net.yeah.mungsoup.mung.command.SubCommand;
import org.bukkit.command.CommandSender;

public class HelpCommand {

    @SubCommand(
            command = "Admin",
            description = "${newhonor.command.describe.admin}",
            permission = "newhonor.admin",
            console = true
    )
    public void execute(CommandSender sender) {
    }
}
