package com.github.euonmyoji.newhonor.data;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.util.Util;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.serializer.TextParseException;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author yinyangshi
 */
public class HonorValueData {
    private Text value;
    private String strValue;
    private List<Text> displayValue;
    private int[] delay;

    public HonorValueData(CommentedConfigurationNode cfg) {
        String rawValue = cfg.getNode("value").getString("[default]");
        Text temp = jsonToText(rawValue);
        Text.Builder valueBuilder = Text.builder().append(temp);
        strValue = TextSerializers.FORMATTING_CODE.serialize(temp);

        CommentedConfigurationNode hoverNode = cfg.getNode("hoverValue");
        if (!hoverNode.isVirtual()) {
            String type = hoverNode.getNode("type").getString("text");
            switch (type) {
                case "item": {
                    try {
                        valueBuilder.onHover(TextActions.showItem(hoverNode.getNode("value").getValue(TypeToken.of(ItemStackSnapshot.class))));
                    } catch (ObjectMappingException e) {
                        NewHonor.plugin.logger.warn("Error about honor.conf (item may be wrong?)", e);
                    }
                    break;
                }
                case "entity": {
                    try {
                        valueBuilder.onHover(TextActions.showEntity(hoverNode.getNode("entity").getValue(TypeToken.of(Entity.class)),
                                hoverNode.getNode("name").getString("name")));
                    } catch (ObjectMappingException e) {
                        NewHonor.plugin.logger.warn("Error about honor.conf (entity may be wrong?)", e);
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
                        valueBuilder.onClick(TextActions.openUrl(new URL(clickNode.getNode("value").getString())));
                    } catch (MalformedURLException e) {
                        NewHonor.plugin.logger.warn("error with open_url", e);
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

        value = valueBuilder.build();
        int defaultDelay = cfg.getNode("intervalTicks").getInt(1);
        try {
            List<String> rawDisplayValue = cfg.getNode("displayValue").getList(TypeToken.of(String.class), Collections.singletonList(strValue));
            delay = new int[rawDisplayValue.size()];
            displayValue = rawDisplayValue.stream().map(new Function<String, Text>() {
                private int index = 0;

                @Override
                public Text apply(String s) {
                    String[] data = s.split(";;");
                    delay[index] = data.length == 1 ? defaultDelay : Integer.valueOf(data[1]);
                    index++;
                    return Util.toText(data[0]);
                }
            }).collect(Collectors.toList());
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    private static Text jsonToText(String str) {
        try {
            return TextSerializers.JSON.deserialize(str);
        } catch (TextParseException e) {
            return TextSerializers.FORMATTING_CODE.deserialize(str);
        }
    }

    public Text getValue() {
        return this.value;
    }

    public List<Text> getDisplayValue() {
        return this.displayValue;
    }

    public String getStrValue() {
        return this.strValue;
    }

    public int[] getDelay() {
        return this.delay;
    }
}
