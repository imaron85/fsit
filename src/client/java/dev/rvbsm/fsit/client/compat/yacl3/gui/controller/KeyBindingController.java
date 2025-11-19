package dev.rvbsm.fsit.client.compat.yacl3.gui.controller;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.ControllerWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class KeyBindingController implements Controller<InputConstants.Key> {

    private final Option<InputConstants.Key> option;

    public KeyBindingController(Option<InputConstants.Key> option) {
        this.option = option;
    }

    @Override
    public Option<InputConstants.Key> option() {
        return this.option;
    }

    @Override
    public Component formatValue() {
        return this.option.pendingValue().getDisplayName();
    }

    @Override
    public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> dimension) {
        return new KeyBindingControllerWidget(this, screen, dimension);
    }

    private static class KeyBindingControllerWidget extends ControllerWidget<KeyBindingController> {

//        private boolean duplicate = false;

        public KeyBindingControllerWidget(KeyBindingController control, YACLScreen screen, Dimension<Integer> dim) {
            super(control, screen, dim);
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            super.render(graphics, mouseX, mouseY, delta);
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
                    .append(" <").withStyle(ChatFormatting.YELLOW);
            }

            // TODO check for duplicates somehow
//            if (this.duplicate) {
//                return Component.literal("[ ")
//                    .append(super.getValueComponent().copy().formatted(Formatting.WHITE))
//                    .append(" ]").formatted(Formatting.RED);
//            }

            return super.getValueText();
        }

        @Override
        public boolean onMouseClicked(double mouseX, double mouseY, int button) {
            if (this.isMouseOver(mouseX, mouseY)) {
                if (this.isFocused()) {
                    this.control.option.requestSet(InputConstants.Type.MOUSE.getOrCreate(button));
                }

                this.setFocused(!this.isFocused());
                return true;
            }

            return super.onMouseClicked(mouseX, mouseY, button);
        }

        @Override
            //? if >=1.21.9 {
        public boolean keyPressed(net.minecraft.client.input.KeyEvent input) {
            final int keyCode = input.key();
            //?} else
            /*public boolean keyPressed(int keyCode, int scanCode, int modifiers) {*/
            if (this.isFocused()) {
                if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                    this.control.option.requestSet(InputConstants.UNKNOWN);
                } else {
                    //? if >=1.21.9 {
                    this.control.option.requestSet(InputConstants.getKey(input));
                    //?} else
                    /*this.control.option.requestSet(InputConstants.getKey(keyCode, scanCode));*/
                }

                this.setFocused(false);
                return true;
            }

            //? if >=1.21.9 {
            return super.keyPressed(input);
            //?} else
            /*return super.keyPressed(keyCode, scanCode, modifiers);*/
        }
    }
}
