package com.github.euonmyoji.newhonor.data;

import com.github.euonmyoji.newhonor.NewHonor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 随机延迟数据
 *
 * @author yinyangshi
 */
public class RandomDelayData {
    private static final Random R = new Random();
    private final List<Range> ranges = new ArrayList<>();

    public RandomDelayData(String arg) {
        try {
            String[] ors = arg.split(",");
            for (String value : ors) {
                ranges.add(new Range(value.split("~", 2)));
            }
        } catch (Exception e) {
            NewHonor.logger.warn("There is something wrong with EffectsDelay: " + arg, e);
        }
    }

    public int getDelay() {
        return ranges.get(R.nextInt(ranges.size())).get();
    }

    private class Range {
        private static final int MAX_LENGTH = 2;
        private int min;
        private int max;

        private Range(String[] arg) throws NumberFormatException {
            if (arg.length == 1) {
                min = Integer.parseInt(arg[0]);
                max = min;
            } else if (arg.length == MAX_LENGTH) {
                int a = Integer.parseInt(arg[0]);
                int b = Integer.parseInt(arg[1]);
                min = Math.min(a, b);
                max = Math.max(a, b);
            } else {
                throw new IllegalArgumentException("Not a correct delay expression");
            }
        }

        private int get() {
            return min == max ? min : R.nextInt(1 + max - min) + min;
        }
    }
}
