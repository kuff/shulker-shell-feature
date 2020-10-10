package afterdark.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderPearlEntity.class)
public abstract class DispensableEnderPearlsMixin extends ThrownItemEntity {

    // constructor is necessary to satisfy the compiler, but not part of the feature implementation
    public DispensableEnderPearlsMixin(EntityType<? extends ThrownItemEntity> type, World world) {
        super(type, world);
    }

    @Inject(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/thrown/EnderPearlEntity;remove()V"))
    private void onCollisionInjection(HitResult hitResult, CallbackInfo ci) {

        // if the Ender Pearl was not thrown by a living entity, it still has the same chance to spawn an Endermite as if it was
        if (this.getOwner() == null && this.random.nextFloat() < 0.05F && this.world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING)) {
            Vec3d pos = hitResult.getPos();  // point of impact
            EndermiteEntity endermiteEntity = (EndermiteEntity) EntityType.ENDERMITE.create(this.world); // new Endermite entity

            // following code is a workaround for Endermites spawning partially inside blocks on horizontal impacts. There must be a better way of doing this.
            double finalX = pos.getX();
            double finalY = pos.getY();
            double finalZ = pos.getZ();
            if (hitResult.getType() == HitResult.Type.BLOCK) { // if the Ender Peal impacted a BLOCK
                BlockHitResult bhr = (BlockHitResult) hitResult;
                Direction d = bhr.getSide();
                Direction.Axis axis = d.getAxis();
                Direction.AxisDirection ad = d.getDirection();

                // increment or decrement the x- or z-coordinate depending on what direction the impact surface is facing
                switch(axis.getName()) {
                    case "x":
                        if (ad.toString().equals("Towards positive")) {
                            finalX = finalX + 0.5D;
                        }
                        else finalX = finalX - 0.5D;
                        break;
                    case "z":
                        if (ad.toString().equals("Towards positive")) {
                            finalZ = finalZ + 0.5D;
                        }
                        else finalZ = finalZ - 0.5D;
                }
            }

            // move the Endermite to the corrected position and spawn it in
            assert endermiteEntity != null;
            endermiteEntity.refreshPositionAndAngles(finalX, finalY, finalZ, this.yaw, this.pitch);
            this.world.spawnEntity(endermiteEntity);
        }
    }

}