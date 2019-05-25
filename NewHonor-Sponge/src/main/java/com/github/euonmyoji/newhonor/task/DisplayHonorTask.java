package com.github.euonmyoji.newhonor.task;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import com.github.euonmyoji.newhonor.NewHonor;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.List;

import static com.github.euonmyoji.newhonor.manager.DisplayHonorTaskManager.TASKS;

/**
 * @author yinyangshi
 */
public class DisplayHonorTask implements Runnable {
    private String id;
    private List<Text> values;
    private List<Text> suffixes;
    private Collection<Team> teams;
    private int[] delays;
    private int index;
    private volatile boolean running = true;

    public DisplayHonorTask(String id, List<Text> values, List<Text> suffixes, Collection<Team> teams, int[] delay) {
        if (values.size() > delay.length || values.size() != suffixes.size()) {
            throw new IllegalArgumentException();
        }
        this.id = id;
        this.values = values;
        this.suffixes = suffixes;
        this.teams = teams;
        this.delays = delay;
    }

    @Override
    public void run() {
        if (running) {
            try (Timing timing = Timings.of(NewHonor.plugin, "NewHonorDisplayTask")) {
                timing.startTimingIfSync();
                for (Team team : teams) {
                    team.setPrefix(values.get(index));
                    if (suffixes != null && suffixes.size() > index) {
                        team.setSuffix(suffixes.get(index));
                    }
                }
                Task.builder().execute(this)
                        .delayTicks(delays[index]).name("NewHonor - displayHonor Task " + id + "#" + index)
                        .submit(NewHonor.plugin);
                if (++index == values.size()) {
                    index = 0;
                }
            } catch (IllegalArgumentException | IndexOutOfBoundsException | NullPointerException e) {
                NewHonor.logger.warn("The display value is wrong", e);
                cancel();
            } catch (Throwable t) {
                NewHonor.logger.warn("Error while running display task " + id, t);
                cancel();
            }
        }
    }

    public void cancel() {
        synchronized (TASKS) {
            running = false;
            TASKS.remove(id);
        }
    }
}
