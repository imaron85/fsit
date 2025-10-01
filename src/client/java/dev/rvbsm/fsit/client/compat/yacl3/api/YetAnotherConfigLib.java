package dev.rvbsm.fsit.client.compat.yacl3.api;

import com.mojang.logging.LogUtils;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.rvbsm.fsit.client.FSitModClient;
import dev.rvbsm.fsit.client.compat.yacl3.api.controller.KeyBindingControllerBuilder;
import dev.rvbsm.fsit.client.option.HybridKeyBinding;
import dev.rvbsm.fsit.client.option.ModOptions;
import dev.rvbsm.fsit.config.ConfigBuilder;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.mixin.client.access.KeyBindingAccess;
import dev.rvbsm.fsit.mixin.client.access.SimpleOptionAccess;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

import java.util.Locale;

public final class YetAnotherConfigLib {

    private static final Logger LOGGER = LogUtils.getLogger();

    private YetAnotherConfigLib() {
    }

    public static Screen generateScreen(Screen screen) {
        final ConfigBuilder configBuilder = FSitModClient.getInstance().getConfig().toBuilder();

        return dev.isxander.yacl3.api.YetAnotherConfigLib.createBuilder()
            .title(Text.translatable("fsit.config.title"))

            .category(categoryGeneral(configBuilder))
            .category(categoryUseInteractions(configBuilder))
            .category(categorySneakInteractions(configBuilder))
            .category(categoryCommands(configBuilder))

            .save(() -> save(configBuilder))
            .build()
            .generateScreen(screen);
    }

    public static OptionDescription defaultDescription(final String prefix, final int lines) {
        Validate.notBlank(prefix, "`prefix` must be a valid translation key");
        Validate.inclusiveBetween(0, 99, lines,"`lines` must be in range 1..=99");

        final String translationKey = "%s.description".formatted(prefix);
        final var optionDescription = OptionDescription.createBuilder();
        final var language = Language.getInstance();

        if (lines <= 1 && language.hasTranslation(translationKey)) {
            optionDescription.text(Text.translatable(translationKey));
            return optionDescription.build();
        }

        if (lines > 0) {
            for (int line = 1; line <= lines; line++) {
                final String lineKey = "%s.%d".formatted(translationKey, line);
                optionDescription.text(Text.translatable(lineKey));
            }
        } else for (int line = 1; line <= 99; line++) {
            final String lineKey = "%s.%d".formatted(translationKey, line);
            if (!language.hasTranslation(lineKey)) {
                break;
            }

            optionDescription.text(Text.translatable(lineKey));
        }

        return optionDescription.build();
    }

    public static OptionDescription defaultDescription(final String prefix) {
        return defaultDescription(prefix, 0);
    }

    public static <T> Option.Builder<T> simpleOption(SimpleOption<T> option) {
        return Option.<T>createBuilder()
            .name(((SimpleOptionAccess<T>) (Object) option).getText())
            .binding(
                ((SimpleOptionAccess<T>) (Object) option).getDefaultValue(),
                option::getValue,
                option::setValue);
    }

