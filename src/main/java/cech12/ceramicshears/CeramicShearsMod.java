package cech12.ceramicshears;

import cech12.ceramicshears.api.item.CeramicShearsItems;
import cech12.ceramicshears.loot_modifiers.CeramicShearsLootModifier;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.block.TripWireBlock;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.annotation.Nonnull;

import static cech12.ceramicshears.CeramicShearsMod.MOD_ID;

@Mod(MOD_ID)
@Mod.EventBusSubscriber
public class CeramicShearsMod {

    public static final String MOD_ID = "ceramicshears";

    public CeramicShearsMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(GlobalLootModifierSerializer.class, this::onRegisterModifierSerializers);
    }

    /**
     * Add ceramic shears loot modifiers to be compatible with other mods and change loot behaviour of vanilla blocks influenced by shears.
     */
    public void onRegisterModifierSerializers(@Nonnull final RegistryEvent.Register<GlobalLootModifierSerializer<?>> event) {
        event.getRegistry().register(
                new CeramicShearsLootModifier.Serializer().setRegistryName(MOD_ID, "ceramic_shears_harvest")
        );
    }

    /**
     * Add ceramic shears block interactions.
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack itemStack = event.getPlayer().getHeldItem(event.getHand());
        //check for ceramic shears
        if (itemStack.getItem() == CeramicShearsItems.CERAMIC_SHEARS) {
            BlockState blockState = event.getWorld().getBlockState(event.getPos());
            //add carving behaviour to pumpkins. mostly copied from Pumpkin class.
            if (blockState.getBlock() == Blocks.PUMPKIN) {
                World worldIn = event.getWorld();
                if (!worldIn.isRemote) {
                    PlayerEntity player = event.getPlayer();
                    BlockPos pos = event.getPos();
                    Direction direction = event.getFace();
                    if (direction == null) return;
                    Direction direction1 = direction.getAxis() == Direction.Axis.Y ? player.getHorizontalFacing().getOpposite() : direction;
                    worldIn.playSound(null, pos, SoundEvents.BLOCK_PUMPKIN_CARVE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    worldIn.setBlockState(pos, Blocks.CARVED_PUMPKIN.getDefaultState().with(CarvedPumpkinBlock.FACING, direction1), 11);
                    ItemEntity itementity = new ItemEntity(worldIn, (double)pos.getX() + 0.5D + (double)direction1.getXOffset() * 0.65D, (double)pos.getY() + 0.1D, (double)pos.getZ() + 0.5D + (double)direction1.getZOffset() * 0.65D, new ItemStack(Items.PUMPKIN_SEEDS, 4));
                    itementity.setMotion(0.05D * (double)direction1.getXOffset() + worldIn.rand.nextDouble() * 0.02D, 0.05D, 0.05D * (double)direction1.getZOffset() + worldIn.rand.nextDouble() * 0.02D);
                    worldIn.addEntity(itementity);
                    itemStack.damageItem(1, player, (p_220282_1_) -> p_220282_1_.sendBreakAnimation(event.getHand()));
                }
                event.setCanceled(true);
                event.setCancellationResult(ActionResultType.SUCCESS);
            }
        }
    }

    /**
     * Add tripwire deactivation.
     */
    @SubscribeEvent
    public static void onBlockBreakEvent(BlockEvent.BreakEvent event) {
        BlockState blockState = event.getState();
        ItemStack itemStack = event.getPlayer().getHeldItemMainhand();
        if (itemStack.getItem() == CeramicShearsItems.CERAMIC_SHEARS && blockState.getBlock() == Blocks.TRIPWIRE) {
            event.getWorld().setBlockState(event.getPos(), blockState.with(TripWireBlock.DISARMED, true), 4);
        }
    }

}
