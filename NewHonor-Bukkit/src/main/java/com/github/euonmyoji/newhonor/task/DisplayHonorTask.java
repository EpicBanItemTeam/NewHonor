package com.github.euonmyoji.newhonor.task;

import com.github.euonmyoji.newhonor.NewHonor;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Team;

import java.util.List;

public class DisplayHonorTask implements Runnable {
    private Team team;
    public static List<DisplayHonorTask> tasks = Lists.newArrayList();
    private List<String> prefixes;
    private int[] delays;
    private List<String> suffixes;
    private int index;
    private volatile boolean running = true;

    public DisplayHonorTask(Team team, List<String> prefixes, List<String> suffixes, int... delays) {
        this.team = team;
        this.prefixes = prefixes;
        this.suffixes = suffixes;
        this.delays = delays;
        tasks.add(this);
        this.run();
    }

    @Override
    public void run() {
        if (running) {
            String prefix = prefixes.get(index).replace("&", "§");
            int delay;
            //fixme: 这些拖慢服务器的代码啊 给我消失吧 拯救世界 刻不容缓！ 多执行一点代码多发出一点热量会让全球变暖啊！
            if (prefix.contains(";;")) {
                delay = Integer.parseInt(prefix.split(";;")[1]);
            } else {
                delay = getDelay(delays, index);
            }
            team.setPrefix(prefix.replaceAll(";;[0-9]*", ""));
            team.setSuffix(suffixes.get(index).replace("&", "§"));
            Bukkit.getScheduler().runTaskLaterAsynchronously(NewHonor.plugin, this, delay);
            index++;
            if (index == prefixes.size()) {
                index = 0;
            }
        }
    }

    private int getDelay(int[] delays, int index) {
        if (delays.length == 1) {
            return delays[0];
        }
        return delays[index];
    }

    public void cancel() {
        this.running = false;
    }

}
