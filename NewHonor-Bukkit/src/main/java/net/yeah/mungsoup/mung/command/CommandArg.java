package net.yeah.mungsoup.mung.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author MungSoup & yinyangshi
 */
class CommandArg {
    static Map<Class<?>, BiFunction<CommandSender, String, ?>> argMap = new HashMap<>(5);

    static {
        new CommandArg(Integer.class, ((sender, arg) -> {
            try {
                return Integer.parseInt(arg);
            } catch (NumberFormatException e) {
                sender.sendMessage("§c不是数字");
                return null;
            }
        }));
        new CommandArg(String.class, ((sender, arg) -> arg));
        new CommandArg(Player.class, ((sender, arg) -> {
            Player player = Bukkit.getPlayer(arg);
            if (player == null) {
                sender.sendMessage("玩家不存在或不在线");
            }
            return player;
        }));
    }

    public <T> CommandArg(Class<T> clazz, BiFunction<CommandSender, String, T> value) {
        argMap.put(clazz, value);
    }
}
