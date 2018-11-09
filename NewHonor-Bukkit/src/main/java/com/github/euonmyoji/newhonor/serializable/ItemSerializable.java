package com.github.euonmyoji.newhonor.serializable;

import com.github.euonmyoji.newhonor.NewHonor;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * @author NewHonor authors
 */
public class ItemSerializable implements TypeSerializer<ItemStack> {

    @Override
    public ItemStack deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        if (type != TypeToken.of(ItemStack.class)) {
            return null;
        }
        ConfigurationNode nameNode = value.getNode("Name"), loreNode = value.getNode("Lore");
        Material material = Material.getMaterial(value.getNode("Type").getString());
        int amount = value.getNode("Count").getInt(1);
        int durability = value.getNode("Durability").getInt(0);
        ItemStack item = new ItemStack(material, amount, (short) durability);
        ItemMeta meta = item.getItemMeta();
        if (!nameNode.isVirtual()) {
            meta.setDisplayName(nameNode.getString());
        }
        if (!loreNode.isVirtual()) {
            meta.setLore(loreNode.getList(TypeToken.of(String.class)));
        }
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void serialize(TypeToken<?> type, ItemStack item, ConfigurationNode value) {
        if (!TypeToken.of(ItemStack.class).equals(type)) {
            NewHonor.plugin.getLogger().info("未知type进行序列化" + type + ",raw:" + type.getRawType());
        }
        String material = item.getType().toString();
        int amount = item.getAmount();
        int durability = item.getDurability();
        value.getNode("ItemType").setValue("minecraft:" + material);
        if (amount != 1) {
            value.getNode("Count").setValue(amount);
        }
        if (durability != 0) {
            value.getNode("UnsafeDamage").setValue(durability);
        }
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            String displayName = meta.getDisplayName();
            List<String> lore = meta.getLore();
            value.getNode("UnsafeData", "display", "Name").setValue(displayName);
            value.getNode("UnsafeData", "display", "Lore").setValue(lore);
        }
    }
}
