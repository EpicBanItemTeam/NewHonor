package com.github.euonmyoji.newhonor;

import com.github.euonmyoji.newhonor.configuration.PlayerData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.text.Text;

import java.util.ConcurrentModificationException;
import java.util.Optional;
import java.util.UUID;

/**
 * @author yinyangshi
 */
class ScoreBoardManager {
    static boolean enable = false;
    private static Scoreboard scoreboard = Scoreboard.builder().build();

    static void init() {
        if (enable) {
            initAllPlayers();
        }
    }

    static void clear() {
        getScoreBoard().getTeams().forEach(team -> team.getMembers().forEach(team::removeMember));
    }

    static void initPlayer(Player p) {
        if (enable) {
            Task.builder().execute(() -> {
                try {
                    execute(p);
                } catch (ConcurrentModificationException e) {
                    Task.builder().execute(() -> {
                    }).name("NewHonor - execute " + p.getName()).submit(NewHonor.plugin);
                } catch (Throwable e) {
                    NewHonor.plugin.logger.warn("Error(s)!", e);
                }
            }).async().name("NewHonor - execute " + p.getName()).submit(NewHonor.plugin);
            setPlayerScoreBoard(p);
        }

    }

    private static void execute(Player p) throws Exception {
        UUID uuid = p.getUniqueId();
        PlayerData pd = PlayerData.get(p);
        String honorID = pd.getUsingHonorID();
        if (honorID == null) {
            return;
        }
        Optional<Team> optionalTeam = getScoreBoard().getTeam(honorID);
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
                    getScoreBoard().registerTeam(optionalTeam.get());
                }
                optionalTeam.ifPresent(team -> team.addMember(p.getTeamRepresentation()));
            }
        }

    }

    private static void setPlayerScoreBoard(Player p) {
        p.setScoreboard(getScoreBoard());
    }

    private static final Object LOCK = new Object();

    private static Scoreboard getScoreBoard() {
        if (scoreboard == null) {
            synchronized (LOCK) {
                if (scoreboard == null) {
                    NewHonor.plugin.logger.warn("The scoreboard was unexpected removed! Building a new one.");
                    scoreboard = Scoreboard.builder().build();
                }
            }
        }
        return scoreboard;
    }

    private static void initAllPlayers() {
        Sponge.getServer().getOnlinePlayers().forEach(ScoreBoardManager::initPlayer);
    }
}
