package com.github.euonmyoji.newhonor.sponge.api.configuration;

import com.github.euonmyoji.newhonor.sponge.NewHonor;
import com.github.euonmyoji.newhonor.sponge.configuration.HonorConfig;
import com.github.euonmyoji.newhonor.sponge.configuration.PluginConfig;
import com.github.euonmyoji.newhonor.sponge.data.HonorData;
import com.github.euonmyoji.newhonor.sponge.manager.LanguageManager;
import com.github.euonmyoji.newhonor.sponge.manager.PlayerConfigManager;
import com.github.euonmyoji.newhonor.sponge.util.Log;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;

import java.sql.SQLException;
import java.util.*;

import static com.github.euonmyoji.newhonor.sponge.configuration.HonorConfig.getHonorValueData;
import static com.github.euonmyoji.newhonor.sponge.manager.PlayerConfigManager.d;

/**
 * @author yinyangshi
 */
@SuppressWarnings("unused")
public interface PlayerConfig {
    String USING_KEY = "usinghonor";
    String HONORS_KEY = "honors";
    String USEHONOR_KEY = "usehonor";
    String ENABLE_EFFECTS_KEY = "enableeffects";
    String AUTO_CHANGE_KEY = "autochange";

    /**
     * 得到玩家爱数据
     *
     * @param user user对象
     * @return data
     * @throws Exception when found any error
     */
    static PlayerConfig get(User user) throws Exception {
        return get(user.getUniqueId());
    }

    /**
     * 得到玩家数据
     *
     * @param uuid 玩家uuid
     * @return data
     * @throws Exception when found any error
     */
    static PlayerConfig get(UUID uuid) throws Exception {
        return PlayerConfigManager.map.get(getDefaultConfigType()).getConstructor(UUID.class).newInstance(uuid);
    }

    /**
     * 通过type得到玩家数据
     *
     * @param type 那个玩家数据的type
     * @param uuid the uuid of player
     * @return data
     * @throws Exception if any error
     */
    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    static PlayerConfig getOf(String type, UUID uuid) throws Exception {
        return PlayerConfigManager.map.get(type).getConstructor(UUID.class).newInstance(uuid);
    }

    /**
     * 设置玩家数据默认用什么
     *
     * @param type the type of config
     */
    static void setDefaultConfigType(String type) {
        if (!isTypePresent(type)) {
            throw new IllegalArgumentException("The type is not present!");
        }
        d = type;
    }

    /**
     * 获取玩家数据默认类型
     *
     * @return string of type
     */
    static String getDefaultConfigType() {
        return d;
    }

    /**
     * 检查一个数据类型是否存在
     *
     * @param type the type of player config
     * @return true if present
     */
    static boolean isTypePresent(String type) {
        return PlayerConfigManager.map.containsKey(type);
    }

    /**
     * registerPlayerConfigType
     *
     * @param id the id of type
     * @param c  the class
     * @throws NoSuchMethodException if there is no constructor(UUID uuid) or it's not public
     */
    static void registerPlayerConfig(String id, Class<? extends PlayerConfig> c) throws NoSuchMethodException {
        c.getConstructor(UUID.class);
        PlayerConfigManager.map.put(id, c);
    }

    /**
     * 移除一个playerConfig type
     *
     * @param id the player config type
     * @return true if unregistered successfully or false if it is not present
     * @deprecated see <see>unregister</see>
     */
    @Deprecated
    static boolean unregister(String id) {
        if (getDefaultConfigType().equals(id)) {
            PlayerConfigManager.map.keySet().stream().filter(s -> !s.equals(id)).findAny()
                    .ifPresent(s -> {
                        setDefaultConfigType(s);
                        NewHonor.logger.info("[NewHonor]A plugin unregister " + id + ", so now is using ", s + " type to save&load playerdata");
                    });
        }
        return PlayerConfigManager.map.remove(id) != null;
    }

    /**
     * 移除一个playerConfig class
     *
     * @param c the class
     * @return true if unregistered successfully or false if it is not present
     */
    static boolean unregister(Class<? extends PlayerConfig> c) {
        String[] id = new String[1];
        boolean result = PlayerConfigManager.map.entrySet().removeIf(entry -> {
            boolean r = entry.getValue().equals(c);
            if (r) {
                id[0] = entry.getKey();
            }
            return r;
        });
        if (result) {
            if (id[0].equals(getDefaultConfigType())) {
                PlayerConfigManager.map.keySet().stream().findAny()
                        .ifPresent(newId -> {
                            setDefaultConfigType(newId);
                            NewHonor.logger.info("[NewHonor]A plugin unregister " + id[0] + ", so now is using ", newId + " type to save&load playerdata");
                        });
            }
        }
        return result;
    }

    /**
     * 获取已注册的玩家数据class所有typeID
     *
     * @return the ids of player configs
     */
    static Set<String> getRegisteredConfigTypes() {
        return PlayerConfigManager.map.keySet();
    }

    /**
     * 初始化玩家数据
     *
     * @throws SQLException when found any error
     */
    void init() throws SQLException;

    /**
     * 玩家是否使用头衔
     *
     * @return boolean
     * @throws SQLException when found any error
     */
    boolean isUseHonor() throws SQLException;

    /**
     * 移除玩家头衔
     *
     * @param ids 被移除的honorid
     * @return 移除是否成功
     * @throws SQLException when found any error
     */
    boolean takeHonor(String... ids) throws SQLException;

