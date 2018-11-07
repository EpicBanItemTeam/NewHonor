package com.github.euonmyoji.newhonor.configuration;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.data.Honor;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import net.yeah.mungsoup.mung.configuration.MungConfig;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;

/**
 * @author MungSoup
 */
public class HonorConfig extends MungConfig {

    public HonorConfig() throws IOException {
        super(NewHonor.plugin, "honor.conf");
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void saveDefault() {
        try {
            CommentedConfigurationNode node = cfg.getNode("Baka");
            node.getNode("clickValue", "type").getValue("suggest");
            node.getNode("clickValue", "value").getValue("Baka!");
            node.getNode("displayValue").getList(TypeToken.of(String.class), ImmutableList.of("[&0⑨&r]", "[&1⑨&r]", "[&2⑨&r]", "[&3Baka⑨&r];;10", "[&4⑨&r]",
                    "[&5⑨&r]", "[&6⑨&r]", "[&7⑨&r]", "[&8⑨&r]", "[&9⑨&r]"));
            node.getNode("suffixValue").getList(TypeToken.of(String.class), ImmutableList.of("←Baka", "←Baka", "←Baka", "[&3⑨&r]", "←Baka", "←Baka",
                    "←Baka", "←Baka", "←Baka", "←Baka"));
            node.getNode("hoverValue", "value").getString("&aB&2A&3K&4A!&r\n#Be a baka to get Baka!");
            node.getNode("intervalTicks").getInt(1);
            node.getNode("value").getString("[&bBaka⑨&r]");
            node = node.getNode("item-value");
            node.getNode("Count").getInt(1);
            node.getNode("ItemType").getString("minecraft:ice");
            node.getNode("UnsafeDamage").getInt(0);
            node.getNode("UnsafeData", "display", "Lore").getList(TypeToken.of(String.class), ImmutableList.of("§3冰符「完美冻结」", "§aB§ba§ck§da§6!"));
            node.getNode("UnsafeData", "display", "Name").getString("§3最强の头衔！");
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    public boolean notExist(String id) {
        return cfg.getNode(id).isVirtual();
    }

    public Honor getHonor(String id) {
        return new Honor(cfg, id);
    }
}
