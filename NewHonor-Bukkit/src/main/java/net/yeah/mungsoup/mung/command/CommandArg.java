package net.yeah.mungsoup.mung.command;

import com.github.euonmyoji.newhonor.NewHonor;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * @author MungSoup
 */
class CommandArg {
    static Table<Plugin, Class, IValue> commandTable = HashBasedTable.create();

    static {
        new CommandArg(NewHonor.instance, int.class, ((sender, arg) -> {
            try {
                return Integer.parseInt(arg);
            } catch (NumberFormatException e) {
                sender.sendMessage("§c不是数字");
                return null;
            }
        }));

        new CommandArg(NewHonor.instance, Integer.class, ((sender, arg) -> {
            try {
                return Integer.parseInt(arg);
            } catch (NumberFormatException e) {
                sender.sendMessage("§c不是数字");
                return null;
            }
        }));
        new CommandArg(NewHonor.instance, String.class, ((sender, arg) -> arg));
        new CommandArg(NewHonor.instance, Player.class, ((sender, arg) -> {
            Player player = Bukkit.getPlayer(arg);
            if (player == null) {
                sender.sendMessage("玩家不存在或不在线");
                return null;
            }
            return Bukkit.getPlayer(arg);
        }));
    }

    private CommandArg(Plugin plugin, Class clazz, IValue value) {
        commandTable.put(plugin, clazz, value);

    }

    public interface IValue {

        /**
         * 获取参数对应的值
         *
         * @param sender - 命令触发者
         * @param arg    - 对应参数
         * @return 参数对应的值
         */
        Object getValue(CommandSender sender, String arg);
    }
}
