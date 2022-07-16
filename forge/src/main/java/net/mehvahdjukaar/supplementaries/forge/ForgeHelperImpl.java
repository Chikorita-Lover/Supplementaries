package net.mehvahdjukaar.supplementaries.forge;

import net.mehvahdjukaar.supplementaries.ForgeHelper;
import net.mehvahdjukaar.supplementaries.common.world.explosion.GunpowderExplosion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;
import java.util.function.Consumer;

public class ForgeHelperImpl {

    public static boolean canEntityDestroy(Level level, BlockPos blockPos, Animal animal) {
        return ForgeHooks.canEntityDestroy(level, blockPos, animal);
    }

    public static void openContainerScreen(ServerPlayer player, MenuProvider menuProvider, BlockPos pos) {
        NetworkHooks.openGui(player, menuProvider, pos);
    }

    public static boolean onExplosionStart(Level level, Explosion explosion) {
        return ForgeEventFactory.onExplosionStart(level, explosion);
    }

    public static void onExplosionDetonate(Level level, Explosion explosion, List<Entity> entities, double diameter) {
        ForgeEventFactory.onExplosionDetonate(level, explosion, entities, diameter);
    }

    public static void onLivingConvert(LivingEntity skellyHorseMixin, LivingEntity newHorse) {
        ForgeEventFactory.onLivingConvert(newHorse, newHorse);
    }

    public static boolean canLivingConvert(LivingEntity entity, EntityType<? extends LivingEntity> outcome, Consumer<Integer> timer) {
        return ForgeEventFactory.canLivingConvert(entity, outcome, timer);
    }

    public static double getReachDistance(LivingEntity entity) {
        return entity.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();
    }

    public static float getExplosionResistance(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        return state.getExplosionResistance(level, pos, explosion);
    }

    public static void onBlockExploded(BlockState blockstate, Level level, BlockPos blockpos, Explosion explosion) {
        blockstate.onBlockExploded(level, blockpos, explosion);
    }

    public static boolean areStacksEqual(ItemStack stack, ItemStack other, boolean sameNbt) {
        return stack.equals(other, sameNbt);
    }

    public static boolean isFireSource(BlockState blockState, Level level, BlockPos pos, Direction up) {
        return blockState.isFireSource(level, pos, up);
    }

    public static boolean canDropFromExplosion(BlockState blockstate, Level level, BlockPos blockpos, Explosion explosion) {
    return blockstate.canDropFromExplosion(level, blockpos, explosion);
    }
}