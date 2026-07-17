/*
 * Code from
 * https://github.com/Fabricators-of-Create/Porting-Lib/blob/1.20.1/modules/base/src/main/java/io/github/fabricators_of_create/porting_lib/util/client/ScrollPanel.java
 * released under GNU LESSER GENERAL PUBLIC LICENSE Version 2.1, February 1999
 */
package net.p3pp3rf1y.sophisticatedcore.client.gui.controls;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.Collections;
import java.util.List;

/**
 * Abstract scroll panel class.
 */
public abstract class ScrollPanel extends AbstractContainerEventHandler implements Renderable, NarratableEntry {
	private final Minecraft client;
	protected final int width;
	protected final int height;
	protected final int top;
	protected final int bottom;
	protected final int right;
	protected final int left;
	private boolean scrolling;
	protected float scrollDistance;
	protected boolean captureMouse = true;
	protected final int border;

	private final int barWidth;
	private final int barLeft;
	private final int bgColorFrom;
	private final int bgColorTo;
	private final int barBgColor;
	private final int barColor;
	private final int barBorderColor;

	/**
	 * @param client the minecraft instance this ScrollPanel should use
	 * @param width the width
	 * @param height the height
	 * @param top the offset from the top (y coord)
	 * @param left the offset from the left (x coord)
	 */
	public ScrollPanel(Minecraft client, int width, int height, int top, int left) {
		this(client, width, height, top, left, 4);
	}

	/**
	 * @param client the minecraft instance this ScrollPanel should use
	 * @param width the width
	 * @param height the height
	 * @param top the offset from the top (y coord)
	 * @param left the offset from the left (x coord)
	 * @param border the size of the border
	 */
	public ScrollPanel(Minecraft client, int width, int height, int top, int left, int border) {
		this(client, width, height, top, left, border, 6);
	}

	/**
	 * @param client the minecraft instance this ScrollPanel should use
	 * @param width the width
	 * @param height the height
	 * @param top the offset from the top (y coord)
	 * @param left the offset from the left (x coord)
	 * @param border the size of the border
	 * @param barWidth the width of the scroll bar
	 */
	public ScrollPanel(Minecraft client, int width, int height, int top, int left, int border, int barWidth) {
		this(client, width, height, top, left, border, barWidth, 0xC0101010, 0xD0101010);
	}

	/**
	 * @param client the minecraft instance this ScrollPanel should use
	 * @param width the width
	 * @param height the height
	 * @param top the offset from the top (y coord)
	 * @param left the offset from the left (x coord)
	 * @param border the size of the border
	 * @param barWidth the width of the scroll bar
	 * @param bgColor the color for the background
	 */
	public ScrollPanel(Minecraft client, int width, int height, int top, int left, int border, int barWidth, int bgColor)
	{
		this(client, width, height, top, left, border, barWidth, bgColor, bgColor);
	}

	/**
	 * @param client the minecraft instance this ScrollPanel should use
	 * @param width the width
	 * @param height the height
	 * @param top the offset from the top (y coord)
	 * @param left the offset from the left (x coord)
	 * @param border the size of the border
	 * @param barWidth the width of the scroll bar
	 * @param bgColorFrom the start color for the background gradient
	 * @param bgColorTo the end color for the background gradient
	 */
	public ScrollPanel(Minecraft client, int width, int height, int top, int left, int border, int barWidth, int bgColorFrom, int bgColorTo) {
		this(client, width, height, top, left, border, barWidth, bgColorFrom, bgColorTo, 0xFF000000, 0xFF808080, 0xFFC0C0C0);
	}

	/**
	 * Base constructor
	 *
	 * @param client the minecraft instance this ScrollPanel should use
	 * @param width the width
	 * @param height the height
	 * @param top the offset from the top (y coord)
	 * @param left the offset from the left (x coord)
	 * @param border the size of the border
	 * @param barWidth the width of the scroll bar
	 * @param bgColorFrom the start color for the background gradient
	 * @param bgColorTo the end color for the background gradient
	 * @param barBgColor the color for the scroll bar background
	 * @param barColor the color for the scroll bar handle
	 * @param barBorderColor the border color for the scroll bar handle
	 */
	public ScrollPanel(Minecraft client, int width, int height, int top, int left, int border, int barWidth, int bgColorFrom, int bgColorTo, int barBgColor, int barColor, int barBorderColor) {
		this.client = client;
		this.width = width;
		this.height = height;
		this.top = top;
		this.left = left;
		this.bottom = height + this.top;
		this.right = width + this.left;
		this.barLeft = this.left + this.width - barWidth;
		this.border = border;
		this.barWidth = barWidth;
		this.bgColorFrom = bgColorFrom;
		this.bgColorTo = bgColorTo;
		this.barBgColor = barBgColor;
		this.barColor = barColor;
		this.barBorderColor = barBorderColor;
	}

