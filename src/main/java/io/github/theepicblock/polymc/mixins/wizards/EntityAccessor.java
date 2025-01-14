package io.github.theepicblock.polymc.mixins.wizards;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("CURRENT_ID")
    static AtomicInteger getEntityIdCounter() {
        throw new IllegalStateException();
    }

    @Accessor("FLAGS")
    static TrackedData<Byte> getFlagTracker() {
        throw new IllegalStateException();
    }

    @Accessor("NO_GRAVITY")
    static TrackedData<Boolean> getNoGravityTracker() {
        throw new IllegalStateException();
    }

    @Accessor("SILENT")
    static TrackedData<Boolean> getSilentTracker() {
        throw new IllegalStateException();
    }
}
