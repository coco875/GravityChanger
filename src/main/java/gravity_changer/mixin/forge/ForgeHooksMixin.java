package gravity_changer.mixin.forge;

import gravity_changer.api.GravityChangerAPI;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.*;

@Pseudo
@Mixin(targets = "net.minecraftforge.common.ForgeHooks")
public class ForgeHooksMixin {
    @Redirect(
        method = "onLivingBreathe",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/BlockPos;containing(DDD)Lnet/minecraft/core/BlockPos;",
            ordinal = 0
        )
    )
    private static BlockPos redirect_baseTick_new_0(double x, double y, double z, LivingEntity self) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) self);
        if (gravityDirection == Direction.DOWN) {
            return BlockPos.containing(x, y, z);
        }
        
        return BlockPos.containing(self.getEyePosition());
    }
}

