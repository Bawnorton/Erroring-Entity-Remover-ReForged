package mod.azure.eerreforged.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import mod.azure.eerreforged.EERRMod;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.timings.TimeTracker;

@Mixin(Level.class)
public abstract class LevelMixin {

	/**
	 * @author The_Fireplace
	 * @reason Avoid letting erroring entities crash the game if possible
	 */
	@Overwrite
	public void guardEntityTick(Consumer<Entity> tickConsumer, Entity entity) {
		try {
			tickConsumer.accept(entity);
		} catch (Throwable throwable) {
			try {
				TimeTracker.ENTITY_UPDATE.trackStart(entity);
				EERRMod.LOGGER.warn("Removing erroring entity at {} :", entity.position().toString());
				EERRMod.LOGGER.warn(entity.saveWithoutId(new CompoundTag()).toString());
				entity.remove(Entity.RemovalReason.DISCARDED);
				EERRMod.LOGGER.error("Erroring Entity Stacktrace:", throwable);
			} catch (Exception e) {
				var crashReport = CrashReport.forThrowable(throwable, "Ticking entity");
				var crashReportSection = crashReport.addCategory("Entity being ticked");
				entity.fillCrashReportCategory(crashReportSection);
				throw new ReportedException(crashReport);
			}
		}
	}
}
