package com.github.euonmyoji.newhonor.task;

import com.github.euonmyoji.newhonor.configuration.EffectsConfig;

import java.io.IOException;
import java.util.List;

/**
 * @author yinyangshi
 */
public class TaskManager {
    /**
     * 更新插件任务缓存
     *
     * @throws IOException 读取配置文件IOE
     */
    public static void update() throws IOException {
        List<String> effects = EffectsConfig.getCreatedEffects();
        EffectsOffer.update(effects);
        HaloEffectsOffer.update(effects);
    }

    private TaskManager() {
        throw new UnsupportedOperationException();
    }
}
