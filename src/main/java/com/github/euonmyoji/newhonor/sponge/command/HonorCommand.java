package com.github.euonmyoji.newhonor.sponge.command;

import com.github.euonmyoji.newhonor.common.manager.LanguageManager;
import com.github.euonmyoji.newhonor.sponge.NewHonor;
import com.github.euonmyoji.newhonor.sponge.api.configuration.PlayerConfig;
import com.github.euonmyoji.newhonor.sponge.command.args.HonorIDArg;
import com.github.euonmyoji.newhonor.sponge.command.args.SettingsArg;
import com.github.euonmyoji.newhonor.sponge.configuration.HonorConfig;
import com.github.euonmyoji.newhonor.sponge.data.HonorData;
import com.github.euonmyoji.newhonor.sponge.manager.SpongeLanguageManager;
import com.github.euonmyoji.newhonor.sponge.util.Util;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
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
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.util.Identifiable;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.github.euonmyoji.newhonor.common.manager.LanguageManager.langBuilder;
import static com.github.euonmyoji.newhonor.sponge.manager.SpongeLanguageManager.getText;
import static org.spongepowered.api.text.Text.of;
import static org.spongepowered.api.text.action.TextActions.runCommand;
import static org.spongepowered.api.text.action.TextActions.showText;

/**
 * @author yinyangshi
 */
public final class HonorCommand {
    private static String ADMIN_PERMISSION = "newhonor.admin";
    private static final HashMap<UUID, Integer> USE_CD = new HashMap<>();
    private static String ID_KEY = "id";

    static {
        Task.builder().execute(() -> new HashMap<>(USE_CD).forEach((uuid, integer) -> {
            USE_CD.put(uuid, integer - 1);
            if (USE_CD.get(uuid) <= 0) {
                USE_CD.remove(uuid);
            }
        })).async().intervalTicks(20).submit(NewHonor.plugin);
    }

    private static CommandSpec data = CommandSpec.builder()
            .permission(ADMIN_PERMISSION)
            .executor((src, args) -> {
                src.sendMessage(of("/honor data updateToSql   Update PlayerData to Sql"));
                src.sendMessage(of("/honor data downloadFromSql   Download PlayerData from Sql"));
                return CommandResult.success();
            })
            .child(SqlCommand.downloadFromSql, "downloadFromSql")
            .child(SqlCommand.updateToSql, "updateToSql")
            .build();

    private static CommandSpec use = CommandSpec.builder()
            .arguments(new HonorIDArg(of(ID_KEY)))
            .executor((src, args) -> {
                if (!(src instanceof Identifiable)) {
                    src.sendMessage(getText("newhonor.changehonor.unknownsource"));
                    return CommandResult.empty();
                }
                if (!src.hasPermission(ADMIN_PERMISSION) && USE_CD.containsKey(((Identifiable) src).getUniqueId())) {
                    int cd = USE_CD.get(((Identifiable) src).getUniqueId());
                    src.sendMessage(of("[NewHonor]You should wait " + cd + " second(s) to change use honor"));
                    return CommandResult.empty();
                }
                Task.builder().execute(() -> {
                    try {
                        PlayerConfig pd = PlayerConfig.get((User) src);
                        pd.checkPermission();
                        if (pd.setUseHonor(args.<String>getOne(of(ID_KEY)).orElseThrow(NoSuchFieldError::new))) {
                            src.sendMessage(getText("newhonor.changehonor.succeed"));
                        } else {
                            pd.setUseHonor("");
                            pd.init();
                            src.sendMessage(getText("newhonor.changehonor.failed"));
                        }
                        NewHonor.updateCache(pd);
                        if (!src.hasPermission(ADMIN_PERMISSION)) {
                            USE_CD.put(((Identifiable) src).getUniqueId(), 9);
                        }
                    } catch (Exception e) {
                        src.sendMessage(getText("[NewHonor] error!"));
                        NewHonor.logger.warn("error while changing using honor", e);
                    }
                }).async().name("newhonor - Player Change Using Honor").submit(NewHonor.plugin);
                return CommandResult.success();
            })
            .build();

