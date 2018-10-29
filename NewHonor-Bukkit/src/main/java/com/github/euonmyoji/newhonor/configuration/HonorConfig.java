package com.github.euonmyoji.newhonor.configuration;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.data.Honor;
import net.yeah.mungsoup.mung.configuration.MungConfig;

import java.io.IOException;

/**
 * @author MungSoup
 */
public class HonorConfig extends MungConfig {

    public HonorConfig() throws IOException {
        super(NewHonor.instance, "honor.conf");
    }

    boolean notExist(String id) {
        return !config.getNode(id).isVirtual();
    }

    public Honor getHonor(String id) {
        return new Honor(config, id);
    }
}
