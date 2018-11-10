package com.github.euonmyoji.newhonor.command;

import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.configuration.EffectsConfig;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

final class EffectsCommand {
    static final CommandSpec DELETE = CommandSpec.builder()
            .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("effectsID"))))
            .executor((src, args) -> {
                String effectsID = args.<String>getOne(Text.of("effectsID")).orElseThrow(NoSuchFieldError::new);
                Path path = EffectsConfig.getPath(effectsID);
                if (Files.exists(path)) {
                    try {
                        Files.delete(path);
                        src.sendMessage(Text.of("[NewHonor]delete a effects successful"));
                    } catch (IOException e) {
                        NewHonor.logger.warn("delete a effects IOE", e);
                        src.sendMessage(Text.of("[NewHonor]delete a effects successful"));
                    }
                } else {
                    src.sendMessage(Text.of("[NewHonor]unknown effectsID:" + effectsID));
                }
                return CommandResult.success();
            })
            .build();

    static final CommandSpec SET = CommandSpec.builder()
            .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("effectsID"))),
                    GenericArguments.onlyOne(GenericArguments.string(Text.of("effectID"))),
                    GenericArguments.onlyOne(GenericArguments.integer(Text.of("level"))))
            .executor((src, args) -> {
                String effectID = args.<String>getOne(Text.of("effectID")).orElseThrow(NoSuchFieldError::new);
                Optional<PotionEffectType> type = Sponge.getRegistry().getType(PotionEffectType.class, effectID);
                if (type.isPresent()) {
                    String effectsID = args.<String>getOne(Text.of("effectsID")).orElseThrow(NoSuchFieldError::new);
                    int level = args.<Integer>getOne(Text.of("level")).orElseThrow(NoSuchFieldError::new);
                    EffectsConfig ed = new EffectsConfig(effectsID);
                    try {
                        List<String> edArgs = ed.getEffectsList();
                        if (ed.anyMatchType(edArgs, type.get())) {
                            src.sendMessage(Text.of("[NewHonor]the effects is already exist!"));
                        } else if (ed.set(edArgs, type.get().getId() + EffectsConfig.CONNECT_KEY + level)) {
                            src.sendMessage(Text.of("[NewHonor]set the effects effect successful"));
                            return CommandResult.success();
                        } else {
                            src.sendMessage(Text.of("[NewHonor]a error while saving data"));
                        }
                    } catch (ObjectMappingException e) {
                        NewHonor.logger.warn("ObjectMappingException while parse effects |ID:" + effectID);
                        src.sendMessage(Text.of("[NewHonor]ObjectMappingException!"));
                    }
                } else {
                    src.sendMessage(Text.of("[NewHonor]Unknown PotionEffect"));
                }
                src.sendMessage(Text.of("[NewHonor]set failed"));
                return CommandResult.empty();

            })
            .build();

    static final CommandSpec REMOVE = CommandSpec.builder()
            .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("effectsID"))),
                    GenericArguments.onlyOne(GenericArguments.string(Text.of("effectID"))))
            .executor((src, args) -> {
                String effectID = args.<String>getOne(Text.of("effectID")).orElseThrow(NoSuchFieldError::new);
                String effectsID = args.<String>getOne(Text.of("effectsID")).orElseThrow(NoSuchFieldError::new);
                Optional<PotionEffectType> type = Sponge.getRegistry().getType(PotionEffectType.class, effectID);
                if (!type.isPresent()) {
                    src.sendMessage(Text.of("[NewHonor]Unknown PotionEffect"));
                } else if (!Files.exists(EffectsConfig.getPath(effectsID))) {
                    src.sendMessage(Text.of("[NewHonor]effects is not found"));
                } else {
                    EffectsConfig ed = new EffectsConfig(effectsID);
                    try {
                        List<String> list = ed.getEffectsList();
                        if (ed.anyMatchType(list, type.get())) {
                            src.sendMessage(Text.of("[NewHonor]The PotionEffect is not found in that effects"));
                        } else {
                            for (String s : ed.getEffectsList()) {
                                Sponge.getRegistry().getType(PotionEffectType.class, s.split(",", 2)[0])
                                        .filter(type.get()::equals)
                                        .ifPresent(t -> list.remove(s));
                            }
                            if (ed.remove(list)) {
                                src.sendMessage(Text.of("[NewHonor]remove effects " + effectID + " PotionEffects successful"));
                                return CommandResult.success();
                            }
                        }
                    } catch (ObjectMappingException e) {
                        NewHonor.logger.warn("ObjectMappingException while getting EffectsData", e);
                        src.sendMessage(Text.of("[NewHonor]ObjectMappingException while getting EffectsData"));
                    }
                }
                src.sendMessage(Text.of("[NewHonor]remove failed"));
                return CommandResult.empty();
            })
            .build();

    static final CommandSpec INFO = CommandSpec.builder()
            .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("effectsID"))))
            .executor((src, args) -> {
                String effectID = args.<String>getOne(Text.of("effectsID")).orElseThrow(NoSuchFieldError::new);
                if (Files.exists(EffectsConfig.getPath(effectID))) {
                    try {
                        src.sendMessage(Text.of("---------" + effectID + "---------"));
                        new EffectsConfig(effectID).getEffects().forEach(effect ->
                                src.sendMessage(Text.of(String.format("id:%s，name:%s，level:%d",
                                        effect.getType().getId(), effect.getType().getName(), effect.getAmplifier()))));
                        return CommandResult.success();
                    } catch (ObjectMappingException e) {
                        e.printStackTrace();
                        src.sendMessage(Text.of("[NewHonor]error"));
                    }
                } else {
                    src.sendMessage(Text.of("[NewHonor]" + effectID + "is not exist"));
                }
                return CommandResult.empty();
            })
            .build();

    static final CommandSpec LIST_ALL_CREATED_EFFECTS = CommandSpec.builder()
            .executor((src, args) -> {
                PaginationList.Builder builder = PaginationList.builder()
                        .title(Text.of("Created Effects")).padding(Text.of("-"));
                try {
                    builder.contents(EffectsConfig.getCreatedEffects().stream().map(Text::of).collect(Collectors.toList()));
                    builder.build().sendTo(src);
                    return CommandResult.success();
                } catch (IOException e) {
                    NewHonor.logger.warn("IOE when list all created effects", e);
                    src.sendMessage(Text.of("[NewHonor]IOE!"));
                }
                return CommandResult.empty();
            })
            .build();

    static final CommandSpec LIST_ALL_PARTICLE = CommandSpec.builder()
            .executor((src, args) -> {
                PaginationList.Builder builder = PaginationList.builder();
                builder.title(Text.of("Particle ID List")).padding(Text.of("-"));
                builder.contents(Sponge.getRegistry().getAllOf(ParticleType.class)
                        .stream().map(type -> Text.of(type.getId())).collect(Collectors.toList()));
                builder.build().sendTo(src);
                return CommandResult.success();
            })
            .build();

    static final CommandSpec LIST_ALL_POTION_EFFECTS = CommandSpec.builder()
            .executor((src, args) -> {
                PaginationList.Builder builder = PaginationList.builder();
                builder.title(Text.of("PotionEffect List")).padding(Text.of("-")).header(Text.of("(Left is PotionEffect ID)"));
                builder.contents(Sponge.getRegistry().getAllOf(PotionEffectType.class)
                        .stream().map(type -> Text.of(type.getId() + " : " + type.getName())).collect(Collectors.toList()));
                builder.build().sendTo(src);
                return CommandResult.success();
            })
            .build();

    private EffectsCommand() {
        throw new UnsupportedOperationException();
    }
}
