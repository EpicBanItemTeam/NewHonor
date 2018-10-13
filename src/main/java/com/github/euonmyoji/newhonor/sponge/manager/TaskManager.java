package com.github.euonmyoji.newhonor.sponge.manager;

import com.github.euonmyoji.newhonor.sponge.configuration.EffectsConfig;
import com.github.euonmyoji.newhonor.sponge.task.EffectsOfferTask;
import com.github.euonmyoji.newhonor.sponge.task.HaloEffectsOfferTask;

import java.io.IOException;
import java.util.List;

/**
 * @author yinyangshi
 */
public final class TaskManager {
    /**
     * 更新插件任务缓存
     *
     * @throws IOException 读取配置文件IOE
     */
    public static void update() throws IOException {
        List<String> effects = EffectsConfig.getCreatedEffects();
        EffectsOfferTask.update(effects);
        HaloEffectsOfferTask.update(effects);
    }

    private TaskManager() {
        throw new UnsupportedOperationException();
    }
}
