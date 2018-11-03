package com.github.euonmyoji.newhonor.inventory;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.configuration.PlayerConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;

public class HonorGUI implements Listener {
    private Inventory inventory;
    private Player player;

    public HonorGUI() {
    }

    public HonorGUI(Player player) {
        this.player = player;
        inventory = Bukkit.createInventory(null, 54, player.getName() + "'s honor list");
        update(inventory);
    }

    @EventHandler
    public void on(InventoryClickEvent e) {
        //fixme 这什么破代码啊 :D (雾)
        if (e.getWhoClicked() instanceof Player && e.getClickedInventory() != null
                && e.getClickedInventory().getTitle().contains("'s honor list")) {
            Player player = (Player) e.getWhoClicked();
            if (e.getCurrentItem() != null) {
                e.setCancelled(true);
                try {
                    PlayerConfig playerConfig = PlayerConfig.get(player.getUniqueId());
                    playerConfig.getOwnHonors().ifPresent(strings -> strings.forEach(s -> {
                        if (e.getCurrentItem().isSimilar(NewHonor.honorConfig.getHonor(s).getIcon())) {
                            try {
                                //fixme: 没有通过命令修改使用头衔 不可以 这不命令
                                if (playerConfig.setUseHonor(s)) {
                                    player.sendMessage("§a成功设置头衔");
                                    player.closeInventory();
                                }
                            } catch (SQLException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public void open() {
        player.openInventory(this.inventory);
    }

    private void update(Inventory inventory) {
        try {
            PlayerConfig playerConfig = PlayerConfig.get(player.getUniqueId());
            playerConfig.getOwnHonors().ifPresent(strings -> strings.forEach(s -> inventory.addItem(NewHonor.honorConfig.getHonor(s).getIcon())));
            ItemStack ornament = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 13);
            inventory.setItem(48, ornament);
            inventory.setItem(50, ornament);
            inventory.setItem(49, NewHonor.honorConfig.getHonor(playerConfig.getUsingHonorID()).getIcon());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
