//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PigZombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class pigmanSpawnListener implements Listener {
	public static main plugin;

	public pigmanSpawnListener(main instance) {
		plugin = instance;
	}

	public boolean method(Entity e) {
		if (e.getType() == EntityType.PIG_ZOMBIE) {
			PigZombie pigman = (PigZombie) e;
			switch (plugin.changeSpeed) {
				case -1:
					pigman.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2147483647, 2));
				case 0:
				default:
					break;
				case 1:
					pigman.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2147483647, 1));
					break;
				case 2:
					pigman.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2147483647, 2));
					break;
				case 3:
					pigman.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2147483647, 4));
			}

			if (plugin.enableAnger && e.getWorld().getEnvironment().getId() == -1) {
				pigman.setAngry(true);
			}

			return false;
		} else {
			return false;
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public boolean onPigmanTarget(EntityTargetEvent event) {
		return method(event.getEntity());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public boolean onPigmanChangeBlock(EntityChangeBlockEvent event) {
		return method(event.getEntity());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public boolean onPigmanSpawnEvent(CreatureSpawnEvent event) {
		return method(event.getEntity());
	}
}
