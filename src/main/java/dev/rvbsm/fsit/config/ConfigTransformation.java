package dev.rvbsm.fsit.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.mojang.logging.LogUtils;
import io.leangen.geantyref.TypeToken;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.configurate.transformation.TransformAction;

import java.util.List;
import java.util.Objects;

public final class ConfigTransformation {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ConfigurationTransformation.Versioned TRANSFORMATION = ConfigurationTransformation
        .versionedBuilder()
        .versionKey(ConfigData.VERSION_KEY)
        .addVersion(1, v1m10(), v1m20(), v1m21(), v1m24())
        .addVersion(2, v2())
        .addVersion(3, v3())
        .addVersion(4, v4())
        .build();

    @CanIgnoreReturnValue
    public static boolean apply(final ConfigurationNode node) throws ConfigurateException {
        final int startVersion = TRANSFORMATION.version(node);
        TRANSFORMATION.apply(node);
        final int finalVersion = TRANSFORMATION.version(node);

        return finalVersion > startVersion;
    }

    private static TransformAction move(final Object... path) {
        return (p, v) -> path;
    }

    private static ConfigurationTransformation v1m10() {
        return ConfigurationTransformation.builder()
            .addAction(NodePath.path("sneak", "enabled"), move("on_double_sneak", "sitting"))
            .addAction(NodePath.path("sneak", "min_angle"), move("on_double_sneak", "min_pitch"))
            .addAction(NodePath.path("sneak", "delay"), move("on_double_sneak", "delay"))
            .addAction(NodePath.path("misc", "ride_players"), move("on_use", "riding"))
            .addAction(NodePath.path("sittable", "blocks"), (path, blocksNode) -> {
                final ConfigurationNode sittableNode = blocksNode.parent();

                final List<String> blocks = Objects.requireNonNullElseGet(blocksNode.getList(String.class), List::of);
                final List<String> tags;
                if (sittableNode != null && sittableNode.hasChild("tags")) {
                    final ConfigurationNode tagsNode = sittableNode.node("tags");
                    tags = Objects.requireNonNullElseGet(tagsNode.getList(String.class), List::of);
                } else {
                    tags = List.of();
                }

                final Iterable<String> materials = Iterables.concat(blocks, Lists.transform(tags, (v) -> '#' + v));
                blocksNode.setList(String.class, ImmutableList.copyOf(materials));

                return new String[]{"on_use", "blocks"};
            })

            .addAction(NodePath.path("sneak", "angle"), move("on_double_sneak", "min_pitch"))
            .addAction(NodePath.path("misc", "riding", "enabled"), move("on_use", "riding"))

            .addAction(NodePath.path("sneak"), TransformAction.remove())
            .addAction(NodePath.path("misc"), TransformAction.remove())
            .build();
    }

    private static ConfigurationTransformation v1m20() {
        return ConfigurationTransformation.builder()
            .addAction(NodePath.path("sittable", "enabled"), move("on_use", "sitting"))
            .addAction(NodePath.path("sittable", "radius"), move("on_use", "range"))
            .addAction(NodePath.path("sittable", "materials"), move("on_use", "blocks"))
            .addAction(NodePath.path("riding", "enabled"), move("on_use", "riding"))

            .addAction(NodePath.path("sittable"), TransformAction.remove())
            .build();
    }

    private static ConfigurationTransformation v1m21() {
        return ConfigurationTransformation.builder()
            .addAction(NodePath.path("sitting", "behaviour"), TransformAction.set(new TypeToken<>() {}, ConfigData.Sitting.Behaviour.GRAVITY))
            .addAction(NodePath.path("sitting", "on_use", "enabled"), move("on_use", "sitting"))
            .addAction(NodePath.path("sitting", "on_use", "range"), move("on_use", "range"))
            .addAction(NodePath.path("sitting", "on_use", "blocks"), move("on_use", "blocks"))
            .addAction(NodePath.path("sitting", "on_double_sneak", "enabled"), move("on_double_sneak", "sitting"))
            .addAction(NodePath.path("sitting", "on_double_sneak", "min_pitch"), move("on_double_sneak", "min_pitch"))
            .addAction(NodePath.path("sitting", "on_double_sneak", "delay"), move("on_double_sneak", "delay"))
            .addAction(NodePath.path("riding", "on_use", "enabled"), move("on_use", "riding"))

            .addAction(NodePath.path("sitting", "seats_gravity"), TransformAction.remove())
            .addAction(NodePath.path("sitting", "on_use"), TransformAction.remove())
            .addAction(NodePath.path("sitting", "on_double_sneak"), TransformAction.remove())
            .addAction(NodePath.path("riding"), TransformAction.remove())
            .build();
    }

    private static ConfigurationTransformation v1m24() {
        return ConfigurationTransformation.builder()
            .addAction(NodePath.path("sitting", "apply_gravity"), (path, gravityNode) -> {
                final ConfigurationNode sittingNode = gravityNode.parent();

                final boolean applyGravity = gravityNode.getBoolean();
                final boolean allowInAir;
                if (sittingNode != null && sittingNode.hasChild("allow_in_air")) {
                    final ConfigurationNode allowInAirNode = sittingNode.node("allow_in_air");
                    allowInAir = allowInAirNode.getBoolean();
                } else {
                    allowInAir = false;
                }

                if (applyGravity) {
                    gravityNode.raw("gravity");
                } else if (!allowInAir) {
                    gravityNode.raw("discard");
                } else {
                    gravityNode.raw("nothing");
                }

                return TransformAction.rename("behaviour").visitPath(path, gravityNode);
            })
            .build();
    }

    private static ConfigurationTransformation v2() {
        return ConfigurationTransformation.builder()
            .addAction(NodePath.path("on_double_sneak"), TransformAction.rename("on_sneak"))
            .build();
    }

    private static ConfigurationTransformation v3() {
        return ConfigurationTransformation.empty();
    }

    private static ConfigurationTransformation v4() {
        return ConfigurationTransformation.builder()
            .addAction(NodePath.path("use_server"), TransformAction.rename("use-server"))
            .addAction(NodePath.path("sitting", "should_center"), TransformAction.rename("should-center"))
            .addAction(NodePath.path("on_use", "check_suffocation"), TransformAction.rename("check-suffocation"))
            .addAction(NodePath.path("on_use"), TransformAction.rename("on-use"))
            .addAction(NodePath.path("on_sneak", "min_pitch"), TransformAction.rename("min-pitch"))
            .addAction(NodePath.path("on_sneak"), TransformAction.rename("on-sneak"))

            .addAction(NodePath.path("on-sneak", "delay"), (path, node) -> {
                final int delayMs = node.getInt();
                node.set(Integer.class, delayMs * 20 / 1_000);

                return new String[]{"on-sneak", "window"};
            })
            .build();
    }
}
