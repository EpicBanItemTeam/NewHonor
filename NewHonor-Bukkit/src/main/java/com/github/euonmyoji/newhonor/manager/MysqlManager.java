package com.github.euonmyoji.newhonor.manager;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.api.configuration.BasePlayerConfig;
import com.github.euonmyoji.newhonor.api.configuration.PlayerConfig;
import com.github.euonmyoji.newhonor.enums.ListHonorStyle;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.*;

/**
 * @author yinyangshi
 */
public final class MysqlManager {
    private static final String TABLE_NAME = "NewHonorPlayerData";
    public static boolean enable = false;
    private static String address;
    private static short port;
    private static String database;
    private static String user;
    private static String password;
    private static String update_encoding;

    private MysqlManager() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param node 含有mysql设置的node
     */
    public static void init(CommentedConfigurationNode node) {
        reloadSQLInfo(node);
        node.getNode("enable").setValue(enable);
        node.getNode("address").setValue(address);
        node.getNode("port").setValue(port);
        node.getNode("database").setValue(database);
        node.getNode("user").setValue(user);
        node.getNode("password").setValue(password);
        node.getNode("update-encoding").setValue(update_encoding);
    }

    /**
     * @param node 含有mysql设置的node
     */
    private static void reloadSQLInfo(CommentedConfigurationNode node) {
        enable = node.getNode("enable").getBoolean(false);
        address = node.getNode("address").getString("address");
        port = (short) node.getNode("port").getInt(3306);
        database = node.getNode("database").getString("database");
        user = node.getNode("user").getString("user");
        password = node.getNode("password").getString("password");
        update_encoding = node.getNode("update-encoding").getString("latin1");
        if (enable) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try (Connection con = getConnection()) {
                        try (Statement s = con.createStatement()) {
                            s.execute("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(UUID varchar(36) not null primary key," +
                                    PlayerConfig.USING_KEY + " varchar(64)," +
                                    PlayerConfig.HONORS_KEY + " TEXT," +
                                    PlayerConfig.USEHONOR_KEY + " BOOL DEFAULT 1," +
                                    PlayerConfig.LIST_HONOR_STYLE_KEY + " varchar(32)" +
                                    PlayerConfig.ENABLE_EFFECTS_KEY + " BOOL DEFAULT 1);");
                            try {
                                s.execute(String.format("ALTER TABLE %s ADD %s bool default 1;", TABLE_NAME, PlayerConfig.AUTO_CHANGE_KEY));
                            } catch (Exception ignore) {
                            }
                            try {
                                s.execute(String.format("ALTER TABLE %s ADD %s varchar(32);", PlayerConfig.LIST_HONOR_STYLE_KEY, PlayerConfig.AUTO_CHANGE_KEY));
                            } catch (Exception ignore) {
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(NewHonor.plugin);
        }
    }

    private static String getURL() {
        return String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s&useUnicode=true&characterEncoding=UTF-8",
                address, port, database, user, password);
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getURL());
    }

    public static class MysqlPlayerConfig extends BasePlayerConfig {
        private static final String D = ",";
        private List<SQLException> es = new ArrayList<>();
        private boolean done;

        public MysqlPlayerConfig(UUID uuid) throws SQLException {
            this.uuid = uuid;
            new BukkitRunnable() {
                @Override
                public void run() {
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
                }
            }.runTaskAsynchronously(NewHonor.plugin);
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
            try (PreparedStatement state = getConnection().prepareStatement(String.format("UPDATE NewHonorPlayerData SET %s='%s' WHERE UUID = '%s'", AUTO_CHANGE_KEY, auto ? 1 : 0, uuid))) {
                state.executeUpdate();
            }

        }

        @Override
        public boolean isEnabledAutoChange() throws SQLException {
            try (PreparedStatement state = getConnection().prepareStatement(String.format("select %s from %s where UUID = '%s'", AUTO_CHANGE_KEY, TABLE_NAME, uuid))) {
                ResultSet r = state.executeQuery();
                r.next();
                return r.getByte(AUTO_CHANGE_KEY) >= 1;
            }

        }

        @Override
        public void init() {
//            Optional<List<String>> defaultHonors = PluginConfig.getDefaultOwnHonors();
//            if (defaultHonors.isPresent()) {
//                giveHonor(defaultHonors.get().stream().reduce((s, s2) -> s + D + s2).orElse(""));
//                if (getUsingHonorID() == null) {
//                    setUseHonor(defaultHonors.get().get(0));
//                }
//            } todo: init player
        }

