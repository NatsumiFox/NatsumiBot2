//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.PigZombie;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Timer;
import java.util.TimerTask;

public class main extends JavaPlugin {
	public static main plugin;
	public final pigmanSpawnListener psl = new pigmanSpawnListener(this);
	double swordChanceInt = (double)this.getConfig().getInt("swordChance");
	boolean enableAnger = this.getConfig().getBoolean("enableAnger");
	boolean enableSwordChance = this.getConfig().getBoolean("enableSwordChance");
	int changeSpeed = this.getConfig().getInt("changeSpeed");
	int utdConf = 2;
	public static String tag = "[HarderPigmen]";

	public main() {
	}

	public void onDisable() {
		PluginDescriptionFile desc = this.getDescription();
		System.out.println(tag + " v." + desc.getVersion() + " has been disabled!");
	}

	public void onEnable() {
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(this.psl, this);
		final pigmanSpawnListener p = new pigmanSpawnListener(this);
		PluginDescriptionFile desc = this.getDescription();
		this.getConfig().options().copyDefaults(true);
		this.getConfig().options().header("See http://dev.bukkit.org/bukkit-plugins/harder-pigmen/ for configuration help!").copyHeader(true);
		this.checkConfigVer(this.getConfig().getInt("configVer"));
		this.saveConfig();

		main.this.getServer().getScheduler().scheduleSyncRepeatingTask(main.this, () -> {
			World w = Bukkit.getWorlds().get(0);
			w.getEntitiesByClass(PigZombie.class).forEach(p::method);
		}, 10 * 20, 20 * 20);

		System.out.println(tag + " v." + desc.getVersion() + " has been enabled!");
	}

	public void checkConfigVer(int ver) {
		switch(ver) {
			case 1:
				System.out.println(tag + " Your config is version 1, updating to most recent version.");
				this.getConfig().set("enableSlowness", null);
				this.getConfig().addDefault("changeSpeed", Integer.valueOf(2));
				this.getConfig().set("configVer", Integer.valueOf(2));
				System.out.println(tag + " Config updated to version 2. Check for new configurable options!");
				break;
			default:
				System.out.println(tag + " Looks like your config is up to date.");
		}

		this.saveConfig();
		if(this.getConfig().getInt("configVer") != this.utdConf) {
			System.out.println("Looping back to check configuration again for any missed updates.");
			this.checkConfigVer(this.getConfig().getInt("configVer"));
		}

	}
}
