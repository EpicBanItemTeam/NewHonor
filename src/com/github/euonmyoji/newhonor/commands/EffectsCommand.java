package com.github.euonmyoji.newhonor.commands;

import com.github.euonmyoji.newhonor.configuration.EffectsData;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.omg.CORBA.UNKNOWN;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

class EffectsCommand {
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
                if (type.isPresent()) {
                    String effectsID = args.<String>getOne(Text.of("effectsID")).orElseThrow(UNKNOWN::new);
                    int level = args.<Integer>getOne(Text.of("level")).orElseThrow(UNKNOWN::new);
                    EffectsData ed = new EffectsData(effectsID);
                    try {
                        List<String> edArgs = ed.getEffectsList();
                        if (ed.anyMatchType(edArgs, type.get())) {
                            src.sendMessage(Text.of("[头衔插件]该药水效果已存在"));
                        } else if (ed.set(edArgs, type.get().getId() + "," + level)) {
                            src.sendMessage(Text.of("[头衔插件]修改药水效果组成功"));
                            return CommandResult.success();
                        } else {
                            src.sendMessage(Text.of("[头衔插件]保存药水效果组更改事发生错误"));
                        }
                    } catch (ObjectMappingException e) {
                        e.printStackTrace();
                        src.sendMessage(Text.of("[头衔插件]修改时发生错误，详情请看报错。"));
                    }
                } else {
                    src.sendMessage(Text.of("[头衔插件]未知药水效果"));
                }
                src.sendMessage(Text.of("[头衔插件]修改药水效果组失败"));
                return CommandResult.empty();

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
                        List<String> list = ed.getEffectsList();
                        if (ed.anyMatchType(list, type.get())) {
                            src.sendMessage(Text.of("[头衔插件]该药水效果组未发现该药水效果"));
                        } else {
                            for (String s : ed.getEffectsList()) {
                                Sponge.getRegistry().getType(PotionEffectType.class, s.split(",", 2)[0])
                                        .filter(type.get()::equals)
                                        .ifPresent(t -> list.remove(s));
                            }
                            if (ed.remove(list)) {
                                src.sendMessage(Text.of("[头衔插件]移除药水效果组中" + effectID + "药水效果成功"));
                                return CommandResult.success();
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
                                src.sendMessage(Text.of(String.format("id:%s，name:%s，level:%d",
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
                PaginationList.Builder builder = PaginationList.builder();
                builder.title(Text.of("药水效果标")).padding(Text.of("-")).header(Text.of("使用请输入id(全大写)"));
                Sponge.getRegistry().getAllOf(PotionEffectType.class).forEach(type ->
                        builder.contents(Text.of(type.getId() + "：" + type.getName())));
                builder.sendTo(src);
                return CommandResult.success();
            })
            .build();
}
