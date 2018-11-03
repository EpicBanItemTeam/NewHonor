package com.github.euonmyoji.newhonor;

import com.github.euonmyoji.newhonor.api.configuration.PlayerConfig;
import com.github.euonmyoji.newhonor.command.admin.GiveCommand;
import com.github.euonmyoji.newhonor.command.admin.HelpCommand;
import com.github.euonmyoji.newhonor.command.admin.ReloadCommand;
import com.github.euonmyoji.newhonor.configuration.HonorConfig;
import com.github.euonmyoji.newhonor.configuration.LocalPlayerConfig;
import com.github.euonmyoji.newhonor.configuration.MainConfig;
import com.github.euonmyoji.newhonor.listener.OnJoin;
import com.github.euonmyoji.newhonor.manager.MysqlManager;
import com.google.common.collect.Maps;
import net.yeah.mungsoup.mung.command.CommandArg;
import net.yeah.mungsoup.mung.command.CommandRegisterer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author NewHonor authors
 */
public class NewHonor extends JavaPlugin {
    public static boolean isPEXEnable = false;
    public static NewHonor plugin;
    public static MainConfig mainConfig;
    public static String prefix = "§a[New§6Honor§a] §7 ";
    public static HonorConfig honorConfig;

    @Override
    public void onEnable() {
        /* 初始化变量 */
        plugin = this;
        isPEXEnable = Bukkit.getPluginManager().isPluginEnabled("PermissionsEx");

        /* 初始化配置 */
        try {
            Path langPath = getDataFolder().toPath().resolve("lang");
            Files.createDirectories(langPath);

            //c v 默认语言文件
            {
                final String cn = "zh_CN.lang";
                final String en = "en_US.lang";
                Path t;
                if (Files.notExists(t = langPath.resolve(cn))) {
                    Files.copy(getResource("assets/newhonor/lang/zh_CN.lang"), t);
                }
                if (Files.notExists(t = langPath.resolve(en))) {
                    Files.copy(getResource("assets/newhonor/lang/en_US.lang"), t);
                }
            }

            mainConfig = new MainConfig();
            honorConfig = new HonorConfig();
            MysqlManager.init(mainConfig.cfg.getNode("SQL-settings"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            PlayerConfig.registerPlayerConfig("local", LocalPlayerConfig.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        PlayerConfig.setDefaultConfigType(MysqlManager.enable ? "mysql" : "local");

        /* 注册命令 */
        new CommandArg(OfflinePlayer.class, ((commandSender, s) -> {
            Player onlinePlayer = Bukkit.getPlayerExact(s);
            if (onlinePlayer != null) {
                return onlinePlayer;
            }

            //如果玩家不在线再这样
            List<OfflinePlayer> list = Stream.of(Bukkit.getOfflinePlayers())
                    .filter(p -> p.getName().equals(s))
                    .collect(Collectors.toList());
            if (list.size() > 1) {
                commandSender.sendMessage(NewHonor.prefix + "found 2 or more users!, ");
            } else if (list.isEmpty()) {
                commandSender.sendMessage(NewHonor.prefix + "User " + s + " not found!");
                return null;
            }
            return list.get(0);
        }));
        CommandRegisterer registerer = new CommandRegisterer("NewHonor", prefix + "没有这个命令!");
        Map<String, Class[]> map = Maps.newHashMap();
        map.put("Admin", new Class[]{ReloadCommand.class, GiveCommand.class});
        registerer.register(new String[]{"Admin"}, map, ReloadCommand.class, HelpCommand.class, GiveCommand.class);

        /* 注册事件 */
        Bukkit.getPluginManager().registerEvents(new OnJoin(), this);

        /* 介绍 */
        Bukkit.getConsoleSender().sendMessage("[§aNew§6Honor§7] §a成功加载NewHonor BUKKIT版本!");
    }
}