        @Override
        public boolean isUseHonor() throws SQLException {
            try (PreparedStatement state = getConnection().prepareStatement(String.format("select %s from %s where UUID = '%s'", USEHONOR_KEY, TABLE_NAME, uuid))) {
                ResultSet result = state.executeQuery();
                result.next();
                return result.getByte(USEHONOR_KEY) >= 1;
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
                //todo:事件
                try (PreparedStatement state = getConnection().prepareStatement(String.format("UPDATE NewHonorPlayerData SET %s='%s' WHERE UUID = '%s'",
                        HONORS_KEY, honors.get().stream().reduce((s, s2) -> s + D + s2).orElse(""), uuid))) {

                    boolean result = state.executeUpdate() == 1;
                    if (result) {
                        checkUsingHonor();
                    }
                    return result;
                }


            }
            return false;
        }

        @Override
        public boolean giveHonor(String id) throws SQLException {
            //todo:事件
            List<String> honors = getOwnHonors().orElseGet(ArrayList::new);
            try (PreparedStatement state = getConnection().prepareStatement(String.format("UPDATE NewHonorPlayerData SET %s='%s' WHERE UUID = '%s'"
                    , HONORS_KEY, honors.stream().reduce((s, s2) -> s + D + s2).orElse("") + D + id, uuid))) {
                boolean result = state.executeUpdate() < 2;
                if (result && isEnabledAutoChange()) {
                    setUseHonor(id);
                }
                return result;
            }


//            return false;
        }

        @Override
        public void setWhetherUseHonor(boolean use) throws SQLException {
            try (PreparedStatement state = getConnection().prepareStatement(String.format("UPDATE NewHonorPlayerData SET %s='%s' WHERE UUID = '%s'", USEHONOR_KEY, use ? 1 : 0, uuid))) {
                state.executeUpdate();
            }

        }

        @Override
        public void setWhetherEnableEffects(boolean enable) throws SQLException {
            try (PreparedStatement state = getConnection().prepareStatement(String.format("UPDATE NewHonorPlayerData SET %s='%s' WHERE UUID = '%s'", ENABLE_EFFECTS_KEY, enable ? 1 : 0, uuid))) {
                state.executeUpdate();
            }

        }

        @Override
        public boolean isEnabledEffects() throws SQLException {
            try (PreparedStatement state = getConnection().prepareStatement(String.format("select %s from %s where UUID = '%s'", ENABLE_EFFECTS_KEY, TABLE_NAME, uuid))) {
                ResultSet result = state.executeQuery();
                result.next();
                return result.getByte(ENABLE_EFFECTS_KEY) >= 1;
            }

        }

        @Override
        public boolean setUseHonor(String id) {
//            try (PreparedStatement state = getConnection().prepareStatement(String.format("UPDATE NewHonorPlayerData SET %s='%s' WHERE UUID = '%s'", USING_KEY, id, uuid))) {
//                boolean isRight = isOwnHonor(id) && !HonorConfig.isVirtual(id);
//                if ("".equals(id) || isRight) {
//                    return state.executeUpdate() < 2;
//                }
//            }
//             todo:
            return false;
        }

        @Override
        public String getUsingHonorID() throws SQLException {
            try (PreparedStatement state = getConnection().prepareStatement(String.format("select %s from %s where UUID = '%s'", USING_KEY, TABLE_NAME, uuid))) {
                ResultSet result = state.executeQuery();
                result.next();
                return result.getString(USING_KEY);
            }

        }

        @Override
        public ListHonorStyle getListHonorStyle() {
//            try (PreparedStatement state = getConnection().prepareStatement(String.format("select %s from %s where UUID = '%s'", LIST_HONOR_STYLE_KEY, TABLE_NAME, uuid))) {
//                ResultSet result = state.executeQuery();
//                String s;
//                return result.next() && (s = result.getString(LIST_HONOR_STYLE_KEY)) != null ?
//                        ListHonorStyle.valueOf(s.toUpperCase()) : PluginConfig.defaultListHonorStyle();
//            } todo:
            return null;
        }

        @Override
        public void setListHonorStyle(ListHonorStyle style) throws SQLException {
            try (PreparedStatement state = getConnection().prepareStatement(String.format("UPDATE NewHonorPlayerData SET %s='%s' WHERE UUID = '%s'", LIST_HONOR_STYLE_KEY, style.toString(), uuid))) {
                state.executeUpdate();
            }

        }

        @Override
        public Optional<List<String>> getOwnHonors() throws SQLException {
            try (PreparedStatement state = getConnection().prepareStatement(String.format("select %s from %s where UUID = '%s'", HONORS_KEY, TABLE_NAME, uuid))) {
                ResultSet result = state.executeQuery();
                if (result.next()) {
                    String honors = result.getString(HONORS_KEY);
                    return honors == null ? Optional.of(new ArrayList<>()) :
                            Optional.of(honors).map(s -> new ArrayList<>(Arrays.asList(s.split(D))));
                }
            }

            return Optional.empty();
        }
    }
}