    private static ConfigCategory categoryGeneral(final ConfigBuilder config) {
        return ConfigCategory.createBuilder()
            .name(Text.translatable("fsit.config.general"))

            .option(Option.<Boolean>createBuilder()
                .name(Text.translatable("fsit.config.general.useServer"))
                .description(defaultDescription("fsit.config.general.useServer"))

                .binding(ConfigData.DEFAULT.useServer(), config::useServer, config::useServer)
                .controller(TickBoxControllerBuilder::create)
                .build())

            .group(OptionGroup.createBuilder()
                .name(Text.translatable("fsit.config.general.sitting"))
                .description(defaultDescription("fsit.config.general.sitting"))

                .option(Option.<ConfigData.Sitting.Behaviour>createBuilder()
                    .name(Text.translatable("fsit.config.general.sitting.behaviour"))
                    .description(defaultDescription("fsit.config.general.sitting.behaviour"))

                    .binding(
                        ConfigData.DEFAULT.sitting().behaviour(),
                        config::sittingBehaviour,
                        config::sittingBehaviour)
                    .controller(o -> EnumControllerBuilder.create(o)
                        .enumClass(ConfigData.Sitting.Behaviour.class)
                        .formatValue((e) -> Text.literal(e.name().toLowerCase(Locale.ROOT)))) // TODO colours
                    .build())
                .option(Option.<Boolean>createBuilder()
                    .name(Text.translatable("fsit.config.general.sitting.shouldCenter"))
                    .description(defaultDescription("fsit.config.general.sitting.shouldCenter"))

                    .binding(
                        ConfigData.DEFAULT.sitting().shouldCenter(),
                        config::sittingShouldCenter,
                        config::sittingShouldCenter)
                    .controller(TickBoxControllerBuilder::create)
                    .build())
                .build())

            .group(OptionGroup.createBuilder()
                .name(Text.translatable("fsit.config.general.keybindings"))
                .description(defaultDescription("fsit.config.general.keybindings"))

                .option(Option.<InputUtil.Key>createBuilder()
                    .name(Text.translatable(ModOptions.sitKey.getId()))
                    .description(defaultDescription(ModOptions.sitKey.getId()))

                    .binding(
                        ModOptions.sitKey.getDefaultKey(),
                        ((KeyBindingAccess) ModOptions.sitKey)::getBoundKey,
                        ModOptions.sitKey::setBoundKey)
                    .controller(KeyBindingControllerBuilder::create)
                    .build())

                .option(Option.<InputUtil.Key>createBuilder()
                    .name(Text.translatable(ModOptions.crawlKey.getId()))
                    .description(defaultDescription(ModOptions.crawlKey.getId()))

                    .binding(
                        ModOptions.crawlKey.getDefaultKey(),
                        ((KeyBindingAccess) ModOptions.crawlKey)::getBoundKey,
                        ModOptions.crawlKey::setBoundKey)
                    .controller(KeyBindingControllerBuilder::create)
                    .build())
                .build())

            .group(OptionGroup.createBuilder()
                .name(Text.translatable("fsit.config.general.keybindingsBehaviour"))
                .description(defaultDescription("fsit.config.general.keybindingsBehaviour"))

                .option(simpleOption(ModOptions.sitMode)
                    .controller(o -> EnumControllerBuilder.create(o).enumClass(HybridKeyBinding.Mode.class))
                    .build())
                .option(simpleOption(ModOptions.crawlMode)
                    .controller(o -> EnumControllerBuilder.create(o).enumClass(HybridKeyBinding.Mode.class))
                    .build())

                .option(simpleOption(ModOptions.hybridModeWindow)
                    .controller(o -> IntegerSliderControllerBuilder.create(o).range(0, 10).step(1))
                    .build())
                .build())
            .build();
    }

    private static ConfigCategory categoryUseInteractions(final ConfigBuilder config) {
        return ConfigCategory.createBuilder()
            .name(Text.translatable("fsit.config.onUse"))

            .option(Option.<Boolean>createBuilder()
                .name(Text.translatable("fsit.config.onUse.sitting"))
                .description(defaultDescription("fsit.config.onUse.sitting"))

                .binding(ConfigData.DEFAULT.onUse().sitting(), config::onUseSitting, config::onUseSitting)
                .controller(TickBoxControllerBuilder::create)
                .build())
            .option(Option.<Boolean>createBuilder()
                .name(Text.translatable("fsit.config.onUse.riding"))
                .description(defaultDescription("fsit.config.onUse.riding"))

                .binding(ConfigData.DEFAULT.onUse().riding(), config::onUseRiding, config::onUseRiding)
                .controller(TickBoxControllerBuilder::create)
                .build())
            .option(Option.<Integer>createBuilder()
                .name(Text.translatable("fsit.config.onUse.range"))
                .description(defaultDescription("fsit.config.onUse.range"))

                .binding(ConfigData.DEFAULT.onUse().range(), config::onUseRange, config::onUseRange)
                .controller(o -> IntegerSliderControllerBuilder.create(o).range(1, 4).step(1))
                .build())
            .option(Option.<Boolean>createBuilder()
                .name(Text.translatable("fsit.config.onUse.checkSuffocation"))
                .description(defaultDescription("fsit.config.onUse.checkSuffocation"))

                .binding(
                    ConfigData.DEFAULT.onUse().checkSuffocation(),
                    config::onUseCheckSuffocation,
                    config::onUseCheckSuffocation)
                .controller(TickBoxControllerBuilder::create)
                .build())

            .group(ListOption.<String>createBuilder()
                .name(Text.translatable("fsit.config.onUse.blocks"))
                .description(defaultDescription("fsit.config.onUse.blocks"))

                .initial("")
                .binding(ConfigData.DEFAULT.onUse().blocks().asList(), config::onUseBlocksList, config::onUseBlocks)
//                .controller(BlockControllerBuilder::create)
                .controller(StringControllerBuilder::create)
                .build())
            .build();
    }

