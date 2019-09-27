package com.github.euonmyoji.newhonor.manager;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.data.HonorData;
import com.github.euonmyoji.newhonor.task.DisplayHonorTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * @author yinyangshi
 */
public final class DisplayHonorTaskManager {
    public static final HashMap<String, DisplayHonorTask> TASKS = new HashMap<>();

    private DisplayHonorTaskManager() {
        throw new UnsupportedOperationException();
    }

    public static void submit(String id, List<Text> values, List<Text> suffixes, Collection<Team> team, int[] delay) {
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

    public static void submit(String idName, HonorData honorData, List<Team> teams, int[] delay) {
        synchronized (TASKS) {
            if (TASKS.get(idName) == null) {
                DisplayHonorTask task = new DisplayHonorTask(idName, honorData,teams, delay);
                Task.builder().delayTicks(1).name("NewHonor - display task:" + idName)
                        .execute(task).submit(NewHonor.plugin);
                TASKS.put(idName, task);
            }
        }
    }
}
