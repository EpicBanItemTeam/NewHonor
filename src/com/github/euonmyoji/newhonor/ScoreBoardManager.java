package com.github.euonmyoji.newhonor;

import com.github.euonmyoji.newhonor.configuration.PlayerData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class ScoreBoardManager {
    private static Scoreboard scoreboard = Scoreboard.builder().build();

    public static void init() {
        scoreboard = Scoreboard.builder().build();
        initAllPlayer();
    }

    static void initPlayer(Player p) {
        execute(p);
    }

    private static void execute(Player p) {
        UUID uuid = p.getUniqueId();
        PlayerData pd = new PlayerData(p);
        Set<Team> teams = scoreboard.getTeams();
        if (!pd.isDisplayHonor()) {
            teams.removeIf(team -> {
                team.removeMember(p.getTeamRepresentation());
                return team.getName().equals(p.getName());
            });
        } else if (NewHonor.honorTextCache.containsKey(uuid)) {
            Text text = NewHonor.honorTextCache.get(uuid);
            teams.removeIf(team -> team.getName().equals(p.getName()));
            teams.add(Team.builder().prefix(text).name(p.getName()).build());
        }
        scoreboard = Scoreboard.builder().teams(new ArrayList<>(teams)).build();
        scoreboard.getTeam(p.getName()).ifPresent(team -> team.addMember(p.getTeamRepresentation()));
        setAllPlayerScoreBoard();
    }

    private static void setPlayerScoreBoard(Player p) {
        p.setScoreboard(scoreboard);
    }

    private static void setAllPlayerScoreBoard() {
        Sponge.getServer().getOnlinePlayers().forEach(ScoreBoardManager::setPlayerScoreBoard);
    }


    private static void initAllPlayer() {
        Sponge.getServer().getOnlinePlayers().forEach(ScoreBoardManager::initPlayer);
    }
}
