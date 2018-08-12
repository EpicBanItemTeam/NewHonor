package com.github.euonmyoji.newhonor.task;

import com.github.euonmyoji.newhonor.NewHonor;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author yinyangshi
 */
public class DisplayHonorTask {
    private static final HashMap<String, Task> TASKS = new HashMap<>();

    public static void submit(String id, List<Text> values, Scoreboard scoreboard, int speed) {
        synchronized (TASKS) {
            if (TASKS.get(id) == null) {
                Task task = Task.builder().delayTicks(1).name("NewHonor - display task:" + id).intervalTicks(speed)
                        .execute(new Consumer<Task>() {
                            private int index = 0;

                            @Override
                            public void accept(Task task) {
                                try {
                                    scoreboard.getTeam(id).orElseThrow(NoSuchFieldException::new).setPrefix(values.get(index));
                                    index++;
                                    if (index == values.size()) {
                                        index = 0;
                                    }
                                } catch (Throwable ignore) {
                                    synchronized (TASKS) {
                                        task.cancel();
                                        TASKS.remove(id);
                                    }
                                }
                            }
                        }).submit(NewHonor.plugin);
                TASKS.put(id, task);
            }
        }
    }

    public static void clear() {
        synchronized (TASKS) {
            TASKS.values().forEach(Task::cancel);
            TASKS.clear();
        }
    }

    private DisplayHonorTask() {
        throw new UnsupportedOperationException();
    }
}
