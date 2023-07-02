package gravity_changer;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import gravity_changer.util.GravityComponent;
import gravity_changer.util.GravityDirectionComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class GravityChangerComponentsInitializer implements EntityComponentInitializer {
    
    public static final ComponentKey<GravityComponent> GRAVITY_MODIFIER =
        ComponentRegistry.getOrCreate(new ResourceLocation("gravityapi", "gravity_direction"), GravityComponent.class);

//    public static final ComponentKey<GravityDimensionStrengthInterface> GRAVITY_DIMENSION_STRENGTH =
//        ComponentRegistry.getOrCreate(new Identifier("gravityapi", "gravity_dimension_strength"), GravityDimensionStrengthInterface.class);
    
    
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(Entity.class, GRAVITY_MODIFIER, GravityDirectionComponent::new);
    }
}
