package com.github.euonmyoji.newhonor.task;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.data.HonorData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
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
    private HonorData data;
    private List<Text> values;
    private List<Text> suffixes;
    private Collection<Team> teams;
    private int[] delays;
    private int index;
    private int size;
    private volatile boolean running = true;
    private boolean hasSuffix = false;

    public DisplayHonorTask(String id, List<Text> values, List<Text> suffixes, Collection<Team> teams, int[] delay) {
        if (values.size() > delay.length || values.size() != suffixes.size()) {
            throw new IllegalArgumentException();
        }
        this.id = id;
        this.values = values;
        this.suffixes = suffixes;
        this.teams = teams;
        this.delays = delay;
        this.size = values.size();
    }

    public DisplayHonorTask(String id, HonorData honorData, List<Team> teams, int[] delay) {
        this.id = id;
        this.data = honorData;
        this.teams = teams;
        this.delays = delay;
        if (data.getSuffixes() != null && data.getSuffixes().size() > 0) {
            hasSuffix = true;
        }
        size = honorData.getDisplayValueSize();
    }

    @Override
    public void run() {
        if (running) {
            try (Timing timing = Timings.of(NewHonor.plugin, "NewHonorDisplayTask")) {
                timing.startTimingIfSync();
                for (Team team : teams) {
                    if (data != null) {
                        Player p = Sponge.getServer().getPlayer(team.getName()).orElse(null);
                        if (p == null) {
                            cancel();
                        } else {
                            team.setPrefix(data.getDisplayValue(p, index));
                            if (hasSuffix) {
                                team.setSuffix(data.getSuffixValue(p, index));
                            }
                        }
                    } else {
                        team.setPrefix(values.get(index));
                        if (suffixes != null && suffixes.size() > index) {
                            team.setSuffix(suffixes.get(index));
                        }
                    }
                }
                Task.builder().execute(this)
                        .delayTicks(delays[index]).name("NewHonor - displayHonor Task " + id + "#" + index)
                        .submit(NewHonor.plugin);
                if (++index == size) {
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
