package com.github.euonmyoji.newhonor.manager;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.configuration.PlayerConfig;
import com.github.euonmyoji.newhonor.api.data.HonorData;
import com.github.euonmyoji.newhonor.api.manager.HonorManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.text.Text;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author yinyangshi
 */
public final class ScoreBoardManager {
    private static final Object LOCK = new Object();
    public static boolean enable = false;
    private static Scoreboard scoreboard = Scoreboard.builder().build();

    private ScoreBoardManager() {
        throw new UnsupportedOperationException();
    }

    public static void init() {
        if (enable) {
            initAllPlayers();
        }
    }

    /**
     * 清掉scoreboard数据
     */
    public static void clear() {
        DisplayHonorTaskManager.clear();
        getScoreBoard().getTeams().forEach(team -> team.getMembers().forEach(team::removeMember));
    }

    /**
     * 初始化玩家
     *
     * @param p 玩家
     */
    public static void initPlayer(Player p) {
        if (enable) {
            try {
                execute(p);
            } catch (Exception e) {
                NewHonor.logger.warn("init player scoreboard error", e);
            }
            setPlayerScoreBoard(p);
        }
    }

    /**
     * 更新玩家拥有头衔到scoreboard上 并加入对应队伍
     *
     * @param p 玩家
     * @throws SQLException 读取玩家配置发生error
     */
    private static void execute(Player p) throws Exception {
        UUID uuid = p.getUniqueId();
        PlayerConfig pd = PlayerConfig.get(uuid);
        String honorID = pd.getUsingHonorID();
        synchronized (LOCK) {
            p.getScoreboard().getTeams().forEach(team -> team.removeMember(p.getTeamRepresentation()));
            if (honorID != null) {
                Optional<Team> optionalTeam = getScoreBoard().getTeam(honorID);
                if (pd.isUseHonor()) {
                    HonorData valueData = Sponge.getServiceManager().provideUnchecked(HonorManager.class).getUsingHonor(uuid);
                    if (valueData != null) {
                        List<Text> prefixes = valueData.getDisplayValue();
                        List<Text> suffixes = valueData.getSuffixes();
                        Text prefix = prefixes.get(0);
                        Team team;
                        if (optionalTeam.isPresent()) {
                            team = optionalTeam.get();
                            team.setPrefix(prefix);
                            team.setSuffix(suffixes == null ? Text.of("") : suffixes.get(0));
                        } else {
                            team = Team.builder()
                                    .name(honorID)
                                    .prefix(prefix)
                                    .suffix(suffixes == null ? Text.of("") : suffixes.get(0))
                                    .allowFriendlyFire(true)
                                    .build();
                            getScoreBoard().registerTeam(team);
                        }
                        team.addMember(p.getTeamRepresentation());
                        if (prefixes.size() > 1) {
                            DisplayHonorTaskManager.submit(honorID, prefixes, suffixes, team, valueData.getDelay());
                        }
                    }
                }
            }
        }
    }

    private static void setPlayerScoreBoard(Player p) {
        try {
            p.setScoreboard(getScoreBoard());
        } catch (NullPointerException e) {
            try {
                p.setScoreboard(getScoreBoard());
            } catch (NullPointerException e2) {
                NewHonor.logger.warn("Something deleted the honor scoreboard!");
            }
        }
    }

    private static Scoreboard getScoreBoard() {
        if (scoreboard == null) {
            synchronized (LOCK) {
                if (scoreboard == null) {
                    NewHonor.logger.warn("The scoreboard was unexpected removed! Building a new one.");
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
