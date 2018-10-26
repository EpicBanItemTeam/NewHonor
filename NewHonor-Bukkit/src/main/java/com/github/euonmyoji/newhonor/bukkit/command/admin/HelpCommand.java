package com.github.euonmyoji.newhonor.bukkit.command.admin;

import com.github.euonmyoji.newhonor.bukkit.NewHonor;
import com.github.euonmyoji.newhonor.bukkit.mung.command.SubCommand;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

/**
 * @author MungSoup
 */
public class HelpCommand {

    @SubCommand(
            command = "Admin",
            description = "管理员命令",
            permission = "newhonor.admin"
    )
    public void execute(CommandSender sender) {
        String line = sender instanceof ConsoleCommandSender ? "§a========================================" : "§a§l－－－－－－－－－－－－－－－－－－－－";
        sender.sendMessage(line);
        sender.sendMessage("");
        for (Class clazz : NewHonor.adminCommandClasses) {
            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(SubCommand.class)) {
                    continue;
                }
                SubCommand commandArgs = method.getAnnotation(SubCommand.class);
                String argString = "".equals(commandArgs.args()) ? " " : " " + commandArgs.args() + " ";
                String text = String.format("§a%s%s§f§l-> §7%s", commandArgs.command(), argString, commandArgs.description());
                String click = String.format("/NewHonor %s%s%s", commandArgs.command(), argString, commandArgs.click());
                TextComponent tellraw = new TextComponent(text);
                tellraw.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, click));
                tellraw.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(commandArgs.hover()).create()));
                if (sender instanceof Player) {
                    sender.spigot().sendMessage(tellraw);
                    continue;
                }
                sender.sendMessage(tellraw.getText());
            }
        }
        sender.sendMessage("");
        sender.sendMessage(line);
    }
}