    private static ConfigCategory categorySneakInteractions(final ConfigBuilder config) {
        return ConfigCategory.createBuilder()
            .name(Text.translatable("fsit.config.onSneak"))

            .option(Option.<Boolean>createBuilder()
                .name(Text.translatable("fsit.config.onSneak.sitting"))
                .description(defaultDescription("fsit.config.onSneak.sitting"))

                .binding(ConfigData.DEFAULT.onSneak().sitting(), config::onSneakSitting, config::onSneakSitting)
                .controller(TickBoxControllerBuilder::create)
                .build())
            .option(Option.<Boolean>createBuilder()
                .name(Text.translatable("fsit.config.onSneak.crawling"))
                .description(defaultDescription("fsit.config.onSneak.crawling"))

                .binding(ConfigData.DEFAULT.onSneak().crawling(), config::onSneakCrawling, config::onSneakCrawling)
                .controller(TickBoxControllerBuilder::create)
                .build())
            .option(Option.<Float>createBuilder()
                .name(Text.translatable("fsit.config.onSneak.minPitch"))
                .description(defaultDescription("fsit.config.onSneak.minPitch"))

                .binding(ConfigData.DEFAULT.onSneak().minPitch(), config::onSneakMinPitch, config::onSneakMinPitch)
                .controller((o) -> FloatSliderControllerBuilder.create(o) .range(-90f, 90f) .step(0.1f))
                .build())
            .option(Option.<Integer>createBuilder()
                .name(Text.translatable("fsit.config.onSneak.window"))
                .description(defaultDescription("fsit.config.onSneak.window"))

                .binding(ConfigData.DEFAULT.onSneak().window(), config::onSneakWindow, config::onSneakWindow)
                .controller((o) -> IntegerSliderControllerBuilder.create(o) .range(0, 20) .step(1))
                .build())
            .build();
    }

    private static ConfigCategory categoryCommands(final ConfigBuilder config) {
        return ConfigCategory.createBuilder()
            .name(Text.translatable("fsit.config.commands"))

            .option(Option.<Boolean>createBuilder()
                .name(Text.translatable("fsit.config.commands.root"))
                .description(defaultDescription("fsit.config.commands.root"))

                .binding(ConfigData.DEFAULT.commands().root(), config::commandsRoot, config::commandsRoot)
                .controller(TickBoxControllerBuilder::create)
                .build())
            .option(Option.<Boolean>createBuilder()
                .name(Text.translatable("fsit.config.commands.sit"))
                .description(defaultDescription("fsit.config.commands.sit"))

                .binding(ConfigData.DEFAULT.commands().sit(), config::commandsSit, config::commandsSit)
                .controller(TickBoxControllerBuilder::create)
                .build())
            .option(Option.<Boolean>createBuilder()
                .name(Text.translatable("fsit.config.commands.crawl"))
                .description(defaultDescription("fsit.config.commands.crawl"))

                .binding(ConfigData.DEFAULT.commands().crawl(), config::commandsCrawl, config::commandsCrawl)
                .controller(TickBoxControllerBuilder::create)
                .build())

            .build();
    }

    private static void save(final ConfigBuilder config) {
        FSitModClient.getInstance().setConfig(config.build()).whenComplete((v, e) -> {
            if (e != null) {
                LOGGER.warn("Could not save configuration provided by YACL screen", e);
            }
        });
    }
}
