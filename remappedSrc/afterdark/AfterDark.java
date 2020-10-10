package afterdark;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Util;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;

public class AfterDark implements ModInitializer {

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.

		// TODO: organize into seperate files

		// register a new behavior for dispensers, allowing them to dispense Ender Pearls as if thrown by a player
		DispenserBlock.registerBehavior(Items.ENDER_PEARL, new ProjectileDispenserBehavior() {
			protected ProjectileEntity createProjectile(World lvl, Position pos, ItemStack stack) {
				return (ProjectileEntity) Util.make(new EnderPearlEntity(lvl, pos.getX(), pos.getY(), pos.getZ()), (enderPearl) -> {
					enderPearl.setItem(stack);
				});
			}
		});
	}

}
