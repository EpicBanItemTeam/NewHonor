package com.github.euonmyoji.newhonor.configuration;

import com.github.euonmyoji.newhonor.NewHonor;
import net.yeah.mungsoup.mung.configuration.MungConfig;

import java.io.IOException;

/**
 * @author MungSoup
 */
public class HonorConfig extends MungConfig {

    public HonorConfig() throws IOException {
        super(NewHonor.instance, "honor", "conf");
    }

    public boolean isHonorVirtual(String id) {
        return config.getNode(id).isVirtual();
    }
}
