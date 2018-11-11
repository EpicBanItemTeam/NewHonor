package com.github.euonmyoji.newhonor.command.admin;

import com.github.euonmyoji.newhonor.NewHonor;
import net.yeah.mungsoup.mung.command.SubCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import static com.github.euonmyoji.newhonor.NewHonor.honorConfig;
import static com.github.euonmyoji.newhonor.manager.LanguageManager.langBuilder;

/**
 * @author yinyangshi
 */
public class AdminCommand {
    @SubCommand(
            command = "admin add",
            args = "<头衔id> <头衔>",
            description = "${newhonor.command.describe.admin.add}",
            permission = "newhonor.admin",
            hover = "§b就是创建头衔啦!",
            console = true
    )
    public void add(CommandSender sender, OfflinePlayer player, String id, String value) {
        try {
            if (honorConfig.notExist(id)) {
                if (honorConfig.addHonor(id, value)) {
                    NewHonor.plugin.getLogger().info(sender.getName() + " added an honor:" + id);
                    sender.sendMessage("[NewHonor]add an honor successful");
                } else {
                    sender.sendMessage("[NewHonor]add an honor failed!");
                }
            } else {
                sender.sendMessage(langBuilder("newhonor.command.arg.error.honorpresent",
                        "The honorid is present")
                        .replaceHonorid(id).build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubCommand(
            command = "admin set",
            args = "<头衔id> <头衔>",
            description = "${newhonor.command.describe.admin.set}",
            permission = "newhonor.admin",
            hover = "§b就是设置头衔啦!",
            console = true
    )
    public void set(CommandSender sender, OfflinePlayer player, String id, String value) {
        try {
            if (honorConfig.setHonor(id, value)) {
                NewHonor.plugin.getLogger().info(sender.getName() + " set an honor:" + id);
                sender.sendMessage("[NewHonor]set an honor successful");
            } else {
                sender.sendMessage("[NewHonor]set an honor failed!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
