package dev.rvbsm.fsit.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CommandConfig(String root, String sit, String crawl, String lay) {

    public static final Codec<CommandConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("root").forGetter(CommandConfig::root),
        Codec.STRING.fieldOf("sit").forGetter(CommandConfig::sit),
        Codec.STRING.fieldOf("crawl").forGetter(CommandConfig::crawl),
        Codec.STRING.fieldOf("lay").forGetter(CommandConfig::lay)
    ).apply(instance, CommandConfig::new));
}
