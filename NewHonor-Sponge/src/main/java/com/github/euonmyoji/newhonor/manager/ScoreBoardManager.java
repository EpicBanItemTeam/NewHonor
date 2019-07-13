package com.github.euonmyoji.newhonor.manager;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.configuration.PlayerConfig;
import com.github.euonmyoji.newhonor.api.data.HonorData;
import com.github.euonmyoji.newhonor.api.manager.HonorManager;
import com.github.euonmyoji.newhonor.util.Util;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scoreboard.CollisionRules;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.Visibilities;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.sql.SQLException;
import java.util.*;

/**
 * @author yinyangshi
 */
public final class ScoreBoardManager {
    private static final Object LOCK = new Object();
    public static boolean enable = false;

    private ScoreBoardManager() {
        throw new UnsupportedOperationException();
    }

    public static void init() {
        if (enable) {
            refresh();
        }
    }

    /**
     * 清掉scoreboard数据
     */
    public static void clear() {
        DisplayHonorTaskManager.clear();
    }

    /**
     * 更新玩家拥有头衔到scoreboard上 并加入对应队伍
     *
     * @param p 玩家
     * @throws Exception 读取玩家配置发生error
     */
    private static void execute(Player p) throws Exception {
        UUID uuid = p.getUniqueId();
        PlayerConfig pd = PlayerConfig.get(uuid);
        String honorID = pd.getUsingHonorID();
        synchronized (LOCK) {
            List<Team> teams = new ArrayList<>();
            HonorData honorData = honorID == null ? null: Sponge.getServiceManager().provideUnchecked(HonorManager.class).getUsingHonor(uuid);
            Util.getStream(Sponge.getServer().getOnlinePlayers()).map(Player::getScoreboard)
                    .distinct()
                    .forEach(sb -> {
                        sb.getTeams().forEach(team -> team.removeMember(p.getTeamRepresentation()));
                        if (honorID != null) {
                            Optional<Team> optionalTeam = sb.getTeam(honorID);
                            try {
                                if (pd.isUseHonor()) {
                                    if (honorData != null) {
                                        Text prefix = honorData.getDisplayValue().get(0);
                                        Team team;
                                        if (optionalTeam.isPresent()) {
                                            team = optionalTeam.get();
                                            team.setPrefix(prefix);
                                            team.setSuffix(honorData.getSuffixes() == null ? Text.of("") : honorData.getSuffixes().get(0));
                                        } else {
                                            team = Team.builder()
                                                    .name(honorID)
                                                    .prefix(prefix)
                                                    .suffix(honorData.getSuffixes() == null ? Text.of("") : honorData.getSuffixes().get(0))
                                                    .allowFriendlyFire(true)
                                                    .collisionRule(CollisionRules.ALWAYS)
                                                    .canSeeFriendlyInvisibles(false)
                                                    .deathTextVisibility(Visibilities.ALWAYS)
                                                    .color(TextColors.WHITE)
                                                    .displayName(honorData.getDisplayValue().get(0))
                                                    .members(new HashSet<>())
                                                    .build();
                                            sb.registerTeam(team);
                                        }
                                        team.addMember(p.getTeamRepresentation());
                                        teams.add(team);
                                    }
                                }
                            } catch (SQLException e) {
                                NewHonor.logger.warn("error about sql", e);
                            }
                        }
                    });
            if (teams.size() > 0 && honorData.getDisplayValue().size() > 1) {
                DisplayHonorTaskManager.submit(honorID, honorData.getDisplayValue(), honorData.getSuffixes(), teams, honorData.getDelay());
            }
        }
    }

    public static void refresh() {
        for (Player player : Sponge.getServer().getOnlinePlayers()) {
            if (enable) {
                try {
                    execute(player);
                } catch (Exception e) {
                    NewHonor.logger.warn("init player scoreboard error", e);
                }
            }
        }
    }
}
