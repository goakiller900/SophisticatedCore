package net.p3pp3rf1y.sophisticatedcore.init;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.WaterFluid;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.util.DeferredRegister;

import java.util.function.Supplier;

public class ModFluids {
	private ModFluids() {
	}

	public static final Identifier EXPERIENCE_TAG_NAME = Identifier.fromNamespaceAndPath("c", "experience");
	public static final TagKey<Fluid> EXPERIENCE_TAG = TagKey.create(Registries.FLUID, EXPERIENCE_TAG_NAME);

	private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, SophisticatedCore.MOD_ID);
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, SophisticatedCore.MOD_ID);
	private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB,
			SophisticatedCore.MOD_ID);

	public static final Supplier<WaterFluid> XP_STILL = FLUIDS.register("xp_still", XpSource::new);
	public static final Supplier<WaterFluid> XP_FLOWING = FLUIDS.register("xp_flowing", XpFlowing::new);
	public static final Supplier<Item> XP_BUCKET = ITEMS.register("xp_bucket", () -> new BucketItem(XP_STILL.get(), new Item.Properties()
			.setId(ResourceKey.create(Registries.ITEM, SophisticatedCore.getIdentifier("xp_bucket"))).stacksTo(1)));
	public static final Supplier<CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register("main", () -> FabricCreativeModeTab.builder()
			.icon(() -> new ItemStack(XP_BUCKET.get()))
			.title(Component.translatable("itemGroup.sophisticatedcore"))
			.displayItems((parameters, output) -> output.accept(new ItemStack(XP_BUCKET.get())))
			.build());

	public static void registerHandlers() {
		FLUIDS.register();
		ITEMS.register();
		CREATIVE_MODE_TABS.register();
	}

	private abstract static class XpFluid extends WaterFluid {
		@Override
		public Fluid getFlowing() {
			return XP_FLOWING.get();
		}

		@Override
		public Fluid getSource() {
			return XP_STILL.get();
		}

		@Override
		public Item getBucket() {
			return XP_BUCKET.get();
		}

		@Override
		protected boolean canConvertToSource(ServerLevel level) {
			return false;
		}

		@Override
		protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
		}

		@Override
		public BlockState createLegacyBlock(FluidState state) {
			return Blocks.AIR.defaultBlockState();
		}

		@Override
		public boolean isSame(Fluid fluid) {
			return fluid == XP_STILL.get() || fluid == XP_FLOWING.get();
		}

		@Override
		public int getSlopeFindDistance(LevelReader level) {
			return 4;
		}

		@Override
		public int getDropOff(LevelReader level) {
			return 1;
		}

		@Override
		public int getTickDelay(LevelReader level) {
			return 5;
		}

		@Override
		public boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
			return direction == Direction.DOWN && !fluid.is(EXPERIENCE_TAG);
		}
	}

	private static final class XpSource extends XpFluid {
		XpSource() {
			registerDefaultState(getStateDefinition().any());
		}

		@Override
		public int getAmount(FluidState state) {
			return 8;
		}

		@Override
		public boolean isSource(FluidState state) {
			return true;
		}
	}

	private static final class XpFlowing extends XpFluid {
		XpFlowing() {
			registerDefaultState(getStateDefinition().any().setValue(LEVEL, 7).setValue(FALLING, false));
		}

		@Override
		protected void createFluidStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Fluid, FluidState> builder) {
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}

		@Override
		public int getAmount(FluidState state) {
			return state.getValue(LEVEL);
		}

		@Override
		public boolean isSource(FluidState state) {
			return false;
		}
	}
}
