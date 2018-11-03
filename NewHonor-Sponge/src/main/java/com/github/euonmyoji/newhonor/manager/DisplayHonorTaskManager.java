package com.github.euonmyoji.newhonor.manager;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.task.DisplayHonorTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.List;

/**
 * @author yinyangshi
 */
public final class DisplayHonorTaskManager {
    public static final HashMap<String, DisplayHonorTask> TASKS = new HashMap<>();

    public static void submit(String id, List<Text> values, List<Text> suffixes, Team team, int[] delay) {
        synchronized (TASKS) {
            if (TASKS.get(id) == null) {
                DisplayHonorTask task = new DisplayHonorTask(id, values, suffixes, team, delay);
                Task.builder().delayTicks(1).name("NewHonor - display task:" + id)
                        .execute(task).submit(NewHonor.plugin);
                TASKS.put(id, task);
            }
        }
    }

    public static void clear() {
        synchronized (TASKS) {
            new HashMap<>(TASKS).values().forEach(DisplayHonorTask::cancel);
            TASKS.clear();
        }
    }

    private DisplayHonorTaskManager() {
        throw new UnsupportedOperationException();
    }
}
