package dev.rvbsm.fsit.client.compat.yacl3.gui.controller;

import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.ControllerWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public final class KeyBindingController implements Controller<InputUtil.Key> {

    private final Option<InputUtil.Key> option;

    public KeyBindingController(Option<InputUtil.Key> option) {
        this.option = option;
    }

    @Override
    public Option<InputUtil.Key> option() {
        return this.option;
    }

    @Override
    public Text formatValue() {
        return this.option.pendingValue().getLocalizedText();
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
        public void render(DrawContext graphics, int mouseX, int mouseY, float delta) {
            super.render(graphics, mouseX, mouseY, delta);
        }

        @Override
        protected int getHoveredControlWidth() {
            return this.getUnhoveredControlWidth();
        }

        @Override
        protected Text getValueText() {
            if (this.isFocused()) {
                return Text.literal("> ")
                    .append(super.getValueText().copy().formatted(Formatting.WHITE, Formatting.UNDERLINE))
                    .append(" <").formatted(Formatting.YELLOW);
            }

            // TODO check for duplicates somehow
//            if (this.duplicate) {
//                return Text.literal("[ ")
//                    .append(super.getValueText().copy().formatted(Formatting.WHITE))
//                    .append(" ]").formatted(Formatting.RED);
//            }

            return super.getValueText();
        }

        @Override
        public boolean onMouseClicked(double mouseX, double mouseY, int button) {
            if (this.isMouseOver(mouseX, mouseY)) {
                if (this.isFocused()) {
                    this.control.option.requestSet(InputUtil.Type.MOUSE.createFromCode(button));
                }

                this.setFocused(!this.isFocused());
                return true;
            }

            return super.onMouseClicked(mouseX, mouseY, button);
        }

        @Override
        //? if >=1.21.9-rc0 {
        public boolean keyPressed(net.minecraft.client.input.KeyInput key) {
            final int keyCode = key.key();
        //?} else
        /*public boolean keyPressed(int keyCode, int scanCode, int modifiers) {*/
            if (this.isFocused()) {
                if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                    this.control.option.requestSet(InputUtil.UNKNOWN_KEY);
                } else {
                    //? if >=1.21.9-rc0 {
                    this.control.option.requestSet(InputUtil.fromKeyCode(key));
                    //?} else
                    /*this.control.option.requestSet(InputUtil.fromKeyCode(keyCode, scanCode));*/
                }

                this.setFocused(false);
                return true;
            }

            //? if >=1.21.9-rc0 {
            return super.keyPressed(key);
            //?} else
            /*return super.keyPressed(keyCode, scanCode, modifiers);*/
        }
    }
}
