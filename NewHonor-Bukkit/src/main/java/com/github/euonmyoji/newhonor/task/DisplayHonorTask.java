package com.github.euonmyoji.newhonor.task;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.configuration.PlayerConfig;
import com.github.euonmyoji.newhonor.data.Honor;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    private static final Object LOCK = new Object();

    public static void init(Player p) throws Exception{
        UUID uuid = p.getUniqueId();
        PlayerConfig pd = PlayerConfig.get(uuid);
        String honorID = pd.getUsingHonorID();
        if (honorID == null) {
            return;
        }
        synchronized (LOCK) {
            Scoreboard scoreboard = p.getScoreboard();
            scoreboard.getTeams().forEach(team -> team.removeEntry(uuid.toString()));
            Optional<Team> optionalTeam = Optional.of(scoreboard.getTeam(honorID));
            boolean isTeamPresent = optionalTeam.isPresent();
            if (pd.isUseHonor()) {
                if (NewHonor.honorCacheMap.containsKey(uuid)) {
                    Honor valueData = NewHonor.honorCacheMap.get(uuid);
                    List<String> prefixes = valueData.getDisplayTexts();
                    List<String> suffixes = valueData.getSuffixes();
                    String prefix = prefixes.get(0);
                    if (isTeamPresent) {
                        optionalTeam.get().setPrefix(prefix);
                        optionalTeam.get().setSuffix(suffixes == null ? "" : suffixes.get(0));
                    } else {
                        Team team = scoreboard.registerNewTeam(honorID);
                        team.setPrefix(prefix);
                        team.setSuffix(suffixes == null ? "" : suffixes.get(0));
                        optionalTeam = Optional.of(team);
                    }
                    optionalTeam.ifPresent(team -> team.addEntry(uuid.toString()));
                    if (prefixes.size() > 1) {
                        new DisplayHonorTask(optionalTeam.get(), prefixes, suffixes, valueData.getIntervalTick());
                    }
                }
            }
        }
    }

    private DisplayHonorTask(Team team, List<String> prefixes, List<String> suffixes, int delay) {
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
