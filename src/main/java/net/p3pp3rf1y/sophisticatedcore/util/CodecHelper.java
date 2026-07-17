package net.p3pp3rf1y.sophisticatedcore.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;

import java.util.Set;
import java.util.Optional;

public class CodecHelper {
	public static final Codec<ItemStack> OVERSIZED_ITEM_STACK_CODEC = Codec.lazyInitialized(
			() -> RecordCodecBuilder.create(
					instance -> instance.group(
									Item.CODEC.fieldOf("id").forGetter(ItemStack::typeHolder),
									Codec.INT.fieldOf("count").orElse(1).forGetter(ItemStack::getCount),
									DataComponentPatch.CODEC
											.optionalFieldOf("components", DataComponentPatch.EMPTY)
											.forGetter(p_330103_ -> p_330103_.components.asPatch())
							)
							.apply(instance, ItemStack::new)));

	public static final PrimitiveCodec<Integer> STRING_ENCODED_INT = new PrimitiveCodec<Integer>() {
		@Override
		public <T> DataResult<Integer> read(final DynamicOps<T> ops, final T input) {
			return ops.getStringValue(input).map(s -> {
				if (s.startsWith("i")) {
					return Integer.parseInt(s.substring(1));
				} else {
					return Integer.parseInt(s);
				}
			});
		}

		@Override
		public <T> T write(final DynamicOps<T> ops, final Integer value) {
			return ops.createString("i" + value);
		}

		@Override
		public String toString() {
			return "Int";
		}
	};

	private CodecHelper() {}

	public static <T> Codec<Set<T>> setOf(Codec<T> elementCodec) {
		return new SetCodec<>(elementCodec);
	}

	public static CompoundTag encodeItemStack(HolderLookup.Provider provider, ItemStack stack) {
		return ItemStack.OPTIONAL_CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), stack)
				.result()
				.filter(CompoundTag.class::isInstance)
				.map(CompoundTag.class::cast)
				.orElseGet(CompoundTag::new);
	}

	public static Optional<ItemStack> decodeItemStack(HolderLookup.Provider provider, Tag tag) {
		return ItemStack.OPTIONAL_CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag).result();
	}
}
