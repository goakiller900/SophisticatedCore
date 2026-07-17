package net.p3pp3rf1y.sophisticatedcore.fluid;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public final class SimpleFluidContent {
	public static final SimpleFluidContent EMPTY = new SimpleFluidContent(FluidStack.EMPTY);
	public static final Codec<SimpleFluidContent> CODEC = FluidStack.OPTIONAL_CODEC.xmap(SimpleFluidContent::new, content -> content.fluidStack);
	public static final StreamCodec<RegistryFriendlyByteBuf, SimpleFluidContent> STREAM_CODEC = FluidStack.OPTIONAL_STREAM_CODEC
			.map(SimpleFluidContent::new, content -> content.fluidStack);

	private final FluidStack fluidStack;

	private SimpleFluidContent(FluidStack fluidStack) {
		this.fluidStack = fluidStack;
	}

	public static SimpleFluidContent copyOf(FluidStack fluidStack) {
		return fluidStack.isEmpty() ? EMPTY : new SimpleFluidContent(fluidStack.copy());
	}

	public FluidStack copy() {
		return fluidStack.copy();
	}

	@Override
	public boolean equals(Object other) {
		return this == other || other instanceof SimpleFluidContent content && FluidStack.matches(fluidStack, content.fluidStack);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fluidStack.getVariant(), fluidStack.getAmount());
	}
}
