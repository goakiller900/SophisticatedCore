package net.p3pp3rf1y.sophisticatedcore.mixin.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.p3pp3rf1y.sophisticatedcore.extensions.client.gui.SophisticatedGuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiGraphicsExtractor.class)
public class GuiGraphicsExtractorMixin implements SophisticatedGuiGraphicsExtractor {
	@Shadow
	@Final
	private GuiRenderState guiRenderState;

	@Override
	public void sophisticatedCore_addPictureInPicture(PictureInPictureRenderState renderState) {
		guiRenderState.addPicturesInPictureState(renderState);
	}
}
