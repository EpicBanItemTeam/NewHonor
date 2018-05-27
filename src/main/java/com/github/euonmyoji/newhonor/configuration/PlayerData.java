package com.github.euonmyoji.newhonor.configuration;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author yinyangshi
 */
public interface PlayerData {
    /**
     * 得到玩家爱数据
     *
     * @param user user对象
     * @return data
     * @throws Exception when found any error
     */
    static PlayerData get(User user) throws Exception {
        return get(user.getUniqueId());
    }

    /**
     * 得到玩家数据
     *
     * @param uuid 玩家uuid
     * @return data
     * @throws Exception when found any error
     */
    static PlayerData get(UUID uuid) throws Exception {
        return SqlManager.enable ? new SqlManager.SqlPlayerData(uuid) : new LocalPlayerData(uuid);
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
    boolean isEnableEffects() throws SQLException;

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
     * 检查玩家拥有的头衔是否有不正确的地方
     *
     * @throws SQLException when found any error
     */
    void checkUsingHonor() throws SQLException;

    /**
     * 得到正在使用的头衔text
     *
     * @return text
     * @throws SQLException when found any error
     */
    default Optional<Text> getUsingHonorText() throws SQLException {
        return Optional.ofNullable(getUsingHonorID()).flatMap(HonorData::getHonorText);
    }

    /**
     * 得到这个数据主人的uuid
     *
     * @return uuid
     */
    UUID getUUID();
}
