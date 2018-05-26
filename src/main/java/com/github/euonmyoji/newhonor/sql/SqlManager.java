package com.github.euonmyoji.newhonor.sql;

import org.javalite.activejdbc.Base;
import org.spongepowered.api.service.sql.SqlService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author yinyangshi
 */
public class SqlManager {
    private static SqlService sql;
    private static String address = "127.0.0.1";
    private static int port = 3306;
    private static String database = "mysql";
    private static String user = "root";
    private static String password;

    public static void init() {
        Base.open("com.mysql.jdbc.Driver", getURL(), user, password);
        List<String> list = new ArrayList<String>() {{
            add("default");
            add("admin");
        }};
        new NewHonorPlayerData().set(UUID.randomUUID(), "use", "admin", "honors", list, "usehonor", true, "enableeffects", true).saveIt();
    }

    private static String getURL() {
        StringBuilder sb = new StringBuilder().append("jdbc:mysql://").append(address).append(":").append(port)
                .append("/").append(database);
        //append("?") append("user=").append(user) and append("&password=").append(password)
        sb.append("&useUnicode=true&characterEncoding=UTF-8");
        return sb.toString();
    }
}
