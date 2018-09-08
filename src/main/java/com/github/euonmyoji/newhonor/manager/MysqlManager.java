package com.github.euonmyoji.newhonor.manager;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.configuration.BasePlayerConfig;
import com.github.euonmyoji.newhonor.api.event.PlayerGetHonorEvent;
import com.github.euonmyoji.newhonor.api.event.PlayerLoseHonorEvent;
import com.github.euonmyoji.newhonor.configuration.HonorConfig;
import com.github.euonmyoji.newhonor.configuration.NewHonorConfig;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.sql.SqlService;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

import static com.github.euonmyoji.newhonor.api.configuration.PlayerConfig.*;
import static com.github.euonmyoji.newhonor.configuration.NewHonorConfig.cfg;

/**
 * @author yinyangshi
 */
public final class MysqlManager {
    public static boolean enable = false;
    private static final String TABLE_NAME = "NewHonorPlayerData";
    private static String address;
    private static short port;
    private static String database;
    private static String user;
    private static String password;
    private static String update_encoding;
    private static SqlService sql;

    public static void init() {
        reloadSQLInfo();
        CommentedConfigurationNode node = cfg.getNode("SQL-settings");
        node.getNode("enable").setValue(enable);
        node.getNode("address").setValue(address);
        node.getNode("port").setValue(port);
        node.getNode("database").setValue(database);
        node.getNode("user").setValue(user);
        node.getNode("password").setValue(password);
        node.getNode("update-encoding").setValue(update_encoding);
    }

    public static void reloadSQLInfo() {
        CommentedConfigurationNode node = cfg.getNode("SQL-settings");
        enable = node.getNode("enable").getBoolean(false);
        address = node.getNode("address").getString("address");
        port = (short) node.getNode("port").getInt(3306);
        database = node.getNode("database").getString("database");
        user = node.getNode("user").getString("user");
        password = node.getNode("password").getString("password");
        update_encoding = node.getNode("update-encoding").getString("latin1");
        if (enable) {
            Task.builder().execute(() -> {
                try (Connection con = getConnection()) {
                    try (Statement s = con.createStatement()) {
                        s.execute("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(UUID varchar(36) not null primary key," +
                                USING_KEY + " varchar(64)," +
                                HONORS_KEY + " TEXT," +
                                USEHONOR_KEY + " BOOL DEFAULT 1," +
                                ENABLE_EFFECTS_KEY + " BOOL DEFAULT 1);");
                        try {
                            s.execute(String.format("ALTER TABLE %s ADD %s bool default 1;", TABLE_NAME, AUTO_CHANGE_KEY));
                        } catch (Exception ignore) {
                        }
                    }
                } catch (SQLException e) {
                    NewHonor.logger.warn("SQLException while init newhonor sql", e);
                }
            }).async().submit(NewHonor.plugin);
        }
    }

    private static DataSource getDataSource(String jdbcUrl) throws SQLException {
        if (sql == null) {
            sql = Sponge.getServiceManager().provide(SqlService.class).orElseThrow(NoSuchFieldError::new);
        }
        return sql.getDataSource(jdbcUrl);
    }

