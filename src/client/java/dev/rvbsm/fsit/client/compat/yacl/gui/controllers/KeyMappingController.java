package dev.rvbsm.fsit.client.compat.yacl.gui.controllers;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.ControllerWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public record KeyMappingController(Option<InputConstants.Key> option) implements Controller<InputConstants.Key> {

    @Override
    public Component formatValue() {
        return option.pendingValue().getDisplayName();
    }

    @Override
    public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> widgetDimension) {
        return new KeyMappingControllerElement(this, screen, widgetDimension);
    }

    public static class KeyMappingControllerElement extends ControllerWidget<KeyMappingController> {

        public KeyMappingControllerElement(KeyMappingController control, YACLScreen screen, Dimension<Integer> dim) {
            super(control, screen, dim);
        }

        @Override
        protected int getHoveredControlWidth() {
            return this.getUnhoveredControlWidth();
        }

        @Override
        protected Component getValueText() {
            if (this.isFocused()) {
                return Component.literal("> ")
                    .append(super.getValueText().copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE))
                    .append(" <")
                    .withStyle(ChatFormatting.YELLOW);
            }

            // TODO: collisions
//            if (this.hasCollision) {
//                return Component.literal("[ ")
//                    .append(super.getValueText().copy().withStyle(ChatFormatting.WHITE))
//                    .append(" ]")
//                    .withStyle(ChatFormatting.YELLOW);
//            }

            return super.getValueText();
        }

        @Override
        public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
            if (!this.isMouseOver(event.x(), event.y()) || !this.isAvailable()) {
                return false;
            }

            if (this.isFocused()) {
                this.control.option.requestSet(InputConstants.Type.MOUSE.getOrCreate(event.button()));
            }

            // FIXME: isFocused is a wrong way
            this.setFocused(!this.isFocused());
            return true;
        }

        @Override
        public boolean keyPressed(@NotNull KeyEvent event) {
            if (!this.isFocused()) {
                return false;
            }

            if (event.isEscape()) {
                this.control.option.requestSet(InputConstants.UNKNOWN);
            } else {
                this.control.option.requestSet(InputConstants.getKey(event));
            }

            this.setFocused(false);
            return true;
        }
    }
}
