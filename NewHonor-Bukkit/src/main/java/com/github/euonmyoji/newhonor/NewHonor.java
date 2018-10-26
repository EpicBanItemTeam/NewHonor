package com.github.euonmyoji.newhonor;

import com.github.euonmyoji.newhonor.command.admin.HelpCommand;
import com.github.euonmyoji.newhonor.command.admin.ReloadCommand;
import com.github.euonmyoji.newhonor.configuration.MainConfig;
import com.github.euonmyoji.newhonor.manager.LanguageManager;
import com.github.euonmyoji.newhonor.mung.command.CommandRegisterer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author NewHonor authors
 */
public class NewHonor extends JavaPlugin {
    public static boolean isPEXEnable = false;
    public static NewHonor instance;
    public static MainConfig mainConfig;
    public static Path langPath;
    public static String prefix = "§a[New§6Honor§a] §7 ";
    public static CommandRegisterer registerer;

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
        registerer = new CommandRegisterer(this, "NewHonor", prefix + "没有这个命令!");
        registerer.register(ReloadCommand.class, HelpCommand.class);

        /* 介绍 */
        Bukkit.getConsoleSender().sendMessage("[§aNew§6Honor§7] §a成功加载NewHonor BUKKIT版本!");
    }
}
