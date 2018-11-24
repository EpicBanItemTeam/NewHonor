package com.github.euonmyoji.newhonor.task;

import com.github.euonmyoji.newhonor.NewHonor;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Team;

import java.util.List;

/**
 * @author NewHonor authors
 */
public class DisplayHonorTask implements Runnable {
    public static List<DisplayHonorTask> tasks = Lists.newArrayList();
    private Team team;
    private List<String> prefixes;
    private int delay;
    private List<String> suffixes;
    private int index;
    private volatile boolean running = true;

    public DisplayHonorTask(Team team, List<String> prefixes, List<String> suffixes, int delay) {
        if (prefixes.size() != suffixes.size() && prefixes.size() == 0) {
            return;
        }
        this.team = team;
        this.prefixes = prefixes;
        this.suffixes = suffixes;
        this.delay = delay;
        tasks.add(this);
        this.run();
    }

    @Override
    public void run() {
        if (running) {
            String prefix = prefixes.get(index).replace("&", "§");
            int delay1 = prefix.contains(";;") ? Integer.parseInt(prefix.split(";;")[1]) : delay;
            //fixme: 这些拖慢服务器的代码啊 给我消失吧 拯救世界 刻不容缓！ 多执行一点代码多发出一点热量会让全球变暖啊！
            team.setPrefix(prefix.replaceAll(";;[0-9]*", ""));
            team.setSuffix(suffixes.get(index).replace("&", "§"));
            Bukkit.getScheduler().runTaskLaterAsynchronously(NewHonor.plugin, this, delay1);
            index++;
            if (index == prefixes.size()) {
                index = 0;
            }
        }
    }

    public void cancel() {
        this.running = false;
    }

}
