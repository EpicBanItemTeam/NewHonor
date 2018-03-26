package com.github.euonmyoji.newhonor.commands;

import com.github.euonmyoji.newhonor.configuration.EffectsData;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.omg.CORBA.UNKNOWN;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EffectsCommand {
    static CommandSpec delete = CommandSpec.builder()
            .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("effectsID"))))
            .executor((src, args) -> {
                String effectsID = args.<String>getOne(Text.of("effectsID")).orElseThrow(UNKNOWN::new);
                Path path = EffectsData.getPath(effectsID);
                if (Files.exists(path)) {
                    try {
                        Files.delete(path);
                        src.sendMessage(Text.of("[头衔插件]删除药水效果组成功"));
                    } catch (IOException e) {
                        e.printStackTrace();
                        src.sendMessage(Text.of("[头衔插件]删除时发生IOE"));
                    }
                } else {
                    src.sendMessage(Text.of("[头衔插件]未发现药水效果组" + effectsID));
                }
                return CommandResult.success();
            })
            .build();

    static CommandSpec set = CommandSpec.builder()
            .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("effectsID"))),
                    GenericArguments.onlyOne(GenericArguments.string(Text.of("effectID"))),
                    GenericArguments.onlyOne(GenericArguments.integer(Text.of("level"))))
            .executor((src, args) -> {
                String effectID = args.<String>getOne(Text.of("effectID")).orElseThrow(UNKNOWN::new);
                Optional<PotionEffectType> type = Sponge.getRegistry().getType(PotionEffectType.class, effectID);
                if (!type.isPresent()) {
                    src.sendMessage(Text.of("[头衔插件]未知药水效果"));
                    return CommandResult.empty();
                }
                String effectsID = args.<String>getOne(Text.of("effectsID")).orElseThrow(UNKNOWN::new);
                int level = args.<Integer>getOne(Text.of("level")).orElseThrow(UNKNOWN::new);
                EffectsData ed = new EffectsData(effectsID);
                if (ed.set(type.get(), level)) {
                    src.sendMessage(Text.of("[头衔插件]修改药水效果成功"));
                } else {
                    src.sendMessage(Text.of("[头衔插件]修改药水效果失败，详情请看报错。"));
                }
                return CommandResult.success();

            })
            .build();

    static CommandSpec remove = CommandSpec.builder()
            .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("effectsID"))),
                    GenericArguments.onlyOne(GenericArguments.string(Text.of("effectID"))))
            .executor((src, args) -> {
                String effectID = args.<String>getOne(Text.of("effectID")).orElseThrow(UNKNOWN::new);
                String effectsID = args.<String>getOne(Text.of("effectsID")).orElseThrow(UNKNOWN::new);
                Optional<PotionEffectType> type = Sponge.getRegistry().getType(PotionEffectType.class, effectID);
                if (!type.isPresent()) {
                    src.sendMessage(Text.of("[头衔插件]未知药水效果"));
                } else if (!Files.exists(EffectsData.getPath(effectsID))) {
                    src.sendMessage(Text.of("[头衔插件]未找到该药水效果组"));
                } else {
                    EffectsData ed = new EffectsData(effectsID);
                    try {
                        List<PotionEffect> list = ed.getEffects();
                        for (PotionEffect effect : new ArrayList<>(list)) {
                            if (effect.getType().equals(type.get())) {
                                if (list.remove(effect) && ed.remove(list)) {
                                    src.sendMessage(Text.of("[头衔插件]移除药水效果组中药水效果成功"));
                                    return CommandResult.success();
                                } else {
                                    src.sendMessage(Text.of("[头衔插件]移除药水效果组中药水效果时发生异常"));
                                    break;
                                }
                            }
                        }
                    } catch (ObjectMappingException e) {
                        e.printStackTrace();
                        src.sendMessage(Text.of("[头衔插件]获取数据时发生ObjectMappingException错误"));
                    }
                }
                src.sendMessage(Text.of("[头衔插件]移除药水效果组中药水效果失败"));
                return CommandResult.empty();
            })
            .build();

    static CommandSpec info = CommandSpec.builder()
            .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("effectsID"))))
            .executor((src, args) -> {
                String effectID = args.<String>getOne(Text.of("effectsID")).orElseThrow(UNKNOWN::new);
                if (Files.exists(EffectsData.getPath(effectID))) {
                    try {
                        src.sendMessage(Text.of("---------" + effectID + "---------"));
                        new EffectsData(effectID).getEffects().forEach(effect ->
                                src.sendMessage(Text.of(String.format("id:%s,name:%s,level:%d",
                                        effect.getType().getId(), effect.getType().getName(), effect.getAmplifier()))));
                        return CommandResult.success();
                    } catch (ObjectMappingException e) {
                        e.printStackTrace();
                        src.sendMessage(Text.of("[头衔插件]获取数据时发生异常"));
                    }
                } else {
                    src.sendMessage(Text.of("[头衔插件]药水效果组" + effectID + "不存在"));
                }
                return CommandResult.empty();
            })
            .build();

    static CommandSpec list = CommandSpec.builder()
            .executor((src, args) -> {
                src.sendMessage(Text.of("-----支持药水效果-----"));
                src.sendMessage(Text.of("使用请输入iid(全大写)"));
                Sponge.getRegistry().getAllOf(PotionEffectType.class).forEach(type ->
                        src.sendMessage(Text.of(type.getId() + ':' + type.getName())));
                src.sendMessage(Text.of("---------------------"));
                return CommandResult.success();
            })
            .build();
}
