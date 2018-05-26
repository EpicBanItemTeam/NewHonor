package com.github.euonmyoji.newhonor.configuration;

import com.github.euonmyoji.newhonor.NewHonor;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.text.Text;

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
    private static int port;
    private static String database;
    private static String user;
    private static String password;
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
    }

    static void reloadSQLInfo() {
        CommentedConfigurationNode node = cfg.getNode("SQL-settings");
        enable = node.getNode("enable").getBoolean(false);
        address = node.getNode("address").getString("address");
        port = node.getNode("port").getInt(3306);
        database = node.getNode("database").getString("database");
        user = node.getNode("user").getString("user");
        password = node.getNode("password").getString("password");
        if (enable) {
            Task.builder().execute(() -> {
                try (Statement s = getDataSource(getURL()).getConnection().createStatement()) {
                    s.execute("use " + database);
                    s.execute("CREATE TABLE IF NOT EXISTS " + DATA_TABLE_NAME + "(UUID varchar(36) not null," +
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

    public static class SqlPlayerData implements PlayerData, AutoCloseable {
        private static volatile Connection con;
        private UUID uuid;
        private ResultSet result;
        private SQLException e;
        private boolean done;
        private Statement statement;
        private List<AutoCloseable> closeables = new ArrayList<>();
        private static int time_out = 0;

        static {
            Task.builder().execute(() -> {
                if (time_out > 0) {
                    time_out--;
                }
                try {
                    if (time_out == 0 && con != null && !con.isClosed()) {
                        con.close();
                    }
                } catch (SQLException ignore) {
                }
            }).async().intervalTicks(20).submit(NewHonor.plugin);
        }

        SqlPlayerData(UUID uuid) throws SQLException {
            this.uuid = uuid;
            new Thread(() -> {
                try {
                    try (PreparedStatement statement2 = getConnection().prepareStatement("INSERT INTO " + DATA_TABLE_NAME + " (UUID) VALUES('" + uuid + "');")) {
                        statement2.execute();
                    } catch (Exception ignore) {
                    }
                    PreparedStatement statement = getConnection().prepareStatement("select * from " + DATA_TABLE_NAME + " where UUID = '" + uuid + "'");
                    result = statement.executeQuery();
                    closeables.add(statement);
                    this.statement = getConnection().createStatement();
                    closeables.add(this.statement);
                } catch (SQLException e) {
                    this.e = e;
                }
                done = true;
            }).start();
            while (!done) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignore) {
                }
            }
            if (e != null) {
                throw e;
            }
        }

        @Override
        public void init() throws SQLException {
            Optional<List<String>> defaultHonors = NewHonorConfig.getDefaultOwnHonors();
            if (defaultHonors.isPresent()) {
                giveHonor(defaultHonors.get().stream().reduce("", (s, s2) -> s + "," + s2));
                if (getUsingHonorID() == null) {
                    setUseHonor(defaultHonors.get().get(0));
                }
            }
        }

        @Override
        public boolean isUseHonor() throws SQLException {
            return result.getByte(USEHONOR_KEY) >= 1;
        }

        @Override
        public boolean takeHonor(String... ids) throws SQLException {
            boolean took = false;
            Optional<List<String>> honors = getOwnHonors();
            for (String id : ids) {
                if (honors.isPresent() && honors.get().stream().anyMatch(id::equals)) {
                    honors.get().remove(id);
                    took = true;
                }
            }
            if (took) {
                statement.execute(String.format("UPDATE NewHonorPlayerData SET %s='%s' WHERE UUID = '%s'", HONORS_KEY, honors.get().stream().reduce((s, s2) -> s + "," + s2), uuid));
            }
            return took;
        }

        @Override
        public boolean giveHonor(String id) throws SQLException {
            List<String> honors = getOwnHonors().orElseGet(ArrayList::new);
            return statement.execute(String.format("UPDATE NewHonorPlayerData SET %s='%s' WHERE UUID = '%s'", HONORS_KEY, honors.stream().reduce("", (s, s2) -> s + "," + s2) + "," + id, uuid));
        }

        @Override
        public void setWhetherUseHonor(boolean use) throws SQLException {
            statement.execute(String.format("UPDATE NewHonorPlayerData SET %s='%s' WHERE UUID = '%s'", USEHONOR_KEY, use ? 1 : 0, uuid));
        }

        @Override
        public void setWhetherEnableEffects(boolean enable) throws SQLException {
            statement.execute(String.format("UPDATE NewHonorPlayerData SET %s='%s' WHERE UUID = '%s'", ENABLEEFFECTS_KEY, enable ? 1 : 0, uuid));
        }

        @Override
        public boolean isEnableEffects() throws SQLException {
            return result.getByte(ENABLEEFFECTS_KEY) > 1;
        }

        @Override
        public boolean setUseHonor(String id) throws SQLException {
            if (isOwnHonor(id) && HonorData.getHonorText(id).isPresent()) {
                statement.execute(String.format("UPDATE NewHonorPlayerData SET %s='%s' WHERE UUID = '%s'", USING_KEY, id, uuid));
            }
            return false;
        }

        @Override
        public String getUsingHonorID() throws SQLException {
            return result.getString(USING_KEY);
        }

        @Override
        public Optional<List<String>> getOwnHonors() throws SQLException {
            return Optional.ofNullable(result.getString("honors")).map(s -> new ArrayList<>(Arrays.asList(s.split(","))));
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
        public Optional<Text> getUsingHonorText() {
            return Optional.empty();
        }

        @Override
        public UUID getUUID() {
            return uuid;
        }

        private boolean isOwnHonor(String id) throws SQLException {
            return getOwnHonors().orElse(Collections.emptyList()).stream().anyMatch(s -> Objects.equals(s, id));
        }

        @Override
        public void close() throws Exception {
            for (AutoCloseable closeable : closeables) {
                if (closeable != null) {
                    closeable.close();
                }
            }
        }

        private static Connection getConnection() throws SQLException {
            if (con == null || con.isClosed()) {
                con = getDataSource(getURL()).getConnection();
            }
            time_out = 60;
            return con;
        }
    }
}