	protected abstract int getContentHeight();

	/**
	 * Draws the background of the scroll panel. This runs AFTER Scissors are enabled.
	 */
	protected void drawBackground(GuiGraphicsExtractor guiGraphics, float partialTick) {
		Screen.extractMenuBackgroundTexture(guiGraphics, Screen.MENU_BACKGROUND, this.left, this.top, 0f, 0f, this.width, this.height);
	}

	/**
	 * Draw anything special on the screen. Scissor (RenderSystem.enableScissor) is enabled
	 * for anything that is rendered outside the view box. Do not mess with Scissor unless you support this.
	 */
	protected abstract void drawPanel(GuiGraphicsExtractor graphics, int entryRight, int relativeY, int mouseX, int mouseY);

	protected boolean clickPanel(double mouseX, double mouseY, int button) { return false; }

	private int getMaxScroll()
	{
		return this.getContentHeight() - (this.height - this.border);
	}

	private void applyScrollLimits() {
		int max = getMaxScroll();

		if (max < 0) {
			max /= 2;
		}

		if (this.scrollDistance < 0.0F) {
			this.scrollDistance = 0.0F;
		}

		if (this.scrollDistance > max) {
			this.scrollDistance = max;
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (scrollY != 0) {
			this.scrollDistance += -scrollY * getScrollAmount();
			applyScrollLimits();
			return true;
		}
		return false;
	}

	protected int getScrollAmount() {
		return 20;
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= this.left && mouseX <= this.left + this.width &&
				mouseY >= this.top && mouseY <= this.bottom;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClicked) {
		if (super.mouseClicked(event, doubleClicked))
			return true;
		double mouseX = event.x();
		double mouseY = event.y();
		int button = event.button();

		this.scrolling = button == 0 && mouseX >= barLeft && mouseX < barLeft + barWidth;
		if (this.scrolling) {
			return true;
		}
		int mouseListY = ((int)mouseY) - this.top - this.getContentHeight() + (int)this.scrollDistance - border;
		if (mouseX >= left && mouseX <= right && mouseListY < 0) {
			return this.clickPanel(mouseX - left, mouseY - this.top + (int)this.scrollDistance - border, button);
		}
		return false;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (super.mouseReleased(event))
			return true;
		boolean ret = this.scrolling;
		this.scrolling = false;
		return ret;
	}

	private int getBarHeight() {
		int barHeight = (height * height) / this.getContentHeight();

		if (barHeight < 32) barHeight = 32;

		if (barHeight > height - border*2)
			barHeight = height - border*2;

		return barHeight;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
		if (this.scrolling) {
			int maxScroll = height - getBarHeight();
			double moved = deltaY / maxScroll;
			this.scrollDistance += getMaxScroll() * moved;
			applyScrollLimits();
			return true;
		}
		return false;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.enableScissor(left, top, right, bottom);

		this.drawBackground(graphics, partialTick);

		int baseY = this.top + border - (int)this.scrollDistance;
		this.drawPanel(graphics, right, baseY, mouseX, mouseY);

		int extraHeight = (this.getContentHeight() + border) - height;
		if (extraHeight > 0) {
			int barHeight = getBarHeight();

			int barTop = (int)this.scrollDistance * (height - barHeight) / extraHeight + this.top;
			if (barTop < this.top) {
				barTop = this.top;
			}

			graphics.fill(barLeft, this.top, barLeft + barWidth, this.bottom, barBgColor);
			graphics.fill(barLeft, barTop, barLeft + barWidth, barTop + barHeight, barColor);
			graphics.outline(barLeft, barTop, barWidth, barHeight, barBorderColor);
		}

		graphics.disableScissor();
	}

	protected void drawGradientRect(GuiGraphicsExtractor guiGraphics, int left, int top, int right, int bottom, int startColor, int endColor) {
		guiGraphics.fillGradient(left, top, right, bottom, startColor, endColor);
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return Collections.emptyList();
	}
}
