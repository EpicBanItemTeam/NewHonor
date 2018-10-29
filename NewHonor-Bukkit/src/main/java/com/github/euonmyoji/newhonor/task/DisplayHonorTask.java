package com.github.euonmyoji.newhonor.task;

import com.github.euonmyoji.newhonor.NewHonor;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Team;

import java.util.List;

public class DisplayHonorTask implements Runnable {
    private Team team;
    private List<String> honors;
    private int[] delays;
    private volatile boolean running;
    private int index;

    public DisplayHonorTask(Team team, List<String> honors, int... delays) {
        if (honors.size() > delays.length) {
            throw new IllegalArgumentException();
        }
        this.team = team;
        this.honors = honors;
        this.delays = delays;
    }

    @Override
    public void run() {
        if (!running) {
            return;
        }
        team.setPrefix(honors.get(index));
        Bukkit.getScheduler().runTaskLaterAsynchronously(NewHonor.instance, this, delays[index]);
        if (index == honors.size()) {
            index = 0;
        }
        index++;
    }

}
