package net.yeah.mungsoup.mung.command;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.manager.LanguageManager;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author MungSoup
 */
public class CommandRegisterer {
    private Plugin plugin;
    private String command;
    private String noCommandMsg;

    public CommandRegisterer(Plugin plugin, String command, String noCommandMsg) {
        this.plugin = plugin;
        this.command = command;
        this.noCommandMsg = noCommandMsg;
    }

    private boolean isNoPermission(CommandSender sender, String permission) {
        boolean hasPermissionInPEX = sender instanceof ConsoleCommandSender || (NewHonor.isPEXEnable && PermissionsEx.getUser((Player) sender).has(permission));
        boolean hasPermissionInBukkit = sender.hasPermission(permission);
        return !hasPermissionInBukkit && !hasPermissionInPEX;
    }

    private boolean help(Class[] commandClasses, String label, CommandSender sender, String[] helpCommands) {
        String line = sender instanceof ConsoleCommandSender ? "§a========================================" : "§a§l－－－－－－－－－－－－－－－－－－－－";
        sender.sendMessage(line);
        sender.sendMessage("");
        for (Class clazz : commandClasses) {
            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(SubCommand.class)) {
                    continue;
                }
                SubCommand commandArgs = method.getAnnotation(SubCommand.class);
                if (!"".equals(commandArgs.permission()) && isNoPermission(sender, commandArgs.permission())) {
                    sender.sendMessage(LanguageManager.getString("newhonor.listhonors.nopermission"));
                    continue;
                }
                if (isHelpCommand(helpCommands, commandArgs.command())) {
                    continue;
                }
                String argString = "".equals(commandArgs.args()) ? " " : " " + commandArgs.args() + " ";
                String language = LanguageManager.getString(commandArgs.description().replaceFirst("\\$\\{", "")
                        .replaceAll("[}]$", "")).replaceAll("&", "§");
                String description = commandArgs.description().matches("\\$\\{.*}") ? language : commandArgs.description();
                String text = String.format("§a%s%s§f§l-> §7%s", commandArgs.command(), argString, description);
                String click = String.format("/%s %s%s%s", label, commandArgs.command(), argString, commandArgs.click());
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
        return true;
    }

    public void setNoCommandMsg(String noCommandMsg) {
        this.noCommandMsg = noCommandMsg;
    }

    public void register(Class... commandClasses) {
        register(null, null, commandClasses);
    }

    public void register(String[] helpCommands, Map<String, Class[]> helpCommandClassMap, Class... commandClasses) {
        plugin.getServer().getPluginCommand(command).setExecutor((sender, cmd, label, args) -> {
            if (args.length == 0) {
                return help(commandClasses, label, sender, helpCommands);
            }
            for (Class clazz : commandClasses) {
                for (Method method : clazz.getMethods()) {
                    if (!method.isAnnotationPresent(SubCommand.class)) {
                        continue;
                    }
                    SubCommand commandArgs = method.getAnnotation(SubCommand.class);
                    String[] subCommandArgs = commandArgs.command().split(" ");
                    int subCommandLength = subCommandArgs.length;
                    int length = "".equals(commandArgs.args()) ? 0 : commandArgs.args().split(" ").length;
                    int count = 0;
                    /* 判断子命令 */
                    if (subCommandLength != args.length - length) {
                        continue;
                    }
                    if (isHelpCommand(helpCommands, commandArgs.command())) {
                        continue;
                    }
                    for (int i = 0; i < subCommandArgs.length; i++) {
                        String subCommand = subCommandArgs[i];
                        if (args[i].equalsIgnoreCase(subCommand)) {
                            count += 1;
                        }
                    }
                    if (count != subCommandArgs.length) {
                        continue;
                    }
                    /* 判断命令参数 */
                    if (args.length - count < length) {
                        continue;
                    }
                    /* 判断方法里的参数与命令参数 */
                    if (method.getParameterTypes().length - 1 != length) {
                        throw new IllegalArgumentException("命令执行方法里的参数与命令参数不匹配!");
                    }
                    if (!commandArgs.console() && sender instanceof ConsoleCommandSender) {
                        sender.sendMessage(commandArgs.consoleExecuteMsg());
                        return false;
                    }
                    /* 判断权限 */
                    if (!"".equals(commandArgs.permission()) && isNoPermission(sender, commandArgs.permission())) {
                        sender.sendMessage(commandArgs.noPermissionMsg());
                        return false;
                    }
                    if (isHelpMainCommand(helpCommands, commandArgs.command())) {
                        return help(helpCommandClassMap.get(commandArgs.command()), label, sender, null);
                    }
                    /* 解析方法参数 */
                    try {
                        method.invoke(clazz.newInstance(), checkArg(sender, commandArgs, args, count, method));
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            sender.sendMessage(noCommandMsg);
            return false;
        });
    }

    private boolean isHelpMainCommand(String[] helpCommands, String command) {
        if (helpCommands != null) {
            for (String helpCommand : helpCommands) {
                if (command.equalsIgnoreCase(helpCommand)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isHelpCommand(String[] helpCommands, String command) {
        if (helpCommands != null) {
            for (String helpCommand : helpCommands) {
                if (command.startsWith(helpCommand + " ")) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Object> checkArg(CommandSender sender, SubCommand commandArgs, String[] args, int count, Method method) {
        List<Object> objectList = Lists.newArrayList();
        objectList.add(commandArgs.console() && sender instanceof ConsoleCommandSender ? sender : (Player) sender);
        int i = 0;
        for (Class<?> parameterType : method.getParameterTypes()) {
            if (i == 0) {
                i += count;
                continue;
            }
            CommandArg.IValue value;
            if (!CommandArg.commandTable.contains(plugin, parameterType)) {
                if (!CommandArg.commandTable.contains(NewHonor.instance, parameterType)) {
                    throw new NullPointerException("获取不到" + parameterType.getSimpleName() + "的参数值");
                }
                value = CommandArg.commandTable.get(NewHonor.instance, parameterType);
            } else {
                value = CommandArg.commandTable.get(plugin, parameterType);
            }
            if (value.getValue(sender, args[i]) == null) {
                return objectList;
            }
            objectList.add(value.getValue(sender, args[i]));
            i += 1;
        }
        return objectList;
    }
}
