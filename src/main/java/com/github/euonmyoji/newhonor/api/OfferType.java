package com.github.euonmyoji.newhonor.api;

/**
 * @author yinyangshi
 */
public enum OfferType {
    /**
     * Owner拥有这个效果组的的人被offer效果
     */
    Owner,
    /**
     * Halo被光环效果触发 (如果include-me=true 那么Owner也会算作Halo)
     */
    Halo
}