    private static HashMap<String, LocalDateTime> listCD = new HashMap<>();
    private static CommandSpec list = CommandSpec.builder()
            .arguments(GenericArguments.optional(GenericArguments.user(of("user"))))
            .executor((src, args) -> {
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

                                //noinspection SwitchStatementDensity  对不起我真的太垃圾了(
                                switch (pd.getListHonorStyle()) {
                                    case ITEM:
                                        if (src instanceof Player && isSelf) {
                                            ItemStack using = pd.getUsingHonorValue().map(HonorData::getItem).orElse(ItemStack.empty());
                                            Task.builder().execute(() -> {
                                                List<HonorData> dataList = honors.get().stream().map(HonorConfig::getHonorData)
                                                        .filter(Optional::isPresent)
                                                        .map(Optional::get)
                                                        .collect(Collectors.toList());
                                                ((Player) src).openInventory(getHonorsInv(((Player) src), dataList, using, 1));
                                            }).submit(NewHonor.plugin);
                                            break;
                                        }
                                    default: {
                                        //text list ------------------------------------------ TEXT TEXT
                                        PaginationList.Builder builder = PaginationList.builder()
                                                .title(Util.toText(langBuilder("newhonor.listhonors.title").replace("%ownername%", user.getName()).build())).padding(of("-"));
                                        String usingID = pd.getUsingHonorID();
                                        HonorConfig.getHonorData(usingID)
                                                .ifPresent(data -> builder.header(Util.toText(langBuilder("newhonor.listhonors.header")
                                                        .replace("%ownername%", user.getName())
                                                        .replaceHonor(data.getStrValue())
                                                        .replace("%effectsID%", HonorConfig.getEffectsID(usingID).orElse("null"))
                                                        .build())));
                                        List<Text> texts = honors.get().stream().map(id -> HonorConfig.getHonorData(id).map(data -> Text.builder()
                                                //显示头衔 药水效果组
                                                .append(Util.toText(langBuilder("newhonor.listhonors.contexts")
                                                        .replaceHonorid(id)
                                                        .replaceHonor(data.getStrValue())
                                                        .replace("%effectsID%", HonorConfig.getEffectsID(id).orElse("null"))
                                                        .build()))
                                                .onHover(showText(Util.toText(langBuilder("newhonor.listhonors.clickuse")
                                                        .replaceHonor(data.getStrValue())
                                                        .replaceHonorid(id)
                                                        .build())))
                                                .onClick(runCommand("/honor use " + id))
                                                .build()))
                                                .filter(Optional::isPresent)
                                                .map(Optional::get)
                                                .collect(Collectors.toList());
                                        builder.contents(texts).build().sendTo(src);
                                        //text list end --------------------------------------------- TEXT END
                                        break;
                                    }
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
            })
            .build();

    private static CommandSpec settings = CommandSpec.builder()
            .permission("newhonor.settings")
            .executor((src, args) -> {
                src.sendMessage(of("-------------------------------------"));
                src.sendMessage(Text.builder().append(of("/honor settings usehonor true/false -", getText("newhonor.command.describe.settings.usehonor")))
                        .onClick(TextActions.suggestCommand("/honor settings usehonor ")).onHover(showText(getText("newhonor.hovermessage.settings.usehonor"))).build());

                src.sendMessage(Text.builder().append(of("/honor settings enableeffects true/false -", getText("newhonor.command.describe.settings.enableeffects")))
                        .onClick(TextActions.suggestCommand("/honor settings enableeffects ")).onHover(showText(getText("newhonor.hovermessage.settings.enableeffects"))).build());

                src.sendMessage(Text.builder().append(of("/honor settings autochange true/false -", getText("newhonor.command.describe.settings.autochange")))
                        .onClick(TextActions.suggestCommand("/honor settings autochange ")).onHover(showText(getText("newhonor.hovermessage.settings.autochange"))).build());
                src.sendMessage(of("-------------------------------------"));
                return CommandResult.success();
            })
            .child(SettingsArg.usehonor, "usehonor")
            .child(SettingsArg.enableEffects, "enableeffects")
            .child(SettingsArg.autochange, "autochange")
            .build();

    private static CommandSpec admin = CommandSpec.builder()
            .permission(ADMIN_PERMISSION)
            .executor((src, args) -> {
                src.sendMessage(of("-------------------------------------"));
                src.sendMessage(of(""));
                src.sendMessage(of("/honor admin effects <honorID> <effectsID> -", getText("newhonor.command.describe.admin.effects")));
                src.sendMessage(of("/honor admin add <honorID> <honor> -", getText("newhonor.command.describe.admin.add")));
                src.sendMessage(of("/honor admin set <honorID> <honor> -", getText("newhonor.command.describe.admin.set")));
                src.sendMessage(of("/honor admin delete <honorID> -", getText("newhonor.command.describe.admin.delete")));
                src.sendMessage(of("/honor admin give <user(s)> <honorID> -", getText("newhonor.command.describe.admin.give")));
                src.sendMessage(of("/honor admin take <user(s)> <honorID> -", getText("newhonor.command.describe.admin.take")));
                src.sendMessage(of("/honor admin list -", getText("newhonor.command.describe.admin.list")));
                src.sendMessage(of("/honor admin reload -", getText("newhonor.command.describe.admin.reload")));
                src.sendMessage(of("/honor admin refresh -", getText("newhonor.command.describe.admin.refresh")));
                src.sendMessage(of("-------------------------------------"));
                return CommandResult.success();
            })
            .child(AdminCommand.list, "list")
            .child(AdminCommand.add, "add")
            .child(AdminCommand.delete, "delete")
            .child(AdminCommand.set, "set")
            .child(AdminCommand.give, "give")
            .child(AdminCommand.take, "take")
            .child(AdminCommand.refresh, "refresh")
            .child(AdminCommand.reload, "reload")
            .child(AdminCommand.effects, "effects")
            .build();

    private static CommandSpec effects = CommandSpec.builder()
            .permission(ADMIN_PERMISSION)
            .executor((src, args) -> {
                src.sendMessage(of("/honor effects set <effectsID> <PotionEffectID> <level> -", getText("newhonor.command.describe.effects.set")));
                src.sendMessage(of("/honor effects delete <effectsID> -", getText("newhonor.command.describe.effects.delete")));
                src.sendMessage(of("/honor effects remove <PotionEffectID> <effectsID> -", getText("newhonor.command.describe.effects.remove")));
                src.sendMessage(of("/honor effects info <effectsID> -", getText("newhonor.command.describe.effects.info")));
                src.sendMessage(of("/honor effects listAllPotionEffects -", getText("newhonor.command.describe.effects.listAllPotionEffects")));
                src.sendMessage(of("/honor effects listAllCreatedEffects -", getText("newhonor.command.describe.effects.listAllCreatedEffects")));
                return CommandResult.success();
            })
            .child(EffectsCommand.delete, "delete")
            .child(EffectsCommand.set, "set")
            .child(EffectsCommand.remove, "remove")
            .child(EffectsCommand.info, "info")
            .child(EffectsCommand.listAllPotionEffects, "listAllPotionEffects")
            .child(EffectsCommand.listAllCreatedEffects, "listAllCreatedEffects")
            .child(EffectsCommand.listAllParticle, "listAllParticle")
            .build();

    public static CommandSpec honor = CommandSpec.builder()
            .permission("newhonor.use")
            .executor((src, args) -> {
                src.sendMessage(of("----------NewHonorV" + NewHonor.VERSION + "----------"));
                src.sendMessage(of("/honor admin  -", getText("newhonor.command.describe.admin")));
                src.sendMessage(Text.builder().append(of("/honor list [User]   -", getText("newhonor.command.describe.list")))
                        .onClick(runCommand("/honor list")).onHover(showText(getText("newhonor.command.hovermessage.list"))).build());
                src.sendMessage(Text.builder().append(of("/honor settings    -", getText("newhonor.command.describe.settings")))
                        .onClick(runCommand("/honor settings")).onHover(showText(getText("newhonor.command.hovermessage.settings"))).build());
                src.sendMessage(of("/honor effects    -", getText("newhonor.command.describe.effects")));
                src.sendMessage(of("-------------------------------------"));
                return CommandResult.success();
            })
            .child(settings, "settings")
            .child(admin, "admin")
            .child(use, "use")
            .child(list, "list")
            .child(effects, "effects")
            .child(data, "data")
            .build();

    private HonorCommand() {
        throw new UnsupportedOperationException();
    }

    //00 01 02 03 04 05 06 07 08
    //09 10 11 12 13 14 15 16 17
    //18 19 20 21 22 23 24 25 26
    //27 28 29 30 31 32 33 34 35
    //36 37 38 39 40 41 42 43 44
    //45 46 47 48 49 50 51 52 53

    private static final ItemStack GLASS = ItemStack.builder().itemType(ItemTypes.GLASS_PANE).add(Keys.DISPLAY_NAME, Text.of("")).build();

    private static Inventory getHonorsInv(Player player, List<HonorData> list, ItemStack using, int page) {
        final int onePage = 5 * 9;

        ItemStack[] previous = new ItemStack[1];
        ItemStack[] next = new ItemStack[1];
        HashMap<Text, String> map = new HashMap<>(list.size() - page * onePage + onePage);
        Inventory.Builder builder = Inventory.builder().of(InventoryArchetypes.DOUBLE_CHEST)
                .property(InventoryTitle.PROPERTY_NAME, new InventoryTitle(Util.toText(langBuilder("newhonor.listhonors.invtitle")
                        .replaceName(player.getName())
                        .replace("n", list.size() + "")
                        .build())))
                .listener(InteractInventoryEvent.class, event -> {
                    if (!(event instanceof InteractInventoryEvent.Open
                            || event instanceof InteractInventoryEvent.Close)) {
                        event.setCancelled(true);
                    }
                    if (event instanceof ClickInventoryEvent.Primary) {
                        ClickInventoryEvent.Primary eve = ((ClickInventoryEvent.Primary) event);
                        eve.getTargetInventory().peek().ifPresent(item -> {
                            String id = map.get(item.get(Keys.DISPLAY_NAME).orElseThrow(NoSuchFieldError::new));
                            if (id != null) {
                                Sponge.getCommandManager().process(player, "honor use " + id);
                                player.closeInventory();
                                return;
                            }
                            if (page > 1 && previous[0] != null && item.equalTo(previous[0])) {
                                player.openInventory(getHonorsInv(player, list, using, page - 1));
                            } else if (next[0] != null && item.equalTo(next[0])) {
                                player.openInventory(getHonorsInv(player, list, using, page + 1));
                            }
                        });
                    }
                });
        Inventory inv = builder.build(NewHonor.plugin);
        list.stream().skip((page - 1) * onePage).limit(onePage).forEach(data -> {
            ItemStack item = data.getItem();
            map.put(item.get(Keys.DISPLAY_NAME).orElseThrow(NoSuchFieldError::new), data.getId());
            inv.offer(data.getItem());
        });
        if (page > 1) {
            previous[0] = ItemStack.builder()
                    .itemType(ItemTypes.ARROW)
                    .add(Keys.DISPLAY_NAME, SpongeLanguageManager.getText("newhonor.listhonors.previous", "previous page")).build();
            inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(45))).set(previous[0]);
        }
        if (list.size() - onePage * page > onePage) {
            next[0] = ItemStack.builder()
                    .itemType(ItemTypes.ARROW)
                    .add(Keys.DISPLAY_NAME, SpongeLanguageManager.getText("newhonor.listhonors.next", "next page")).build();
            inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(53))).set(next[0]);
        }

        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(49))).set(using);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(48))).set(GLASS);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(50))).set(GLASS);
        return inv;
    }
}