    /**
     * 给予玩家头衔
     *
     * @param id 被给予的头衔id
     * @return 给予+保存是否成功
     * @throws SQLException when found any error
     */
    boolean giveHonor(String id) throws SQLException;

    /**
     * 设置玩家是否使用头衔
     *
     * @param use boolean
     * @throws SQLException when found any error
     */
    void setWhetherUseHonor(boolean use) throws SQLException;

    /**
     * 设置玩家是否使用药水效果
     *
     * @param enable boolean
     * @throws SQLException when found any error
     */
    void setWhetherEnableEffects(boolean enable) throws SQLException;

    /**
     * 玩家是否使用药水效果
     *
     * @return boolean
     * @throws SQLException when found any error
     */
    boolean isEnabledEffects() throws SQLException;

    /**
     * 设置玩家使用的头衔
     *
     * @param id 使用头衔的id
     * @return 设置是否成功
     * @throws SQLException when found any error
     */
    boolean setUseHonor(String id) throws SQLException;

    /**
     * 获得玩家正在使用的头衔id
     *
     * @return honorid
     * @throws SQLException when found any error
     */
    String getUsingHonorID() throws SQLException;

    /**
     * 获得玩家拥有的头衔
     *
     * @return 玩家拥有的头衔
     * @throws SQLException when found any error
     */
    Optional<List<String>> getOwnHonors() throws SQLException;

    /**
     * 玩家获得新头衔后是否自动切换
     *
     * @param auto 是否自动切换
     * @throws SQLException when found any SQL E
     */
    void enableAutoChange(boolean auto) throws SQLException;

    /**
     * 玩家获得新头衔后是否自动切换
     *
     * @return true if yes
     * @throws SQLException when any SQL E
     */
    boolean isEnabledAutoChange() throws SQLException;

    /**
     * 检查玩家拥有的头衔是否有不正确的地方
     *
     * @throws SQLException when found any error
     */
    default void checkUsingHonor() throws SQLException {
        String usingID = getUsingHonorID();
        if (!isOwnHonor(usingID)) {
            Optional<List<String>> list = getOwnHonors();
            if (list.isPresent() && !list.get().isEmpty()) {
                for (String nowHonor : list.get()) {
                    HonorData nowHonorValue = HonorConfig.getHonorValueData(nowHonor).orElse(null);
                    if (nowHonorValue != null) {
                        setUseHonor(list.get().get(0));
                        Sponge.getServer().getPlayer(getUUID()).ifPresent(player -> player.sendMessage(LanguageManager
                                .langBuilder("newhonor.event.changehonorbylose")
                                .replaceName(player)
                                .replaceHonorid(usingID)
                                .replaceHonor(getHonorValueData(usingID).map(HonorData::getStrValue).orElse(""))
                                .replace("%changedhonor%", nowHonorValue.getStrValue())
                                .build()));
                        return;
                    }
                }
            }

            setUseHonor("");
            NewHonor.clearPlayerCache(getUUID());
            Sponge.getServer().getPlayer(getUUID()).ifPresent(player -> player.sendMessage(LanguageManager
                    .langBuilder("newhonor.event.changehonorbylose")
                    .replaceName(player)
                    .replaceHonorid(usingID)
                    .replaceHonor(getHonorValueData(usingID).map(HonorData::getStrValue).orElse(""))
                    .replace("%changedhonor%", "null")
                    .build()));

        }
    }

    /**
     * 检查玩家权限并give/take头衔
     */
    default void checkPermission() {
        Sponge.getServer().getPlayer(getUUID()).ifPresent(player ->
                HonorConfig.getAllCreatedHonors().forEach(id -> {
                    //已经在遍历每一个创建的头衔了！
                    final String checkPrefix = "newhonor.honor.";
                    try {
                        List<String> ownedHonors = getOwnHonors().orElseGet(ArrayList::new);
                        //如果移除没权限的头衔
                        if (PluginConfig.cfg.getNode(PluginConfig.PERMISSION_MANAGE).getBoolean() && !player.hasPermission(checkPrefix + id)) {
                            if (takeHonor(id)) {
                                Log.info(String.format("[Cause:permission not pass]Player %s lost honor: %s", player.getName(), id));
                            }
                        } else if (player.hasPermission(checkPrefix + id) && !ownedHonors.contains(id)) {
                            try {
                                if (giveHonor(id)) {
                                    Log.info(String.format("[Cause:Permission pass]Player %s got an honor: %s", player.getName(), id));
                                }
                            } catch (Exception e) {
                                NewHonor.logger.warn("error about data! (give honor)", e);
                            }
                        }
                    } catch (SQLException e) {
                        NewHonor.logger.warn("SQL E when check player honors!", e);
                    }
                }));
    }

    /**
     * 得到正在使用的头衔text
     *
     * @return text
     * @throws SQLException when found any error
     */
    default Optional<HonorData> getUsingHonorValue() throws SQLException {
        return Optional.ofNullable(getUsingHonorID()).flatMap(HonorConfig::getHonorValueData);
    }

    /**
     * 玩家是否拥有一个头衔
     *
     * @param id 那个头衔id
     * @return true if own
     * @throws SQLException if any Sql E
     */
    default boolean isOwnHonor(String id) throws SQLException {
        return id == null || "".equals(id) || getOwnHonors().orElse(Collections.emptyList()).contains(id);
    }


    /**
     * 得到这个数据主人的uuid
     *
     * @return uuid
     */
    UUID getUUID();
}
