package com.github.euonmyoji.newhonor.api.data;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.util.Util;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.serializer.TextParseException;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * @author yinyangshi
 */
public class HonorData {
    private String id;
    private Text value;
    private String strValue;
    private List<Text> displayValue;
    private List<Text> suffixes;
    private int[] delay;
    private ItemStack item;

    public HonorData(CommentedConfigurationNode cfg, String id) {
        this.id = id;
        String rawValue = cfg.getNode("value").getString("[default]");
        Text temp = jsonToText(rawValue);
        Text.Builder valueBuilder = Text.builder().append(temp);
        strValue = Util.toStr(temp);

        CommentedConfigurationNode hoverNode = cfg.getNode("hoverValue");
        if (!hoverNode.isVirtual()) {
            String type = hoverNode.getNode("type").getString("text");
            switch (type) {
                case "item": {
                    try {
                        valueBuilder.onHover(TextActions
                                .showItem(requireNonNull(hoverNode.getNode("value").getValue(TypeToken.of(ItemStackSnapshot.class)))));
                    } catch (ObjectMappingException e) {
                        NewHonor.logger.warn("Error about honor.conf (item may be wrong?)", e);
                    }
                    break;
                }
                case "entity": {
                    try {
                        valueBuilder.onHover(TextActions
                                .showEntity(requireNonNull(hoverNode.getNode("entity").getValue(TypeToken.of(Entity.class))),
                                        hoverNode.getNode("name").getString("name")));
                    } catch (ObjectMappingException | NullPointerException e) {
                        NewHonor.logger.warn("Error about honor.conf (entity may be wrong?)", e);
                    }
                    break;
                }
                default: {
                    valueBuilder.onHover(TextActions.showText(Util.toText(hoverNode.getNode("value").getString(""))));
                    break;
                }
            }
        }

        CommentedConfigurationNode clickNode = cfg.getNode("clickValue");
        if (!clickNode.isVirtual()) {
            String type = clickNode.getNode("type").getString("runCommand");
            switch (type) {
                case "open_url": {
                    try {
                        valueBuilder.onClick(TextActions.openUrl(new URL(clickNode.getNode("value").getString(""))));
                    } catch (MalformedURLException e) {
                        NewHonor.logger.warn("error with open_url", e);
                    }
                    break;
                }
                case "suggestCommand": {
                    valueBuilder.onClick(TextActions.suggestCommand(clickNode.getNode("value").getString("")));
                    break;
                }
                default: {
                    valueBuilder.onClick(TextActions.runCommand(clickNode.getNode("value").getString("")));
                    break;
                }
            }
        }

        value = valueBuilder.trim().build();
        int defaultDelay = cfg.getNode("intervalTicks").getInt(1);
        try {
            List<String> rawDisplayValue = cfg.getNode("displayValue").getList(TypeToken.of(String.class), Collections.singletonList(strValue));
            delay = new int[rawDisplayValue.size()];
            displayValue = rawDisplayValue.stream().map(new Function<String, Text>() {
                private int index = 0;

                @Override
                public Text apply(String s) {
                    String[] data = s.split(";;", 2);
                    delay[index] = data.length == 1 ? defaultDelay : Integer.valueOf(data[1]);
                    index++;
                    return Text.builder().append(Util.toText(data[0])).onClick(value.getClickAction().orElse(null))
                            .onHover(value.getHoverAction().orElse(null))
                            .trim().build();
                }
            }).collect(Collectors.toList());

            List<String> rawSuffixes = cfg.getNode("suffixValue").getList(TypeToken.of(String.class));
            if (!rawSuffixes.isEmpty()) {
                suffixes = rawSuffixes.stream().map(Util::toText).collect(Collectors.toList());
                while (suffixes.size() < displayValue.size()) {
                    suffixes.add(Text.of(""));
                }
            }
        } catch (ObjectMappingException e) {
            NewHonor.logger.warn("honor config error", e);
        }

        try {
            ItemStack.Builder builder = ItemStack.builder().itemType(ItemTypes.NAME_TAG).add(Keys.DISPLAY_NAME, value);
            List<Text> lores = new ArrayList<>();
            value.getHoverAction().ifPresent(hoverAction -> {
                if (hoverAction instanceof HoverAction.ShowText) {
                    String str = Util.toStr((Text) hoverAction.getResult());
                    String nextLine = "\n";
                    for (String s : str.split(nextLine)) {
                        lores.add(Util.toText("&r" + s));
                    }
                }
            });
            value.getClickAction().ifPresent(clickAction -> {
                if (clickAction instanceof ClickAction.RunCommand) {
                    lores.add(Text.of("点击执行:", clickAction.getResult()));
                } else if (clickAction instanceof ClickAction.SuggestCommand) {
                    lores.add(Text.of("点击建议:", clickAction.getResult()));
                }
            });
            if (!lores.isEmpty()) {
                builder.add(Keys.ITEM_LORE, lores);
            }
            item = cfg.getNode("item-value").getValue(TypeToken.of(ItemStack.class), builder.build());
        } catch (ObjectMappingException e) {
            NewHonor.logger.warn("honor config error", e);
        }
    }

    private static Text jsonToText(String str) {
        try {
            return TextSerializers.JSON.deserialize(str);
        } catch (TextParseException e) {
            return TextSerializers.FORMATTING_CODE.deserialize(str);
        }
    }

    public String getId() {
        return this.id;
    }

    public Text getValue() {
        return this.value;
    }

    public List<Text> getDisplayValue() {
        return this.displayValue;
    }

    public List<Text> getSuffixes() {
        return this.suffixes;
    }

    public String getStrValue() {
        return this.strValue;
    }

    public int[] getDelay() {
        return this.delay;
    }

    public ItemStack getItem() {
        return item.copy();
    }
}
