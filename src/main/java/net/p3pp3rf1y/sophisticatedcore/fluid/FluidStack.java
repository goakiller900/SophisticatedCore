package net.p3pp3rf1y.sophisticatedcore.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;

import java.util.Objects;
import java.util.Optional;

/** Amount-bearing fluid value backed by Fabric's component-aware {@link FluidVariant}. */
public final class FluidStack {
	private static final Codec<Long> POSITIVE_AMOUNT = Codec.LONG.validate(amount -> amount > 0
			? DataResult.success(amount)
			: DataResult.error(() -> "Fluid amount must be positive"));
	public static final FluidStack EMPTY = new FluidStack(FluidVariant.blank(), 0);
	public static final Codec<FluidStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			FluidVariant.CODEC.fieldOf("variant").forGetter(FluidStack::getVariant),
			POSITIVE_AMOUNT.fieldOf("amount").forGetter(FluidStack::getAmount)
	).apply(instance, FluidStack::new));
	public static final Codec<FluidStack> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC)
			.xmap(value -> value.orElse(EMPTY), stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack));
	public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> OPTIONAL_STREAM_CODEC = new StreamCodec<>() {
		@Override
		public FluidStack decode(RegistryFriendlyByteBuf buffer) {
			long amount = buffer.readVarLong();
			return amount <= 0 ? EMPTY : new FluidStack(FluidVariant.PACKET_CODEC.decode(buffer), amount);
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buffer, FluidStack stack) {
			buffer.writeVarLong(stack.getAmount());
			if (!stack.isEmpty()) {
				FluidVariant.PACKET_CODEC.encode(buffer, stack.variant);
			}
		}
	};

	private final FluidVariant variant;
	private long amount;

	public FluidStack(Fluid fluid, long amount) {
		this(FluidVariant.of(fluid), amount);
	}

	public FluidStack(FluidVariant variant, long amount) {
		this.variant = variant;
		this.amount = amount;
	}

	public FluidStack(StorageView<FluidVariant> view) {
		this(view.getResource(), view.getAmount());
	}

	public FluidStack(ResourceAmount<FluidVariant> resourceAmount) {
		this(resourceAmount.resource(), resourceAmount.amount());
	}

	public boolean isEmpty() {
		return this == EMPTY || variant.isBlank() || amount <= 0;
	}

	public Fluid getFluid() {
		return isEmpty() ? Fluids.EMPTY : variant.getFluid();
	}

	public Holder<Fluid> getFluidHolder() {
		return getFluid().builtInRegistryHolder();
	}

	public FluidVariant getVariant() {
		return isEmpty() ? FluidVariant.blank() : variant;
	}

	public long getAmount() {
		return isEmpty() ? 0 : amount;
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}

	public FluidStack copy() {
		return isEmpty() ? EMPTY : new FluidStack(variant, amount);
	}

	public FluidStack copyWithAmount(long amount) {
		return isEmpty() ? EMPTY : new FluidStack(variant, amount);
	}

	public boolean is(TagKey<Fluid> tag) {
		return getFluidHolder().is(tag);
	}

	public Component getHoverName() {
		ItemStack bucket = new ItemStack(getFluid().getBucket());
		return bucket.getHoverName();
	}

	public Tag saveOptional(HolderLookup.Provider provider) {
		return isEmpty() ? new CompoundTag() : CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
	}

	public static FluidStack parseOptional(HolderLookup.Provider provider, CompoundTag tag) {
		if (tag.isEmpty()) {
			return EMPTY;
		}
		return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag)
				.resultOrPartial(error -> SophisticatedCore.LOGGER.error("Could not load fluid stack: {}", error))
				.orElse(EMPTY);
	}

	public static boolean isSameFluidSameComponents(FluidStack first, FluidStack second) {
		return first == second || Objects.equals(first.getVariant(), second.getVariant());
	}

	public static boolean matches(FluidStack first, FluidStack second) {
		return first.getAmount() == second.getAmount() && isSameFluidSameComponents(first, second);
	}
}
