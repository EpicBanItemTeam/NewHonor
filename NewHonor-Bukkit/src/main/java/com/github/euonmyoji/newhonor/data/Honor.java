package com.github.euonmyoji.newhonor.data;

import com.google.common.reflect.TypeToken;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author MungSoup
 */
public class Honor {
    private ClickEvent.Action clickType;
    private HoverEvent.Action hoverType;
    private String clickValue, hoverValue, text;
    private List<String> displayTexts, suffixes;
    private int intervalTick;
    private TextComponent tellraw;
    private ItemStack icon;

    public Honor(CommentedConfigurationNode cfg, String id) {
        Logger logger = Bukkit.getLogger();
        CommentedConfigurationNode config = cfg.getNode(id);
        String defaultValue = "[default]";
        text = color(config.getNode("value").getString(defaultValue));
        tellraw = new TextComponent(text);
        /* click event */
        {
            CommentedConfigurationNode clickNode = config.getNode("clickValue");
            clickValue = color(clickNode.getNode("value").getString(defaultValue));
            String clickTypeString = clickNode.getNode("type").getString("suggest").toUpperCase();
            try {
                clickType = ClickEvent.Action.valueOf(clickTypeString);
                tellraw.setClickEvent(new ClickEvent(clickType, clickValue));
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
        try {
            displayTexts = config.getNode("displayValue").getList(TypeToken.of(String.class), ArrayList::new);
            suffixes = config.getNode("suffixValue").getList(TypeToken.of(String.class), ArrayList::new);
            intervalTick = config.getNode("intervalTicks").getInt(1);
            icon = config.getNode("item-value").getValue(TypeToken.of(ItemStack.class), new ItemStack(Material.NAME_TAG, 1));
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    public ClickEvent.Action getClickType() {
        return this.clickType;
    }

    public HoverEvent.Action getHoverType() {
        return this.hoverType;
    }

    public String getClickValue() {
        return this.clickValue;
    }

    public String getHoverValue() {
        return this.hoverValue;
    }

    public String getText() {
        return this.text;
    }

    public List<String> getDisplayTexts() {
        return this.displayTexts;
    }

    public List<String> getSuffixes() {
        return this.suffixes;
    }

    public int getIntervalTick() {
        return this.intervalTick;
    }

    public TextComponent getTellraw() {
        return this.tellraw;
    }

    public ItemStack getIcon() {
        return this.icon;
    }

    private String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

}
