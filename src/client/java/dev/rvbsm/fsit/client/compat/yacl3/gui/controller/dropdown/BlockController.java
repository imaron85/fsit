package dev.rvbsm.fsit.client.compat.yacl3.gui.controller.dropdown;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.dropdown.AbstractDropdownController;
import dev.isxander.yacl3.gui.controllers.dropdown.AbstractDropdownControllerElement;
import dev.rvbsm.fsit.client.compat.yacl3.gui.util.RegistryHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockController extends AbstractDropdownController<String> {

    public BlockController(Option<String> option) {
        super(option);
    }

    @Override
    public String getString() {
        return this.option.pendingValue();
    }

    @Override
    public void setFromString(String input) {
        if (RegistryHelper.isRegistered(BuiltInRegistries.BLOCK, input)) {
            option.requestSet(input);
        }
    }

    @Override
    public boolean isInputValid(String input) {
        return RegistryHelper.isRegistered(BuiltInRegistries.BLOCK, input);
    }

    @Override
    protected String getValidValue(String input, int offset) {
        return RegistryHelper.getMatchingIdentifiers(BuiltInRegistries.BLOCK, input)
            .skip(Math.max(0, offset)) // fixme im sorry? wdym offset can be negative?
            .findFirst()
            .orElseGet(this::getString);
    }

    @Override
    public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> widgetDimension) {
        return new BlockControllerElement(this, screen, widgetDimension);
    }

    public static class BlockControllerElement extends AbstractDropdownControllerElement<String, String> {

        private final BlockController blockController;
        protected Block currentBlock = null;
        protected Map<String, Block> matchingBlocks = new HashMap<>();
//        private float displayTime = 0.0f;

        public BlockControllerElement(BlockController control, YACLScreen screen, Dimension<Integer> widgetDimension) {
            super(control, screen, widgetDimension);
            this.blockController = control;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
//            if (!Screen.hasControlDown()) {
//                this.displayTime += delta;
//            }

            super.render(graphics, mouseX, mouseY, delta);
        }

        @Override
        protected void drawValueText(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            final Dimension<Integer> dimension = this.getDimension();
            this.setDimension(dimension.withWidth(dimension.width() - this.getDecorationPadding()));
            super.drawValueText(graphics, mouseX, mouseY, delta);
            this.setDimension(dimension);

            if (currentBlock != null) {
                graphics.renderFakeItem(
                    new ItemStack(currentBlock.asItem()),
                    dimension.xLimit() - this.getXPadding() - this.getDecorationPadding() + 2,
                    dimension.y() + 2
                );
            }
        }

        @Override
        public List<String> computeMatchingValues() {
            final List<String> ids = RegistryHelper.getMatchingIdentifiers(BuiltInRegistries.BLOCK, this.inputField).toList();
            this.currentBlock = RegistryHelper.getFromId(BuiltInRegistries.BLOCK, this.inputField, null);
            for (final var id : ids) {
                final Block block = RegistryHelper.getFromId(BuiltInRegistries.BLOCK, id, null);
                if (block != null) {
                    this.matchingBlocks.put(id, block);
                }
            }
            return ids;
        }

        @Override
        protected void renderDropdownEntry(GuiGraphics graphics, Dimension<Integer> entryDimension, String identifier) {
            super.renderDropdownEntry(graphics, entryDimension, identifier);
            graphics.renderFakeItem(
                new ItemStack(matchingBlocks.get(identifier)),
                entryDimension.xLimit() - 2,
                entryDimension.y() + 1
            );
        }

        @Override
        public String getString(String identifier) {
            return identifier;
        }

        @Override
        protected int getDecorationPadding() {
            return 16;
        }

        @Override
        protected int getDropdownEntryPadding() {
            return 4;
        }

        @Override
        protected int getControlWidth() {
            return super.getControlWidth() + this.getDecorationPadding();
        }

        @Override
        protected Component getValueText() {
            if (this.inputField.isEmpty() || this.blockController == null) {
                return super.getValueText();
            }

            if (this.inputFieldFocused) {
                return Component.literal(inputField);
            }

            if (this.inputField.charAt(0) != '#') {
                return currentBlock.getName();
            }

            return Component.literal(blockController.option().pendingValue());
        }
    }
}
