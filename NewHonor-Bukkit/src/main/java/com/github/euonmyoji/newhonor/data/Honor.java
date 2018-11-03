package com.github.euonmyoji.newhonor.data;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

//fixme: 这个类一点都不Data 纯符「纯粹のData(确信)」
/**
 * @author MungSoup
 */
public class Honor {
    private final String CLICK_VALUE_NODE = "clickValue";
    private CommentedConfigurationNode config;

    public Honor(CommentedConfigurationNode config, String id) {
        this.config = config.getNode(id);
    }

    public String getClickType() {
        return config.getNode(CLICK_VALUE_NODE, "type").getString("suggest");
    }

    public String getClickValue() {
        return config.getNode(CLICK_VALUE_NODE, "value").getString("value");
    }

    public List<String> getDisplayTexts() {
        try {
            //noinspection UnstableApiUsage
            return config.getNode("displayValue").getList(TypeToken.of(String.class), ArrayList::new);
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getHoverValue() {
        return config.getNode("hoverValue", "value").getString("value").replace("&", "§");
    }

    public int[] getIntervalTicks() {
        CommentedConfigurationNode node = config.getNode("intervalTicks");
        String intervalTickString = node.getString("1");
        String strip = ",";
        if (intervalTickString.contains(strip)) {
            String[] tickStrings = intervalTickString.split(",");
            int[] ints = new int[tickStrings.length];
            for (int i = 0; i < tickStrings.length; i++) {
                ints[i] = Integer.valueOf(tickStrings[i]);
            }
            return ints;
        }
        return new int[]{Integer.valueOf(intervalTickString)};
    }

    public ItemStack getIcon() {
        CommentedConfigurationNode node = config.getNode("item-value");
        int amount = node.getNode("Count").getInt(1);
        Material type = Material.getMaterial(node.getNode("ItemType").getString("minecraft:ice")
                .replace("minecraft:", "").toUpperCase());
        int data = node.getNode("UnsafeDamage").getInt(0);
        String displayName = node.getNode("UnsafeData", "display", "Name").getString("Name").replace("&", "§");
        List<String> lore = null;
        try {
            //noinspection UnstableApiUsage
            lore = node.getNode("UnsafeData", "display", "Lore").getList(TypeToken.of(String.class), ArrayList::new);
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
        ItemStack item = new ItemStack(type, amount, (short) data);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public List<String> getSuffixes() {
        try {
            //noinspection UnstableApiUsage
            return config.getNode("suffixValue").getList(TypeToken.of(String.class), ArrayList::new);
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
        return Lists.newArrayList();
    }

    public String getText() {
        return config.getNode("value").getString("value").replace("&", "§");
    }
}
