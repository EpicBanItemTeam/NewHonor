package com.github.euonmyoji.newhonor.command;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.configuration.PlayerConfig;
import com.github.euonmyoji.newhonor.configuration.HonorConfig;
import com.github.euonmyoji.newhonor.api.data.HonorData;
import com.github.euonmyoji.newhonor.enums.ListHonorStyle;
import com.github.euonmyoji.newhonor.manager.LanguageManager;
import com.github.euonmyoji.newhonor.manager.SpongeLanguageManager;
import com.github.euonmyoji.newhonor.util.Util;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.euonmyoji.newhonor.command.HonorCommand.ADMIN_PERMISSION;
import static com.github.euonmyoji.newhonor.manager.LanguageManager.langBuilder;
import static com.github.euonmyoji.newhonor.manager.SpongeLanguageManager.getText;
import static org.spongepowered.api.text.Text.of;
import static org.spongepowered.api.text.action.TextActions.runCommand;
import static org.spongepowered.api.text.action.TextActions.showText;

/**
 * @author yinyangshi
 */
class ListHonorCommand {
    private static HashMap<String, LocalDateTime> listCD = new HashMap<>();

    @Nonnull
    static CommandResult execute(CommandSource src, CommandContext args) {
        boolean free = !listCD.containsKey(src.getName()) || listCD.get(src.getName()).plusSeconds(10).isBefore(LocalDateTime.now())
                || src.hasPermission(ADMIN_PERMISSION);
        if (!free) {
            src.sendMessage(of("[NewHonor]You should wait a moment to use this command again."));
            return CommandResult.empty();
        }
        listCD.put(src.getName(), LocalDateTime.now());
        Optional<User> optionalUser = args.getOne(of("user"));
        boolean typedUser = optionalUser.isPresent();
        User user = typedUser ? optionalUser.get()
                : src instanceof User ? (User) src : null;
        boolean isSelf = src.getName().equals(user == null ? null : user.getName());
        boolean permissionPass = isSelf || src.hasPermission(ADMIN_PERMISSION);
        boolean execute = typedUser ? permissionPass : user != null;
        if (execute) {
            //async
            Task.builder().execute(() -> {
                try {
                    PlayerConfig pd = PlayerConfig.get(user);
                    pd.checkPermission();
                    pd.checkUsingHonor();
                    Optional<List<String>> honors = pd.getOwnHonors();
                    if (honors.isPresent()) {
                        if (honors.get().isEmpty()) {
                            src.sendMessage(Util.toText(langBuilder("newhonor.listhonors.empty").replaceName(user.getName()).build()));
                            return;
                        }
                        if (src instanceof Player && (isSelf ? pd.getListHonorStyle()
                                : PlayerConfig.get(((User) src)).getListHonorStyle()) == ListHonorStyle.ITEM) {
                            HonorData using = pd.getUsingHonorValue();
                            Task.builder().execute(() -> {
                                List<HonorData> dataList = honors.get().stream().map(HonorConfig::getHonorData)
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toList());
                                ((Player) src).openInventory(getHonorsInv(((Player) src), dataList, using, 1, isSelf, user.getName()));
                            }).submit(NewHonor.plugin);
                        } else {
                            listHonorByText(src, pd, user, honors.get(), isSelf);
                        }
                        Task.builder().async().name("NewHonor - check" + user.getName() + "has honors")
                                .execute(() -> honors.get().forEach(s -> {
                                    if (HonorConfig.isVirtual(s)) {
                                        try {
                                            pd.takeHonor(s);
                                        } catch (SQLException e) {
                                            src.sendMessage(getText("[NewHonor] error!"));
                                            e.printStackTrace();
                                        }
                                    }
                                })).submit(NewHonor.plugin);
                    } else {
                        src.sendMessage(of("unknown error"));
                    }
                } catch (Exception e) {
                    src.sendMessage(getText("[NewHonor] get data error!"));
                    e.printStackTrace();
                }
            }).async().name("newhonor - List Player" + user.getName() + " Honors").submit(NewHonor.plugin);
            return CommandResult.success();
        } else {
            if (user == null) {
                src.sendMessage(Util.toText(LanguageManager.getString("newhonor.listhonors.needuser"
                        , "[NewHonor] you should type a user!")));
            } else {
                src.sendMessage(getText("newhonor.listhonors.nopermission"));
            }
        }
        return CommandResult.empty();
    }

    private static final ItemStack GLASS = ItemStack.builder().itemType(ItemTypes.GLASS_PANE).add(Keys.DISPLAY_NAME, Text.of("")).build();

    private static void listHonorByText(CommandSource src, PlayerConfig pd, User user, List<String> honors, boolean isSelf) throws SQLException {
        PaginationList.Builder builder = PaginationList.builder()
                .title(Util.toText(langBuilder("newhonor.listhonors.title").replace("%ownername%", user.getName()).build())).padding(of("-"));
        String usingID = pd.getUsingHonorID();
        Optional.ofNullable(HonorConfig.getHonorData(usingID))
                .ifPresent(data -> builder.header(Util.toText(langBuilder("newhonor.listhonors.header")
                        .replace("%ownername%", user.getName())
                        .replaceHonor(data.getStrValue())
                        .replace("%effectsID%", HonorConfig.getEffectsID(usingID).orElse("null"))
                        .build())));
        List<Text> texts = honors.stream().map(id -> Optional.ofNullable(HonorConfig.getHonorData(id)).map(data -> Text.builder()
                //显示头衔 药水效果组
                .append(Util.toText(langBuilder("newhonor.listhonors.contexts")
                        .replaceHonorid(id)
                        .replaceHonor(data.getStrValue())
                        .replace("%effectsID%", HonorConfig.getEffectsID(id).orElse("null"))
                        .build()))
                .onHover(isSelf ? showText(Util.toText(langBuilder("newhonor.listhonors.clickuse")
                        .replaceHonor(data.getStrValue())
                        .replaceHonorid(id)
                        .build())) : null)
                .onClick(isSelf ? runCommand("/honor use " + id) : null)
                .build()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        builder.contents(texts).build().sendTo(src);
    }

    private static Inventory getHonorsInv(Player player, List<HonorData> list, HonorData using, int page, boolean isSelf, String name) {
        final int onePage = 5 * 9;
        try (Timing timing = Timings.of(NewHonor.plugin, "ShowHonorsInvTime")) {
            timing.startTimingIfSync();
            ItemStack[] previous = new ItemStack[1];
            ItemStack[] next = new ItemStack[1];
            HashMap<String, String> map = new HashMap<>(list.size() - (page - 1) * onePage);
            Inventory.Builder builder = Inventory.builder().of(InventoryArchetypes.DOUBLE_CHEST)
                    .property(InventoryTitle.PROPERTY_NAME, new InventoryTitle(Util.toText(langBuilder("newhonor.listhonors.invtitle")
                            .replaceName(name)
                            .replace("%n%", Integer.toString(list.size()))
                            .replace("%page%", Integer.toString(page))
                            .build())))
                    .listener(InteractInventoryEvent.class, event -> {
                        if (!(event instanceof InteractInventoryEvent.Open
                                || event instanceof InteractInventoryEvent.Close)) {
                            event.setCancelled(true);
                        }
                        if (event instanceof ClickInventoryEvent.Primary) {
                            ClickInventoryEvent.Primary eve = ((ClickInventoryEvent.Primary) event);
                            ItemStack item = eve.getCursorTransaction().getFinal().createStack();
                            if (item.getType() != ItemTypes.AIR) {
                                String id = map.get(Util.toStr(item.get(Keys.DISPLAY_NAME).orElse(Text.of(""))));
                                if (id != null && isSelf) {
                                    Sponge.getCommandManager().process(player, "honor use " + id);
                                    Task.builder().execute(player::closeInventory).submit(NewHonor.plugin);
                                } else if (page > 1 && previous[0] != null && item.equalTo(previous[0])) {
                                    Inventory inv = getHonorsInv(player, list, using, page - 1, isSelf, name);
                                    Task.builder().execute(() -> player.openInventory(inv)).submit(NewHonor.plugin);
                                } else if (next[0] != null && item.equalTo(next[0])) {
                                    Inventory inv = getHonorsInv(player, list, using, page + 1, isSelf, name);
                                    Task.builder().execute(() -> player.openInventory(inv)).submit(NewHonor.plugin);
                                }
                            }
                        }
                    });
            Inventory inv = builder.build(NewHonor.plugin);
            list.stream().skip((page - 1) * onePage).limit(onePage).forEach(data -> {
                ItemStack item = data.getItem();
                try {
                    map.put(Util.toStr(item.get(Keys.DISPLAY_NAME).orElseThrow(NoSuchFieldException::new)), data.getId());
                    inv.offer(data.getItem());
                } catch (NoSuchFieldException e) {
                    NewHonor.logger.warn("The honor config is error with honorid {}", data.getId());
                    NewHonor.logger.warn("error:", e);
                    player.sendMessage(Text.of("[NewHonor]error found while listing the honors."));
                }
            });
            if (page > 1) {
                previous[0] = ItemStack.builder()
                        .itemType(ItemTypes.ARROW)
                        .quantity(1)
                        .add(Keys.DISPLAY_NAME, SpongeLanguageManager.getText("newhonor.listhonors.previous", "previous page")).build();
                inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(45))).set(previous[0].copy());
            }
            if (list.size() - (onePage * page) > 0) {
                next[0] = ItemStack.builder()
                        .itemType(ItemTypes.ARROW)
                        .quantity(1)
                        .add(Keys.DISPLAY_NAME, SpongeLanguageManager.getText("newhonor.listhonors.next", "next page")).build();
                inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(53))).set(next[0].copy());
            }

            if (using != null) {
                inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(49))).set(using.getItem());
                inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(48))).set(GLASS);
                inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(50))).set(GLASS);
            }
            timing.stopTiming();
            return inv;
        }
    }
}
