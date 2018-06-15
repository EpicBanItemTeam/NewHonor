package com.github.euonmyoji.newhonor.configuration;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.event.PlayerGetHonorEvent;
import com.github.euonmyoji.newhonor.api.event.PlayerLoseHonorEvent;
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

import static com.github.euonmyoji.newhonor.configuration.NewHonorConfig.cfg;

/**
 * @author yinyangshi
 */
public class SqlManager {
    static boolean enable = false;
    private static final String DATA_TABLE_NAME = "NewHonorPlayerData";
    private static String address;
    private static short port;
    private static String database;
    private static String user;
    private static String password;
    private static String update_encoding;
    private static CommentedConfigurationNode node = cfg.getNode("SQL-settings");
    private static SqlService sql;

    private static final String USING_KEY = "usinghonor";
    private static final String HONORS_KEY = "honors";
    private static final String USEHONOR_KEY = "usehonor";
    private static final String ENABLEEFFECTS_KEY = "enableeffects";

    public static void init() {
        reloadSQLInfo();
        node.getNode("enable").setValue(enable);
        node.getNode("address").setValue(address);
        node.getNode("port").setValue(port);
        node.getNode("database").setValue(database);
        node.getNode("user").setValue(user);
        node.getNode("password").setValue(password);
        node.getNode("update-encoding").setValue(update_encoding);
    }

