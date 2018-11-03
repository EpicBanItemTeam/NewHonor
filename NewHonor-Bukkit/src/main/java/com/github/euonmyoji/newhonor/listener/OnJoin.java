package com.github.euonmyoji.newhonor.listener;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.configuration.PlayerConfig;
import com.github.euonmyoji.newhonor.data.Honor;
import com.github.euonmyoji.newhonor.task.DisplayHonorTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class OnJoin implements Listener {

    @EventHandler
    public void on(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(NewHonor.plugin, () -> {
            try {
                Player player = e.getPlayer();
                Scoreboard scoreboard = player.getScoreboard();
                if (scoreboard == null) {
                    scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
                }
                PlayerConfig playerConfig = PlayerConfig.get(player.getUniqueId());
                playerConfig.checkPermission();
                if (playerConfig.isUseHonor()) {
                    String honorID = playerConfig.getUsingHonorID();
                    Honor honor = NewHonor.honorConfig.getHonor(honorID);
                    Team team;
                    if (scoreboard.getTeam(honorID) == null) {
                        team = scoreboard.registerNewTeam(honorID);
                    } else {
                        team = scoreboard.getTeam(honorID);
                    }
                    new DisplayHonorTask(team, honor.getDisplayTexts(), honor.getIntervalTicks());
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }, 0, 40);
    }
}
