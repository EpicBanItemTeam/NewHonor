package com.github.euonmyoji.newhonor.bukkit.mung.command;

import com.github.euonmyoji.newhonor.bukkit.NewHonor;
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


/**
 * @author MungSoup
 */
public class CommandManager {

    public static void register(Args registerArgs) {
        register(registerArgs, null, null);
    }

    public static void register(Args registerArgs, String[] helpCommands, List<Class[]> helpCommandClassList) {
        registerArgs.plugin.getServer().getPluginCommand(registerArgs.command).setExecutor((sender, command, label, args) -> {
            if (!registerArgs.console && !(sender instanceof Player)) {
                sender.sendMessage(registerArgs.consoleExecuteMsg);
                return false;
            }
            if (args.length == 0) {
                return help(registerArgs, label, sender, helpCommands);
            }
            for (Class clazz : registerArgs.commandClasses) {
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
                    /* 判断权限 */
                    if (!"".equals(commandArgs.permission()) && isNoPermission(sender, commandArgs.permission())) {
                        sender.sendMessage(registerArgs.noPermissionMsg);
                        continue;
                    }
                    /* 解析方法参数 */
                    List<Object> objectList = Lists.newArrayList();
                    objectList.add(registerArgs.console ? sender : (Player) sender);
                    int i = 0;
                    for (Class<?> parameterType : method.getParameterTypes()) {
                        if (i == 0) {
                            i += count;
                            continue;
                        }
                        CommandArg.IValue value;
                        if (!CommandArg.commandTable.contains(registerArgs.plugin, parameterType)) {
                            if (!CommandArg.commandTable.contains(NewHonor.instance, parameterType)) {
                                throw new NullPointerException("获取不到" + parameterType.getSimpleName() + "的参数值");
                            }
                            value = CommandArg.commandTable.get(NewHonor.instance, parameterType);
                        } else {
                            value = CommandArg.commandTable.get(registerArgs.plugin, parameterType);
                        }
                        if (value.getValue(sender, args[i]) == null) {
                            return false;
                        }
                        objectList.add(value.getValue(sender, args[i]));
                        i += 1;
                    }
                    try {
                        method.invoke(clazz.newInstance(), objectList.toArray());
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            sender.sendMessage(registerArgs.noCommandMsg);
            return false;
        });
    }

    private static boolean isNoPermission(CommandSender sender, String permission) {
        boolean hasPermissionInPEX = sender instanceof ConsoleCommandSender || (NewHonor.isPEXEnable && PermissionsEx.getUser((Player) sender).has(permission));
        boolean hasPermissionInBukkit = sender.hasPermission(permission);
        return !hasPermissionInBukkit && !hasPermissionInPEX;
    }

    private static boolean help(Args args, String label, CommandSender sender, String[] helpCommands) {
        String line = sender instanceof ConsoleCommandSender ? "§a========================================" : "§a§l－－－－－－－－－－－－－－－－－－－－";
        sender.sendMessage(line);
        sender.sendMessage("");
        for (Class clazz : args.commandClasses) {
            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(SubCommand.class)) {
                    continue;
                }
                SubCommand commandArgs = method.getAnnotation(SubCommand.class);
                if (!"".equals(commandArgs.permission()) && isNoPermission(sender, commandArgs.permission())) {
                    sender.sendMessage(LanguageManager.getString("newhonor.listhonors.nopermission"));
                    continue;
                }
                boolean isHelpCommand = false;
                if (helpCommands != null) {
                    for (String helpCommand : helpCommands) {
                        if (commandArgs.command().startsWith(helpCommand + " ")) {
                            isHelpCommand = true;
                            break;
                        }
                    }
                }
                if (isHelpCommand) {
                    continue;
                }
                String argString = "".equals(commandArgs.args()) ? " " : " " + commandArgs.args() + " ";
                String text = String.format("§a%s%s§f§l-> §7%s", commandArgs.command(), argString, commandArgs.description());
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

    public static class Args {
        public Plugin plugin;
        public String command, noPermissionMsg, consoleExecuteMsg, noCommandMsg;
        public boolean console;
        Class[] commandClasses;

        public Args(Plugin plugin, String command, Class... commandClasses) {
            this.plugin = plugin;
            this.command = command;
            this.commandClasses = commandClasses;
            this.console = false;
            final String prefix = "§a[绿豆提示] ";
            this.noPermissionMsg = prefix + "§c你没有权限!";
            this.consoleExecuteMsg = prefix + "§c控制台无法执行这个命令!";
            this.noCommandMsg = prefix + "§c没有这个指令!";
        }

    }
}
