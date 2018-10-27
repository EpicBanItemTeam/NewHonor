package com.github.euonmyoji.newhonor;

import com.github.euonmyoji.newhonor.api.configuration.PlayerConfig;
import com.github.euonmyoji.newhonor.command.admin.HelpCommand;
import com.github.euonmyoji.newhonor.command.admin.ReloadCommand;
import com.github.euonmyoji.newhonor.configuration.HonorConfig;
import com.github.euonmyoji.newhonor.configuration.MainConfig;
import com.github.euonmyoji.newhonor.manager.LanguageManager;
import com.google.common.collect.Maps;
import net.yeah.mungsoup.mung.command.CommandRegisterer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

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
    public static HonorConfig honorConfig;

    @Override
    public void onEnable() {
        /* 初始化变量 */
        instance = this;
        isPEXEnable = Bukkit.getPluginManager().isPluginEnabled("PermissionsEx");

        /* 初始化配置 */
        try {
            langPath = getDataFolder().toPath().resolve("Language");
            String language = "zh_CN.lang";
            if (Files.notExists(langPath) && Files.notExists(langPath.resolve(language))) {
                Files.createDirectories(langPath);
                langPath = langPath.resolve(language);
                saveResource("Language/zh_CN.lang", false);
                LanguageManager.reload(langPath);
                mainConfig = new MainConfig();
            } else {
                mainConfig = new MainConfig();
                langPath = langPath.resolve(mainConfig.getLanguage() + ".lang");
                if (Files.notExists(langPath)) {
                    saveResource(String.format("Language/%s.lang", mainConfig.getLanguage()), false);
                }
                LanguageManager.reload(langPath);
            }
            honorConfig = new HonorConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
        PlayerConfig.setDefaultConfigType(mainConfig.isMySQLEnable() ? "mysql" : "local");

        /* 注册命令 */
        registerer = new CommandRegisterer(this, "NewHonor", prefix + "没有这个命令!");
        Map<String, Class[]> map = Maps.newHashMap();
        map.put("Admin", new Class[]{ReloadCommand.class});
        registerer.register(new String[]{"Admin"}, map, ReloadCommand.class, HelpCommand.class);

        /* 介绍 */
        Bukkit.getConsoleSender().sendMessage("[§aNew§6Honor§7] §a成功加载NewHonor BUKKIT版本!");
    }
}
