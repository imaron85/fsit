package dev.rvbsm.fsit.client.option;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.Codec;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class HybridKeyMapping extends KeyMapping {

    private static final Map<String, HybridKeyMapping> HYBRID = new HashMap<>();

    private final Supplier<Mode> modeGetter;
    private final IntSupplier windowGetter;
    private int toggleTicks = 0;

    public static void resetHybridKeys() {
        for (var hybridKeyMapping : HYBRID.values()) {
            hybridKeyMapping.reset();
        }
    }

    public static void tickAll() {
        for (var hybridKeyMapping : HYBRID.values()) {
            hybridKeyMapping.consumeTick();
        }
    }

    @SuppressWarnings("this-escape")
    public HybridKeyMapping(
        String name,
        int value,
        Category category,
        Supplier<Mode> modeGetter,
        IntSupplier windowGetter
    ) {
        super(name, InputConstants.Type.KEYSYM, value, category);
        this.modeGetter = modeGetter;
        this.windowGetter = windowGetter;
        HYBRID.put(name, this);
    }

    private void consumeTick() {
        if (this.toggleTicks > 0) {
            this.toggleTicks -= 1;
        }
    }

    @Override
    public void setDown(boolean pressed) {
        switch (this.modeGetter.get()) {
            case TOGGLE -> {
                if (pressed) {
                    super.setDown(!this.isDown());
                }
            }
            case HOLD -> super.setDown(pressed);
            case HYBRID -> {
                if (pressed) {
                    super.setDown(!this.isDown());

                    if (this.isDown()) {
                        this.toggleTicks = this.windowGetter.getAsInt();
                    }
                } else {
                    if (this.toggleTicks == 0) {
                        super.setDown(false);
                    }
                }
            }
        }
    }

    @Override
    protected void release() {
        switch (this.modeGetter.get()) {
            case TOGGLE -> super.release();
            case HOLD -> super.setDown(false);
            case HYBRID -> {
                if (this.toggleTicks > 0) {
                    super.release();
                } else {
                    super.setDown(false);
                }
            }
        }
    }

    private void reset() {
        super.setDown(false);
    }

    public enum Mode implements StringRepresentable {
        TOGGLE("toggle", "options.key.toggle"),
        HOLD("hold", "options.key.hold"),
        HYBRID("hybrid", "option.fsit.key.hybrid");

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);
        private final String serializedName;
        private final Component displayName;

        Mode(String serializedName, String key) {
            this.serializedName = serializedName;
            this.displayName = Component.translatable(key);
        }

        @Override
        public String getSerializedName() {
            return this.serializedName;
        }

        public Component getDisplayName() {
            return this.displayName;
        }
    }
}
