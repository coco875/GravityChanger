package gravity_changer;

import gravity_changer.util.QuaternionUtil;
import gravity_changer.util.RotationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class RotationAnimation {
    private boolean inAnimation = false;
    private Quaternionf startGravityRotation;
    private Quaternionf endGravityRotation;
    private long startTimeMs;
    private long endTimeMs;
    
    public void applyRotationAnimation(Direction newGravity, Direction prevGravity, long durationTimeMs, Entity entity, long timeMs, boolean rotateView) {
        if (durationTimeMs == 0) {
            inAnimation = false;
            return;
        }
        
        Validate.notNull(entity);
        
        Vec3 newLookingDirection = getNewLookingDirection(newGravity, prevGravity, entity);
        
        Quaternionf oldViewRotation = QuaternionUtil.getViewRotation(entity.getXRot(), entity.getYRot());
        
        Quaternionf currentAnimatedGravityRotation = getCurrentGravityRotation(prevGravity, timeMs);
        
        // camera rotation = view rotation(pitch and yaw) * gravity rotation(animated)
//        Quaternionf currentAnimatedCameraRotation = QuaternionUtil.mult(oldViewRotation, currentAnimatedGravityRotation);
        Quaternionf currentAnimatedCameraRotation = new Quaternionf().set(oldViewRotation).mul(currentAnimatedGravityRotation);
        
        Quaternionf newEndGravityRotation = RotationUtil.getWorldRotationQuaternion(newGravity);
        
        Vec2 newYawAndPitch = RotationUtil.vecToRot(
            RotationUtil.vecWorldToPlayer(newLookingDirection, newGravity)
        );
        float newPitch = newYawAndPitch.y;
        float newYaw = newYawAndPitch.x;
        float deltaYaw = newYaw - entity.getYRot();
        float deltaPitch = newPitch - entity.getXRot();
        entity.setYRot(entity.getYRot() + deltaYaw);
        entity.setXRot(entity.getXRot() + deltaPitch);
        entity.yRotO += deltaYaw;
        entity.xRotO += deltaPitch;
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.yBodyRot += deltaYaw;
            livingEntity.yBodyRotO += deltaYaw;
            livingEntity.yHeadRot += deltaYaw;
            livingEntity.yHeadRotO += deltaYaw;
        }
        
        Quaternionf newViewRotation = QuaternionUtil.getViewRotation(entity.getXRot(), entity.getYRot());
        
        // gravity rotation = (view rotation^-1) * camera rotation
//        Quaternionf animationStartGravityRotation = QuaternionUtil.mult(
//            QuaternionUtil.inversed(newViewRotation), currentAnimatedCameraRotation
//        );
        Quaternionf animationStartGravityRotation = new Quaternionf().set(newViewRotation).conjugate().mul(currentAnimatedCameraRotation);
        
        inAnimation = true;
        startGravityRotation = animationStartGravityRotation;
        endGravityRotation = newEndGravityRotation;
        startTimeMs = timeMs;
        endTimeMs = timeMs + durationTimeMs;
    }
    
    private Vec3 getNewLookingDirection(
        Direction newGravity, Direction prevGravity, Entity player
    ) {
        Vec3 oldLookingDirection = RotationUtil.vecPlayerToWorld(
            RotationUtil.rotToVec(player.getYRot(), player.getXRot()),
            prevGravity
        );
        
        if (newGravity == prevGravity.getOpposite()) {
            return oldLookingDirection.scale(-1);
        }
        
        Quaternionf deltaRotation = QuaternionUtil.getRotationBetween(
            Vec3.atLowerCornerOf(prevGravity.getNormal()),
            Vec3.atLowerCornerOf(newGravity.getNormal())
        );
        
        Vector3f lookingDirection = new Vector3f((float) oldLookingDirection.x, (float) oldLookingDirection.y, (float) oldLookingDirection.z);
        lookingDirection.rotate(deltaRotation);
        Vec3 newLookingDirection = new Vec3(lookingDirection);
        return newLookingDirection;
    }
    
    public Quaternionf getCurrentGravityRotation(Direction currentGravity, long timeMs) {
        
        if (timeMs > endTimeMs) {
            inAnimation = false;
        }
        
        if (!inAnimation) {
            return RotationUtil.getWorldRotationQuaternion(currentGravity);
        }
        
        double delta = (double) (timeMs - startTimeMs) / (endTimeMs - startTimeMs);
        
        // make sure that frustum culling updates when running rotation animation
        Minecraft.getInstance().levelRenderer.needsUpdate();
        
        return RotationUtil.interpolate(
            startGravityRotation, endGravityRotation,
            mapProgress((float) delta)
        );
    }
    
    private static float mapProgress(float delta) {
        return Mth.clamp((delta * delta * (3 - 2 * delta)), 0, 1);
    }
    
    public boolean isInAnimation() {
        return inAnimation;
    }
    
    public void toNbt(CompoundTag nbt) {
        nbt.putBoolean("InAnimation", inAnimation);
        if (inAnimation) {
            nbt.putFloat("Q0X", startGravityRotation.x());
            nbt.putFloat("Q0Y", startGravityRotation.y());
            nbt.putFloat("Q0Z", startGravityRotation.z());
            nbt.putFloat("Q0W", startGravityRotation.w());
            nbt.putFloat("Q1X", endGravityRotation.x());
            nbt.putFloat("Q1Y", endGravityRotation.y());
            nbt.putFloat("Q1Z", endGravityRotation.z());
            nbt.putFloat("Q1W", endGravityRotation.w());
            nbt.putLong("StartTime", startTimeMs);
            nbt.putLong("EndTime", endTimeMs);
        }
    }
    
    public void fromNbt(CompoundTag nbt) {
        inAnimation = nbt.getBoolean("InAnimation");//Will return false if no such element exists
        if (inAnimation) {
            startGravityRotation = new Quaternionf(
                nbt.getFloat("Q0X"),
                nbt.getFloat("Q0Y"),
                nbt.getFloat("Q0Z"),
                nbt.getFloat("Q0W")
            );
            endGravityRotation = new Quaternionf(
                nbt.getFloat("Q1X"),
                nbt.getFloat("Q1Y"),
                nbt.getFloat("Q1Z"),
                nbt.getFloat("Q1W")
            );
            startTimeMs = nbt.getLong("StartTime");
            endTimeMs = nbt.getLong("EndTime");
        }
    }
}