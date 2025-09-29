package dev.rvbsm.fsit.config;

import dev.rvbsm.fsit.util.registry.BlockSet;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

// TODO configurate does not support snakeyaml comments yet 🫩
@ConfigSerializable
public record ConfigData(
    @Comment("Configuration version. DO NOT EDIT") int version,
    @Comment("Enforce server configuration") boolean useServer,
    Sitting sitting, OnUse onUse, OnSneak onSneak, Commands commands
) {

    public static final String VERSION_KEY = "version";
    public static final int LATEST_VERSION = 4;
    public static final ConfigData DEFAULT = builder().build();

    public ConfigData {
        version = Math.clamp(version, 0, LATEST_VERSION);
    }

    public static ConfigBuilder builder() {
        return new ConfigBuilder();
    }

    public static ConfigBuilder builder(ConfigData config) {
        return new ConfigBuilder(config);
    }

    public ConfigBuilder toBuilder() {
        return builder(this);
    }

    @ConfigSerializable
    public record Sitting(
        @Comment("""
            Specifies seats' behaviour.
            
            Possible values: nothing, discard, gravity (case-insensitive).
            nothing: does nothing.
            discard: removes seats if block underneath was removed.
            gravity: makes seats movable by external sources, e.g. gravity, liquids, pistons, other entities.""")
        Behaviour behaviour,
        @Comment("Adjust seats position to be in the centre of the block.")
        boolean shouldCenter
    ) {

        public enum Behaviour {
            NOTHING, DISCARD, GRAVITY;

            public boolean hasGravity() {
                return this == GRAVITY;
            }

            public boolean requireSupport() {
                return this == DISCARD;
            }
        }
    }

    @ConfigSerializable
    public record OnUse(
        @Comment("Initiate sitting by interaction with blocks.")
        boolean sitting,
        @Comment("Initiate riding by interaction with players.")
        boolean riding,
        @Comment("Maximum distance to an interaction target.")
        int range,
        @Comment("Prevents sitting and riding in places that will result in suffocation.")
        boolean checkSuffocation,
        @Comment("""
            List of blocks and block types available for interaction.
            Identifiers only. Tags are prepended with hash (#) symbol.
            Vanilla identifiers can omit a namespace (e.g, #minecraft:stairs -> #stairs).
            
            Examples: minecraft:oak_slab, minecraft:amethyst_cluster, acacia_fence, #minecraft:logs, #stairs.""")
        BlockSet blocks
    ) {
        public OnUse {
            range = Math.clamp(range, 1, 4);
        }
    }

    @ConfigSerializable
    public record OnSneak(
        @Comment("Initiate sitting by double sneaking.")
        boolean sitting,
        @Comment("Initiate crawling by double sneaking near a one-block gap.")
        boolean crawling,
        @Comment("Minimum pitch angle for double sneak detection.")
        float minPitch,
        @Comment("The window between sneaks (in ticks).")
        int window
    ) {
        public OnSneak {
            minPitch = Math.clamp(minPitch, -90.0f, 90.0f);
            window = Math.clamp(window, 0, 20);
        }
    }

    @ConfigSerializable
    public record Commands(
        @Comment("/fsit and /fsit:client")
        boolean root,
        @Comment("/sit")
        boolean sit,
        @Comment("/crawl")
        boolean crawl
    ) {
    }
}
