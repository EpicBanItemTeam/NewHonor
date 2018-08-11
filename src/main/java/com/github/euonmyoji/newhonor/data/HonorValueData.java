package com.github.euonmyoji.newhonor.data;

import com.github.euonmyoji.newhonor.util.Util;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.text.Text;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yinyangshi
 */
public class HonorValueData {
    private Text value;
    private String rawValue;
    private List<Text> displayValue;

    private final int intervalTicks;

    public HonorValueData(CommentedConfigurationNode cfg) {
        rawValue = cfg.getNode("value").getString("[default]");
        value = Util.toText(rawValue);
        intervalTicks = cfg.getNode("intervalTicks").getInt(1);
        try {
            List<String> rawDisplayValue = cfg.getNode("displayValue").getList(TypeToken.of(String.class), Collections.singletonList(rawValue));
            displayValue = rawDisplayValue.stream().map(Util::toText).collect(Collectors.toList());
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    public Text getValue() {
        return this.value;
    }

    public List<Text> getDisplayValue() {
        return this.displayValue;
    }

    public String getRawValue() {
        return this.rawValue;
    }

    public int getIntervalTicks() {
        return this.intervalTicks;
    }
}
