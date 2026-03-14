package dev.rvbsm.fsit.client.gui.screen;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.yacl3.api.Binding;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.EnumDropdownControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.rvbsm.fsit.client.FSitModClient;
import dev.rvbsm.fsit.client.option.HybridKeyMapping;
import dev.rvbsm.fsit.client.option.ModOptions;
import dev.rvbsm.fsit.config.SeatTrait;
import dev.rvbsm.fsit.mixin.client.OptionInstanceAccessor;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Set;

public class YetAnotherConfigLibScreen {

    private static Component caption(OptionInstance<?> option) {
        return ((OptionInstanceAccessor) (Object) option).getCaption();
    }

    private static ConfigCategory general(ModOptions options) {
        return ConfigCategory.createBuilder()
                .group(OptionGroup.createBuilder()
                        .name(ModOptionsScreen.SITTING)
                        .option(Option.<SeatTrait>createBuilder()
                                .name(caption(options.seatsTrait))
                                .description(OptionDescription.of(
                                        ModOptions.SEATS_TRAIT_STATIC_TOOLTIP,
                                        ModOptions.SEATS_TRAIT_ATTACHED_TOOLTIP,
                                        ModOptions.SEATS_TRAIT_MOVEABLE_TOOLTIP))
                                .binding(Binding.minecraft(options.seatsTrait))
                                .controller(EnumDropdownControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(caption(options.seatsCentered))
                                .description(OptionDescription.of(ModOptions.SEATS_CENTERED_TOOLTIP))
                                .binding(Binding.minecraft(options.seatsCentered))
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(ModOptionsScreen.INTERACTION)
                        .option(Option.<Boolean>createBuilder()
                                .name(caption(options.blockInteraction))
                                .description(OptionDescription.of(ModOptions.BLOCK_INTERACTION_TOOLTIP))
                                .binding(Binding.minecraft(options.blockInteraction))
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(caption(options.ridingMinimumYaw))
                                .description(OptionDescription.of(ModOptions.RIDING_MINIMUM_YAW_TOOLTIP))
                                .binding(Binding.minecraft(options.ridingMinimumYaw))
                                .controller(IntegerSliderControllerBuilder::create)
                                .build())
                        .build())
                // TODO: block ids
                .group(OptionGroup.createBuilder()
                        .name(ModOptionsScreen.SNEAK)
                        .option(Option.<Integer>createBuilder()
                                .name(caption(options.sneakMinimumPitch))
                                .description(OptionDescription.of(ModOptions.SNEAK_WINDOW_TOOLTIP))
                                .binding(Binding.minecraft(options.sneakWindow))
                                .controller(IntegerSliderControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(caption(options.sneakMinimumPitch))
                                .description(OptionDescription.of(ModOptions.SNEAK_MINIMUM_PITCH_TOOLTIP))
                                .binding(Binding.minecraft(options.sneakMinimumPitch))
                                .controller(IntegerSliderControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(caption(options.sneakCrawling))
                                .description(OptionDescription.of(ModOptions.SNEAK_CRAWLING_TOOLTIP))
                                .binding(Binding.minecraft(options.sneakCrawling))
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(ModOptionsScreen.CONTROL)
                        .option(Option.<HybridKeyMapping.Mode>createBuilder()
                                .name(caption(options.hybridSit))
                                .binding(Binding.minecraft(options.hybridSit))
                                .controller(EnumDropdownControllerBuilder::create)
                                .build())
                        .option(Option.<HybridKeyMapping.Mode>createBuilder()
                                .name(caption(options.hybridCrawl))
                                .binding(Binding.minecraft(options.hybridCrawl))
                                .controller(EnumDropdownControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(caption(options.hybridCrawl))
                                .binding(Binding.minecraft(options.hybridKeyWindow))
                                .controller(IntegerSliderControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }

    public static Screen generateScreen(Screen screen) {
        var modOptions = FSitModClient.getInstance().getOptions();
        return YetAnotherConfigLib.createBuilder()
            .categories(Set.of(general(modOptions)))
            .build().generateScreen(screen);
    }
}
