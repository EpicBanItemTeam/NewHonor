package com.github.euonmyoji.newhonor.data;

import com.github.euonmyoji.newhonor.NewHonor;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author MungSoup
 */
@Getter
public class Honor {
    private final ClickEvent.Action clickType;
    private final HoverEvent.Action hoverType;
    private final String clilckValue, hoverValue, text;
    private final List<String> displayTexts, suffixes;
    private final int[] intervalTicks;
    private final TextComponent tellraw;
    private final ItemStack icon;

    public Honor(String id) {
        Logger logger = Bukkit.getLogger();
        CommentedConfigurationNode config = NewHonor.honorConfig.cfg.getNode(id);
        String defaultValue = "[default]";
        text = color(config.getNode("value").getString(defaultValue));
        tellraw = new TextComponent(text);
        /* click event */
        {
            CommentedConfigurationNode clickNode = config.getNode("clickValue");
            clilckValue = color(clickNode.getNode("value").getString(defaultValue));
            String clickTypeString = clickNode.getNode("type").getString("suggest").toUpperCase();
            try {
                clickType = ClickEvent.Action.valueOf(clickTypeString);
                tellraw.setClickEvent(new ClickEvent(clickType, clilckValue));
            } catch (IllegalArgumentException e) {
                logger.log(Level.CONFIG, "Error about honor.conf (clickType may be wrong?)");
            }
        }
        /* hover event */
        {
            CommentedConfigurationNode hoverNode = config.getNode("hoverValue");
            hoverValue = color(hoverNode.getNode("value").getString(defaultValue));
            String hoverTypeString = hoverNode.getNode("type").getString("suggest").toUpperCase();
            try {
                hoverType = HoverEvent.Action.valueOf(hoverTypeString);
                tellraw.setHoverEvent(new HoverEvent(hoverType, new ComponentBuilder(hoverValue).create()));
            } catch (IllegalArgumentException e) {
                logger.log(Level.CONFIG, "Error about honor.conf (hoverType may be wrong?)");
            }
        }
    }

    private String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
