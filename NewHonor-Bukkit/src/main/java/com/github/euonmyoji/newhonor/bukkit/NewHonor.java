package com.github.euonmyoji.newhonor.bukkit;

import com.github.euonmyoji.newhonor.bukkit.command.admin.HelpCommand;
import com.github.euonmyoji.newhonor.bukkit.command.admin.ReloadCommand;
import com.github.euonmyoji.newhonor.bukkit.configuration.MainConfig;
import com.github.euonmyoji.newhonor.bukkit.mung.command.CommandManager;
import com.github.euonmyoji.newhonor.manager.LanguageManager;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


/**
 * @author NewHonor authors
 */
public class NewHonor extends JavaPlugin {
    public static boolean isPEXEnable = false;
    public static NewHonor instance;
    public static MainConfig mainConfig;
    public static Path langPath;
    public static String prefix = "§a[New§6Honor§a] §7 ";
    public static List<Class> adminCommandClasses = Lists.newArrayList();

    @Override
    public void onEnable() {
        /* 初始化变量 */
        instance = this;
        isPEXEnable = Bukkit.getPluginManager().isPluginEnabled("PermissionsEx");

        /* 初始化配置 */
        try {
            mainConfig = new MainConfig();
            langPath = Files.createDirectories(getDataFolder().toPath().resolve("Language"))
                    .resolve(mainConfig.getLanguage() + ".lang");
            if (Files.notExists(langPath)) {
                saveResource(String.format("Language/%s.lang", mainConfig.getLanguage()), false);
            }
            LanguageManager.reload(langPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* 注册命令 */
        NewHonor.adminCommandClasses.add(ReloadCommand.class);
        CommandManager.Args args = new CommandManager.Args(this, "NewHonor", ReloadCommand.class, HelpCommand.class);
        args.console = true;
        args.noCommandMsg = prefix + "不存在的命令!";
        args.noPermissionMsg = LanguageManager.getString("newhonor.listhonors.nopermission");
        CommandManager.register(args);

        /* 介绍 */
        Bukkit.getConsoleSender().sendMessage("[§aNew§6Honor§7] §a成功加载NewHonor BUKKIT版本!");
    }
}
