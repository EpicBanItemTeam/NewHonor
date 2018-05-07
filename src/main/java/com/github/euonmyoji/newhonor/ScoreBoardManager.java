package com.github.euonmyoji.newhonor;

import com.github.euonmyoji.newhonor.configuration.PlayerData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.text.Text;

import java.util.Optional;
import java.util.UUID;

/**
 * @author yinyangshi
 */
public class ScoreBoardManager {
    static boolean enable = false;
    private static Scoreboard scoreboard = Scoreboard.builder().build();

    static void init() {
        if (enable) {
            scoreboard = Scoreboard.builder().build();
            initAllPlayers();
        }
    }

    static void clear() {
        scoreboard.getTeams().forEach(team -> team.getMembers().forEach(team::removeMember));
    }

    static void initPlayer(Player p) {
        if (enable) {
            execute(p);
        }
    }

    private static void execute(Player p) {
        if (enable) {
            Task.builder().execute(() -> {
                UUID uuid = p.getUniqueId();
                PlayerData pd = new PlayerData(p);
                String honorID = pd.getUsingHonorID();
                Optional<Team> optionalTeam = scoreboard.getTeam(honorID);
                boolean isTeamPresent = optionalTeam.isPresent();
                optionalTeam.ifPresent(team -> team.removeMember(p.getTeamRepresentation()));
                if (pd.isUseHonor()) {
                    if (NewHonor.plugin.honorTextCache.containsKey(uuid)) {
                        Text prefix = NewHonor.plugin.honorTextCache.get(uuid);
                        if (isTeamPresent) {
                            optionalTeam.get().setPrefix(prefix);
                        } else {
                            optionalTeam = Optional.of(Team.builder()
                                    .name(honorID)
                                    .prefix(prefix)
                                    .build());
                            scoreboard.registerTeam(optionalTeam.get());
                        }
                        optionalTeam.ifPresent(team -> team.addMember(p.getTeamRepresentation()));
                    }
                }
                setPlayerScoreBoard(p);
            }).async().name("NewHonor - execute" + p.getName()).submit(NewHonor.plugin);
        }
    }

    private static void setPlayerScoreBoard(Player p) {
        if (enable) {
            p.setScoreboard(scoreboard);
        }
    }


    private static void initAllPlayers() {
        if (enable) {
            Sponge.getServer().getOnlinePlayers().forEach(ScoreBoardManager::initPlayer);
        }
    }
}
