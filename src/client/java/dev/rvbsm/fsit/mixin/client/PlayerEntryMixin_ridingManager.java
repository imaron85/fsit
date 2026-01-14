package dev.rvbsm.fsit.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import dev.rvbsm.fsit.client.FSitModClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.social.PlayerEntry;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

@Mixin(PlayerEntry.class)
public abstract class PlayerEntryMixin_ridingManager extends ContainerObjectSelectionList.Entry<PlayerEntry> {

    @Shadow
    protected abstract MutableComponent getEntryNarationMessage(MutableComponent buttonNarrationMessage);

    @Shadow
    @Final
    private List<AbstractWidget> children;

    @Unique
    private static final WidgetSprites RESTRICT_BUTTON_SPRITES = new WidgetSprites(Identifier.fromNamespaceAndPath("fsit", "social_interactions/restrict_button"), Identifier.fromNamespaceAndPath("fsit", "social_interactions/restrict_button_highlighted"));
    @Unique
    private static final WidgetSprites UNRESTRICT_BUTTON_SPRITES = new WidgetSprites(Identifier.fromNamespaceAndPath("fsit", "social_interactions/allow_button"), Identifier.fromNamespaceAndPath("fsit", "social_interactions/allow_button_highlighted"));

    @Unique
    private @Nullable Button restrictButton;
    @Unique
    private @Nullable Button unrestrictButton;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/social/PlayerEntry;updateHideAndShowButton(Z)V"))
    private void init(CallbackInfo ci, @Local(argsOnly = true) final UUID id) {
        if (FSitModClient.getInstance().isModded()) {
            final var restrictManager = FSitModClient.getInstance().getRestrictionsManager();

            this.restrictButton = new ImageButton(0, 0, 20, 20, RESTRICT_BUTTON_SPRITES, (button) -> {
                restrictManager.restrictPlayer(id);
                this.onRestrictedOrAllowed(true);
            }) {

                @Override
                protected MutableComponent createNarrationMessage() {
                    return PlayerEntryMixin_ridingManager.this.getEntryNarationMessage(super.createNarrationMessage());
                }
            };

            this.unrestrictButton = new ImageButton(0, 0, 20, 20, UNRESTRICT_BUTTON_SPRITES, (button) -> {
                restrictManager.allowPlayer(id);
                this.onRestrictedOrAllowed(false);
            }) {

                @Override
                protected MutableComponent createNarrationMessage() {
                    return PlayerEntryMixin_ridingManager.this.getEntryNarationMessage(super.createNarrationMessage());
                }
            };

            this.children.add(this.restrictButton);
            this.updateRestrictAndAllowButton(restrictManager.isRestricted(id));
        }
    }

    @Inject(method = "renderContent", at = @At("TAIL"))
    private void renderContent(
        CallbackInfo ci,
        @Local(argsOnly = true) final GuiGraphics graphics,
        @Local(ordinal = 0, argsOnly = true) final int mouseX,
        @Local(ordinal = 1, argsOnly = true) final int mouseY,
        @Local(argsOnly = true) final float a
    ) {
        if (this.restrictButton != null && this.unrestrictButton != null) {
            this.restrictButton.setX(this.getContentX() + this.getContentWidth() - this.restrictButton.getWidth() - 4 - 40 - 8);
            this.restrictButton.setY(this.getContentY() + this.getContentHeight() - this.restrictButton.getHeight() / 2);
            this.restrictButton.render(graphics, mouseX, mouseY, a);
            this.unrestrictButton.setX(this.getContentX() + this.getContentWidth() - this.restrictButton.getWidth() - 4 - 40 - 8);
            this.unrestrictButton.setY(this.getContentY() + this.getContentHeight() - this.restrictButton.getHeight() / 2);
            this.unrestrictButton.render(graphics, mouseX, mouseY, a);
        }
    }

    @Unique
    private void onRestrictedOrAllowed(final boolean isRestricted) {
        this.updateRestrictAndAllowButton(isRestricted);
    }

    @Unique
    private void updateRestrictAndAllowButton(final boolean isRestricted) {
        this.restrictButton.visible = isRestricted;
        this.unrestrictButton.visible = !isRestricted;
        final var index = this.children.indexOf(isRestricted ? this.restrictButton : this.unrestrictButton);
        this.children.set(index, isRestricted ? this.unrestrictButton : this.restrictButton);
    }
}
