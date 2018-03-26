package com.github.euonmyoji.newhonor.enums;

import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.effect.potion.PotionEffectTypes;

public enum EffectEnum {
    ABSORPTION(PotionEffectTypes.ABSORPTION),
    BLINDNESS(PotionEffectTypes.BLINDNESS),
    FIRE_RESISTANCE(PotionEffectTypes.FIRE_RESISTANCE),
    GLOWING(PotionEffectTypes.GLOWING),
    HASTE(PotionEffectTypes.HASTE),
    HEALTH_BOOST(PotionEffectTypes.HEALTH_BOOST),
    HUNGER(PotionEffectTypes.HUNGER),
    INSTANT_DAMAGE(PotionEffectTypes.INSTANT_DAMAGE),
    INSTANT_HEALTH(PotionEffectTypes.INSTANT_HEALTH),
    INVISIBILITY(PotionEffectTypes.INVISIBILITY),
    JUMP_BOOST(PotionEffectTypes.JUMP_BOOST),
    LEVITATION(PotionEffectTypes.LEVITATION),
    LUCK(PotionEffectTypes.LUCK),
    MINING_FATIGUE(PotionEffectTypes.MINING_FATIGUE),
    NAUSEA(PotionEffectTypes.NAUSEA),
    NIGHT_VISION(PotionEffectTypes.NIGHT_VISION),
    POISON(PotionEffectTypes.POISON),
    REGENERATION(PotionEffectTypes.REGENERATION),
    RESISTANCE(PotionEffectTypes.RESISTANCE),
    SATURATION(PotionEffectTypes.SATURATION),
    SLOWNESS(PotionEffectTypes.SLOWNESS),
    SPEED(PotionEffectTypes.SPEED),
    STRENGTH(PotionEffectTypes.STRENGTH),
    UNLUCK(PotionEffectTypes.UNLUCK),
    WATER_BREATHING(PotionEffectTypes.WATER_BREATHING),
    WEAKNESS(PotionEffectTypes.WEAKNESS),
    WITHER(PotionEffectTypes.WITHER);

    private PotionEffectType type;

    EffectEnum(PotionEffectType type) {
        this.type = type;
    }
}
