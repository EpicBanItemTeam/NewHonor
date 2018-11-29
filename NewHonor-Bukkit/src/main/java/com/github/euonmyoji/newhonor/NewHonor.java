package com.github.euonmyoji.newhonor;

import com.github.euonmyoji.newhonor.api.configuration.PlayerConfig;
import com.github.euonmyoji.newhonor.command.admin.AdminCommand;
import com.github.euonmyoji.newhonor.command.admin.GiveCommand;
import com.github.euonmyoji.newhonor.command.admin.HelpCommand;
import com.github.euonmyoji.newhonor.command.admin.ReloadCommand;
import com.github.euonmyoji.newhonor.command.player.GUICommand;
import com.github.euonmyoji.newhonor.configuration.HonorConfig;
import com.github.euonmyoji.newhonor.configuration.LocalPlayerConfig;
import com.github.euonmyoji.newhonor.configuration.MainConfig;
import com.github.euonmyoji.newhonor.data.Honor;
import com.github.euonmyoji.newhonor.hook.PAPIHook;
import com.github.euonmyoji.newhonor.inventory.HonorGUI;
import com.github.euonmyoji.newhonor.manager.MysqlManager;
import com.github.euonmyoji.newhonor.serializable.ItemSerializable;
import com.github.euonmyoji.newhonor.task.DisplayHonorTask;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import me.clip.placeholderapi.PlaceholderAPI;
import net.yeah.mungsoup.mung.command.CommandArg;
import net.yeah.mungsoup.mung.command.CommandRegisterer;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    public static Map<UUID, Honor> honorCacheMap = new HashMap<>();
    private static Logger logger;

    @Override
    public void onEnable() {
        /* 初始化变量 */
        logger = getLogger();
        plugin = this;
        isPEXEnable = Bukkit.getPluginManager().isPluginEnabled("PermissionsEx");
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(ItemStack.class), new ItemSerializable());
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
        registerer.register(new String[]{"Admin"}, map, ReloadCommand.class,
                HelpCommand.class,
                GiveCommand.class,
                GUICommand.class,
                AdminCommand.class);

        /* 注册事件 */
        Bukkit.getPluginManager().registerEvents(new HonorGUI(), this);
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            PlaceholderAPI.registerPlaceholderHook("newhonor", new PAPIHook());
        }


        /* 介绍 */
        Bukkit.getConsoleSender().sendMessage("[§aNew§6Honor§7] §a成功加载NewHonor BUKKIT版本!");
    }

    public void updateCache(PlayerConfig playerConfig) {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                playerConfig.checkUsingHonor();
                if (playerConfig.isUseHonor()) {
                    Optional<Honor> honor = Optional.of(honorConfig.getHonor(playerConfig.getUsingHonorID()));
                    honor.ifPresent(data -> {
                        honorCacheMap.put(playerConfig.getUUID(), data);
                        try {
                            DisplayHonorTask.init(Bukkit.getPlayer(playerConfig.getUUID()));
                        } catch (Exception e) {
                            logger.log(Level.WARNING, "error about task!", e);
                        }
                    });
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "error about data!", e);
            }
        });
    }

    @Override
    public void onDisable() {
        DisplayHonorTask.tasks.forEach(DisplayHonorTask::cancel);
    }
}