    private static String getURL() {
        return String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s&useUnicode=true&characterEncoding=UTF-8",
                address, port, database, user, password);
    }

    public static class MysqlPlayerConfig extends BasePlayerConfig {
        private List<SQLException> es = new ArrayList<>();
        private boolean done;

        private static final String D = ",";

        public MysqlPlayerConfig(UUID uuid) throws SQLException {
            this.uuid = uuid;
            Task.builder().execute(() -> {
                try (Connection con = getConnection()) {
                    try (PreparedStatement preStat = con.prepareStatement("INSERT INTO " + TABLE_NAME + " (UUID) VALUES('" + uuid + "');")) {
                        preStat.execute();
                    } catch (SQLException ignore) {
                    }
                    try (PreparedStatement state = con.prepareStatement("set names " + update_encoding + ";")) {
                        state.execute();
                    }
                } catch (SQLException e) {
                    es.add(e);
                }
                done = true;
            }).async().submit(NewHonor.plugin);
            while (!done) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignore) {
                }
            }
            if (es.size() > 0) {
                SQLException e = new SQLException();
                es.forEach(e::addSuppressed);
                throw e;
            }
        }

        @Override
        public void enableAutoChange(boolean auto) throws SQLException {
            try (Connection con = getConnection()) {
                try (PreparedStatement state = con.prepareStatement(String.format("UPDATE NewHonorPlayerData SET %s='%s' WHERE UUID = '%s'", AUTO_CHANGE_KEY, auto ? 1 : 0, uuid))) {
                    state.executeUpdate();
                }
            }
        }

        @Override
        public boolean isEnabledAutoChange() throws SQLException {
            try (Connection con = getConnection()) {
                try (PreparedStatement state = con.prepareStatement(String.format("select %s from %s where UUID = '%s'", AUTO_CHANGE_KEY, TABLE_NAME, uuid))) {
                    ResultSet r = state.executeQuery();
                    r.next();
                    return r.getByte(AUTO_CHANGE_KEY) >= 1;
                }
            }
        }

        @Override
        public void init() throws SQLException {
            Optional<List<String>> defaultHonors = NewHonorConfig.getDefaultOwnHonors();
            if (defaultHonors.isPresent()) {
                giveHonor(defaultHonors.get().stream().reduce((s, s2) -> s + D + s2).orElse(""));
                if (getUsingHonorID() == null) {
                    setUseHonor(defaultHonors.get().get(0));
                }
            }
        }

        @Override
        public boolean isUseHonor() throws SQLException {
            try (Connection con = getConnection()) {
                try (PreparedStatement state = con.prepareStatement(String.format("select %s from %s where UUID = '%s'", USEHONOR_KEY, TABLE_NAME, uuid))) {
                    ResultSet result = state.executeQuery();
                    result.next();
                    return result.getByte(USEHONOR_KEY) >= 1;
                }
            }
        }

        @Override
        public boolean takeHonor(String... ids) throws SQLException {
            boolean took = false;
            Optional<List<String>> honors = getOwnHonors();
            if (honors.isPresent()) {
                for (String id : ids) {
                    if (honors.get().remove(id)) {
                        took = true;
                    }
                }
            }
            if (took) {
                PlayerLoseHonorEvent event = new PlayerLoseHonorEvent(Cause.builder().append(NewHonor.plugin).build(EventContext.empty()), uuid, ids);
                if (!Sponge.getEventManager().post(event)) {
                    try (Connection con = getConnection()) {
                        try (PreparedStatement state = con.prepareStatement(String.format("UPDATE NewHonorPlayerData SET %s='%s' WHERE UUID = '%s'",
                                HONORS_KEY, honors.get().stream().reduce((s, s2) -> s + D + s2).orElse(""), uuid))) {

                            boolean result = state.executeUpdate() == 1;
                            if (result) {
                                checkUsingHonor();
                            }
                            return result;
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public boolean giveHonor(String id) throws SQLException {
            List<String> honors = getOwnHonors().orElseGet(ArrayList::new);
            PlayerGetHonorEvent event = new PlayerGetHonorEvent(Cause.builder().append(NewHonor.plugin).build(EventContext.empty()), uuid, id);
            Sponge.getEventManager().post(event);
            if (honors.contains(id)) {
                event.setCancelled(true);
            }
            if (!event.isCancelled() && !HonorConfig.isVirtual(id)) {
                try (Connection con = getConnection()) {
                    Sponge.getServer().getPlayer(uuid).map(Player::getName).ifPresent(name ->
                            HonorConfig.getGetMessage(id, name).ifPresent(Sponge.getServer().getBroadcastChannel()::send));
                    try (PreparedStatement state = con.prepareStatement(String.format("UPDATE NewHonorPlayerData SET %s='%s' WHERE UUID = '%s'"
                            , HONORS_KEY, honors.stream().reduce((s, s2) -> s + D + s2).orElse("") + D + id, uuid))) {
                        boolean result = state.executeUpdate() < 2;
                        if (result && isEnabledAutoChange()) {
                            setUseHonor(id);
                        }
                        return result;
                    }
                }
            }
            return false;
        }

        @Override
        public void setWhetherUseHonor(boolean use) throws SQLException {
            try (Connection con = getConnection()) {
                try (PreparedStatement state = con.prepareStatement(String.format("UPDATE NewHonorPlayerData SET %s='%s' WHERE UUID = '%s'", USEHONOR_KEY, use ? 1 : 0, uuid))) {
                    state.executeUpdate();
                }
            }
        }

        @Override
        public void setWhetherEnableEffects(boolean enable) throws SQLException {
            try (Connection con = getConnection()) {
                try (PreparedStatement state = con.prepareStatement(String.format("UPDATE NewHonorPlayerData SET %s='%s' WHERE UUID = '%s'", ENABLE_EFFECTS_KEY, enable ? 1 : 0, uuid))) {
                    state.executeUpdate();
                }
            }
        }

        @Override
        public boolean isEnabledEffects() throws SQLException {
            try (Connection con = getConnection()) {
                try (PreparedStatement state = con.prepareStatement(String.format("select %s from %s where UUID = '%s'", ENABLE_EFFECTS_KEY, TABLE_NAME, uuid))) {
                    ResultSet result = state.executeQuery();
                    result.next();
                    return result.getByte(ENABLE_EFFECTS_KEY) >= 1;
                }
            }
        }

        @Override
        public boolean setUseHonor(String id) throws SQLException {
            try (Connection con = getConnection()) {
                try (PreparedStatement state = con.prepareStatement(String.format("UPDATE NewHonorPlayerData SET %s='%s' WHERE UUID = '%s'", USING_KEY, id, uuid))) {
                    boolean isRight = isOwnHonor(id) && !HonorConfig.isVirtual(id);
                    if ("".equals(id) || isRight) {
                        return state.executeUpdate() < 2;
                    }
                }
            }
            return false;
        }

        @Override
        public String getUsingHonorID() throws SQLException {
            try (Connection con = getConnection()) {
                try (PreparedStatement state = con.prepareStatement(String.format("select %s from %s where UUID = '%s'", USING_KEY, TABLE_NAME, uuid))) {
                    ResultSet result = state.executeQuery();
                    result.next();
                    return result.getString(USING_KEY);
                }
            }
        }

        @Override
        public Optional<List<String>> getOwnHonors() throws SQLException {
            try (Connection con = getConnection()) {
                try (PreparedStatement state = con.prepareStatement(String.format("select %s from %s where UUID = '%s'", HONORS_KEY, TABLE_NAME, uuid))) {
                    ResultSet result = state.executeQuery();
                    if (result.next()) {
                        String honors = result.getString(HONORS_KEY);
                        return honors == null ? Optional.of(new ArrayList<>()) :
                                Optional.of(honors).map(s -> new ArrayList<>(Arrays.asList(s.split(D))));
                    }
                }
            }
            return Optional.empty();
        }
    }

    private static Connection getConnection() throws SQLException {
        return getDataSource(getURL()).getConnection();
    }

    private MysqlManager() {
        throw new UnsupportedOperationException();
    }
}