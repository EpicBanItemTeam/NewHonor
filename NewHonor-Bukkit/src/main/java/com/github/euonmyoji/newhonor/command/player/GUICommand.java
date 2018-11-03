package com.github.euonmyoji.newhonor.command.player;

import com.github.euonmyoji.newhonor.inventory.HonorGUI;
import net.yeah.mungsoup.mung.command.SubCommand;
import org.bukkit.entity.Player;
//fixme: 怎么还有GUICommand的 解释下@(((
public class GUICommand {

    @SubCommand(
            command = "gui",
            description = "${newhonor.command.describe.list}",
            hover = "${newhonor.command.describe.list}"
    )
    public void execute(Player player) {
        new HonorGUI(player).open();
    }
}
