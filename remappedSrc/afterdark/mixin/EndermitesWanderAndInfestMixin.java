package afterdark.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import afterdark.mixin.EndermitesWanderAndInfestMixin.EndermiteWanderAndInfestGoal;
import java.util.EnumSet;
import java.util.Random;

@Mixin(EndermiteEntity.class)
public class EndermitesWanderAndInfestMixin extends HostileEntity {

    protected EndermitesWanderAndInfestMixin(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
    }

    @Inject(method = "initGoals", at = @At("HEAD"))
    private void newInitGoals(CallbackInfo ci) {
        // add our custom goal
        this.goalSelector.add(5, new EndermiteWanderAndInfestGoal(this));
    }

    @Override // override method in HostileEntity
    public float getPathfindingFavor(BlockPos pos, WorldView worldView) {
        // paths that result in a Purpur Block underneath the endermite are desirable
        return (worldView.getBlockState(pos.down()).getBlock() == Blocks.PURPUR_BLOCK) ? 10.0F : super.getPathfindingFavor(pos, worldView);
    }

    /**
     * Custom goal for infecting Purpur Blocks
     */
    static class EndermiteWanderAndInfestGoal extends WanderAroundGoal {
        private Direction direction;
        private boolean canConvert;

        public EndermiteWanderAndInfestGoal(HostileEntity endermite) {
            super(endermite, 1.0D, 10);
            this.setControls(EnumSet.of(Control.MOVE));
        }

        /**
         * Evaluates if a block in the Endermite path can be converted
         */
        @Override
        public boolean canStart() {
            if (this.mob.getTarget() != null) {
                return false;
            } else if (!this.mob.getNavigation().isIdle()) {
                return false;
            }
            Random random = this.mob.getRandom();
            if (this.mob.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) && random.nextInt(10) == 0) {
                this.direction = Direction.random(random);
                BlockPos blockPos = (new BlockPos(this.mob.getX(), this.mob.getY() + 0.5D, this.mob.getZ())).offset(this.direction);
                BlockState blockState = this.mob.world.getBlockState(blockPos);
                if (blockState.getBlock() == Blocks.PURPUR_BLOCK) {
                    this.canConvert = true;
                    return true;
                }
            }

            this.canConvert = false;
            return super.canStart();
        }

        /**
         * Whether or not the Endermite should continue with the goal
         */
        @Override
        public boolean shouldContinue() {
            return !this.canConvert && super.shouldContinue();
        }

        /**
         * Performs the conversion if a target block in the Endermite path is convertible
         */
        @Override
        public void start() {
            if (!this.canConvert) {
                super.start();
            } else {
                World iworld = this.mob.world;
                BlockPos blockPos = (new BlockPos(this.mob.getX(), this.mob.getY() + 0.5D, this.mob.getZ())).offset(this.direction);
                BlockState blockState = iworld.getBlockState(blockPos);
                if (blockState.getBlock() == Blocks.PURPUR_BLOCK) {
                    ShulkerEntity shulker = (ShulkerEntity) EntityType.SHULKER.create(iworld); // new Shulker entity
                    shulker.refreshPositionAndAngles(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 0, 0);
                    iworld.breakBlock(blockPos, false, shulker); // break the Purpur Block
                    iworld.spawnEntity(shulker); // spawn the shulker entity
                    this.mob.playSpawnEffects();
                    this.mob.remove();
                }
            }
        }
    }

}
