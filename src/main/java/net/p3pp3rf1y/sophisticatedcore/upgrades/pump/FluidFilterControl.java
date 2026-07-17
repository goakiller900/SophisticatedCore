package net.p3pp3rf1y.sophisticatedcore.upgrades.pump;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.fluid.FluidStack;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.WidgetBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FluidFilterControl extends WidgetBase {
	private final FluidFilterContainer container;
	private final List<Position> slotTopLeftPositions = new ArrayList<>();

	protected FluidFilterControl(Position position, FluidFilterContainer container) {
		super(position, new Dimension(container.getNumberOfFluidFilters() * 18, 18));
		this.container = container;
		for (int i = 0; i < container.getNumberOfFluidFilters(); i++) {
			slotTopLeftPositions.add(new Position(x + i * 18 + 1, y + 1));
		}
	}

	@Override
	protected void extractBg(GuiGraphicsExtractor guiGraphics, Minecraft minecraft, int mouseX, int mouseY) {
		GuiHelper.renderSlotsBackground(guiGraphics, x, y, container.getNumberOfFluidFilters(), 1);
	}

	@Override
	protected void extractWidget(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		for (int i = 0; i < container.getNumberOfFluidFilters(); i++) {
			FluidStack fluid = container.getFluid(i);
			if (!fluid.isEmpty()) {
				ItemStack bucket = new ItemStack(fluid.getFluid().getBucket());
				if (!bucket.isEmpty()) {
					guiGraphics.item(bucket, x + i * 18 + 1, y + 1);
				}
			}
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClicked) {
		if (!isMouseOver(event.x(), event.y())) {
			return false;
		}

		getSlotClicked(event.x(), event.y()).ifPresent(container::slotClick);

		return true;
	}

	@Override
	public void extractTooltip(Screen screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		getSlotClicked(mouseX, mouseY).ifPresent(slot -> {
			FluidStack fluid = container.getFluid(slot);
			if (!fluid.isEmpty()) {
				GuiHelper.extractTooltip(screen, guiGraphics, List.of(fluid.getHoverName()), mouseX, mouseY);
			}
		});
	}

	public List<Position> getSlotTopLeftPositions() {
		return slotTopLeftPositions;
	}

	private Optional<Integer> getSlotClicked(double mouseX, double mouseY) {
		if (mouseY < y + 1 || mouseY >= y + 17) {
			return Optional.empty();
		}
		int index = (int) ((mouseX - x) / 18);
		if (index < 0 || index >= container.getNumberOfFluidFilters()) {
			return Optional.empty();
		}
		return Optional.of(index);
	}

	@Override
	public void updateNarration(NarrationElementOutput narrationElementOutput) {
		//TODO narration
	}

	public void setFluid(int index, FluidStack fluid) {
		container.setFluid(index, fluid);
	}
}
