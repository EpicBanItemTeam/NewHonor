package com.github.euonmyoji.newhonor.data;

import com.github.euonmyoji.newhonor.util.Util;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.text.Text;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author yinyangshi
 */
public class HonorValueData {
    private Text value;
    private String rawValue;
    private List<Text> displayValue;
    private int[] delay;

    public HonorValueData(CommentedConfigurationNode cfg) {
        rawValue = cfg.getNode("value").getString("[default]");
        value = Util.toText(rawValue);
        int defaultDelay = cfg.getNode("intervalTicks").getInt(1);
        try {
            List<String> rawDisplayValue = cfg.getNode("displayValue").getList(TypeToken.of(String.class), Collections.singletonList(rawValue));
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

    public Text getValue() {
        return this.value;
    }

    public List<Text> getDisplayValue() {
        return this.displayValue;
    }

    public String getRawValue() {
        return this.rawValue;
    }

    public int[] getDelay() {
        return this.delay;
    }
}