    static void reloadSQLInfo() {
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
                try (Statement s = getDataSource(getURL()).getConnection().createStatement()) {
                    s.execute("use " + database);
                    s.execute("CREATE TABLE IF NOT EXISTS " + DATA_TABLE_NAME + "(UUID varchar(36) not null primary key," +
                            USING_KEY + " varchar(64)," +
                            HONORS_KEY + " TEXT," +
                            USEHONOR_KEY + " BOOL DEFAULT 1," +
                            ENABLEEFFECTS_KEY + " BOOL DEFAULT 1);");
                } catch (SQLException e) {
                    NewHonor.plugin.logger.warn("SQLException while init newhonor sql", e);
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

    public static class SqlPlayerConfig implements PlayerConfig {
        private static volatile Connection con;
        private static volatile Statement statement;
        private UUID uuid;
        private List<SQLException> es = new ArrayList<>();
        private boolean done;
        private static byte time_out = 0;

        private static final String D = ",";

        static {
            Task.builder().execute(() -> {
                if (time_out >= 0) {
                    time_out--;
                }
                if (time_out == 0) {
                    try {
                        if (con != null && !con.isClosed()) {
                            con.close();
                        }
                    } catch (SQLException ignore) {
                    }
                    try {
                        if (statement != null && !statement.isClosed()) {
                            statement.close();
                        }
                    } catch (SQLException ignore) {
                    }
                }
            }).async().intervalTicks(20).submit(NewHonor.plugin);
        }

        public SqlPlayerConfig(UUID uuid) throws Exception {
            this.uuid = uuid;
            Task.builder().execute(() -> {
                try {
                    try (PreparedStatement preStat = getConnection().prepareStatement("INSERT INTO " + DATA_TABLE_NAME + " (UUID) VALUES('" + uuid + "');")) {
                        preStat.execute();
                    } catch (SQLException ignore) {
                    }
                    getStatement().execute("set names " + update_encoding + ";");
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
                Exception e = new Exception();
                es.forEach(e::addSuppressed);
                throw e;
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
            ResultSet result = getStatement().executeQuery(String.format("select %s from %s where UUID = '%s'", USEHONOR_KEY, DATA_TABLE_NAME, uuid));
            result.next();
            return result.getByte(USEHONOR_KEY) >= 1;
        }

        @Override
        public boolean takeHonor(String... ids) throws SQLException {
            boolean took = false;
            Optional<List<String>> honors = getOwnHonors();
            if (honors.isPresent()) {
                for (String id : ids) {
                    if (honors.get().stream().anyMatch(id::equals)) {
                        honors.get().remove(id);
                        took = true;
                    }
                }
            }
            PlayerLoseHonorEvent event = new PlayerLoseHonorEvent(Cause.builder().append(NewHonor.plugin).build(EventContext.empty()), uuid, ids);
            Sponge.getEventManager().post(event);
            return took && !event.isCancelled() && getStatement().executeUpdate(
                    String.format("UPDATE NewHonorPlayerData SET %s='%s' WHERE UUID = '%s'",
                            HONORS_KEY, honors.get().stream().reduce((s, s2) -> s + D + s2).orElse(""), uuid)) < 2;
        }

        @Override
        public boolean giveHonor(String id) throws SQLException {
            List<String> honors = getOwnHonors().orElseGet(ArrayList::new);
            PlayerGetHonorEvent event = new PlayerGetHonorEvent(Cause.builder().append(NewHonor.plugin).build(EventContext.empty()), uuid, id);
            Sponge.getEventManager().post(event);
            if (honors.contains(id)) {
                event.setCancelled(true);
            }
            if (!event.isCancelled() && HonorConfig.getHonorText(id).isPresent()) {
                Sponge.getServer().getPlayer(uuid).map(Player::getName).ifPresent(name ->
                        HonorConfig.getGetMessage(id, name).ifPresent(Sponge.getServer().getBroadcastChannel()::send));
                return getStatement().executeUpdate(String.format("UPDATE NewHonorPlayerData SET %s='%s' WHERE UUID = '%s'"
                        , HONORS_KEY, honors.stream().reduce((s, s2) -> s + D + s2).orElse("") + D + id, uuid)) < 2;
            }
            return false;
        }

        @Override
        public void setWhetherUseHonor(boolean use) throws SQLException {
            getStatement().executeUpdate(String.format("UPDATE NewHonorPlayerData SET %s='%s' WHERE UUID = '%s'", USEHONOR_KEY, use ? 1 : 0, uuid));
        }

        @Override
        public void setWhetherEnableEffects(boolean enable) throws SQLException {
            getStatement().executeUpdate(String.format("UPDATE NewHonorPlayerData SET %s='%s' WHERE UUID = '%s'", ENABLEEFFECTS_KEY, enable ? 1 : 0, uuid));
        }

        @Override
        public boolean isEnableEffects() throws SQLException {
            ResultSet result = getStatement().executeQuery(String.format("select %s from %s where UUID = '%s'", ENABLEEFFECTS_KEY, DATA_TABLE_NAME, uuid));
            result.next();
            return result.getByte(ENABLEEFFECTS_KEY) >= 1;
        }

        @Override
        public boolean setUseHonor(String id) throws SQLException {
            if (isOwnHonor(id) && HonorConfig.getHonorText(id).isPresent()) {
                return getStatement().executeUpdate(String.format("UPDATE NewHonorPlayerData SET %s='%s' WHERE UUID = '%s'", USING_KEY, id, uuid)) < 2;
            }
            return false;
        }

        @Override
        public String getUsingHonorID() throws SQLException {
            ResultSet result = getStatement().executeQuery(String.format("select %s from %s where UUID = '%s'", USING_KEY, DATA_TABLE_NAME, uuid));
            result.next();
            return result.getString(USING_KEY);
        }

        @Override
        public Optional<List<String>> getOwnHonors() throws SQLException {
            ResultSet result = getStatement().executeQuery(String.format("select %s from %s where UUID = '%s'", HONORS_KEY, DATA_TABLE_NAME, uuid));
            result.next();
            return Optional.ofNullable(result.getString(HONORS_KEY)).map(s -> new ArrayList<>(Arrays.asList(s.split(D))));
        }

        @Override
        public void checkUsingHonor() throws SQLException {
            String usingID = getUsingHonorID();
            if (usingID == null) {
                return;
            }
            if (!isOwnHonor(getUsingHonorID())) {
                Optional<List<String>> list = NewHonorConfig.getDefaultOwnHonors();
                if (list.isPresent()) {
                    setUseHonor(list.get().get(0));
                } else {
                    setUseHonor("");
                }
            }
        }

        @Override
        public UUID getUUID() {
            return uuid;
        }

        private boolean isOwnHonor(String id) throws SQLException {
            return getOwnHonors().orElse(Collections.emptyList()).stream().anyMatch(s -> Objects.equals(s, id));
        }

        private static Connection getConnection() throws SQLException {
            if (con == null || con.isClosed()) {
                con = getDataSource(getURL()).getConnection();
            }
            time_out = 30;
            return con;
        }

        private static Statement getStatement() throws SQLException {
            if (statement == null || statement.isClosed()) {
                statement = getConnection().createStatement();
            }
            time_out = 30;
            return statement;
        }
    }
}