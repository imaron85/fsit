package dev.rvbsm.fsit.client.option;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.TranslatableOption;
import net.minecraft.util.Util;

import java.util.function.IntFunction;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class HybridKeyBinding extends KeyBinding {

    private final Supplier<Mode> modeGetter;
    private final LongSupplier windowGetter;
    private long lastTimePressed = 0;

    //? if >=1.21.9 {
    public HybridKeyBinding(String id, int code, Category category, Supplier<Mode> modeGetter, LongSupplier windowGetter) {
    //?} else
    /*public HybridKeyBinding(String id, int code, String category, Supplier<Mode> modeGetter, LongSupplier windowGetter) {*/
        super(id, InputUtil.Type.KEYSYM, code, category);
        this.modeGetter = modeGetter;
        this.windowGetter = windowGetter;
    }

    @Override
    public void setPressed(boolean pressed) {
        // todo test
        switch (this.modeGetter.get()) {
            case TOGGLE -> {
                if (pressed) {
                    super.setPressed(!this.isPressed());
                }
            }
            case HOLD -> super.setPressed(pressed);
            case HYBRID -> {
                final long timePressed = Util.getMeasuringTimeMs() / 50;

                // TODO test bindings
                if (pressed) {
                    super.setPressed(!this.isPressed());

                    this.lastTimePressed = timePressed;
                } else {
                    final boolean isSticky = timePressed - this.lastTimePressed < this.windowGetter.getAsLong();
                    if (!isSticky) {
                        super.setPressed(false);
                    }

                    this.lastTimePressed = 0L;
                }
            }
        }
    }

    public enum Mode implements TranslatableOption {
        TOGGLE("options.key.toggle"), HOLD("options.key.hold"), HYBRID("fsit.option.key.hybrid");

        private static final IntFunction<Mode> ID_MAPPER = new Int2ObjectArrayMap<>(IntStream.range(0, values().length).toArray(), values());
        public static final Codec<Mode> CODEC = Codec.INT.xmap(ID_MAPPER::apply, Mode::ordinal);
        private final String translationKey;

        Mode(final String translationKey) {
            this.translationKey = translationKey;
        }

        @Override
        public int getId() {
            return this.ordinal();
        }

        @Override
        public String getTranslationKey() {
            return this.translationKey;
        }
    }
}
