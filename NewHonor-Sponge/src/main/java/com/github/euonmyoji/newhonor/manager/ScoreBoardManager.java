package com.github.euonmyoji.newhonor.manager;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.configuration.PlayerConfig;
import com.github.euonmyoji.newhonor.data.HonorData;
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
    public static boolean enable = false;
    private static Scoreboard scoreboard = Scoreboard.builder().build();
    private static final Object LOCK = new Object();

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
        PlayerConfig pd = PlayerConfig.get(p);
        String honorID = pd.getUsingHonorID();
        if (honorID == null) {
            return;
        }
        synchronized (LOCK) {
            p.getScoreboard().getTeams().forEach(team -> team.removeMember(p.getTeamRepresentation()));

            Optional<Team> optionalTeam = getScoreBoard().getTeam(honorID);
            boolean isTeamPresent = optionalTeam.isPresent();
            if (pd.isUseHonor()) {
                if (NewHonor.plugin.honorTextCache.containsKey(uuid)) {
                    HonorData valueData = NewHonor.plugin.honorTextCache.get(uuid);
                    List<Text> prefixes = valueData.getDisplayValue();
                    Text prefix = prefixes.get(0);
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
                    if (prefixes.size() > 1) {
                        DisplayHonorTaskManager.submit(honorID, prefixes, optionalTeam.orElseThrow(NoSuchFieldError::new), valueData.getDelay());
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

    private ScoreBoardManager() {
        throw new UnsupportedOperationException();
    }
}
