package net.minecraft.server.v1_8_R3;

import bot.nat.sumi.ConfigFile;
import bot.nat.sumi.Main;
import bot.nat.sumi.Server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

public class _MinecraftLogin {

	public static String canLogin(String name) {
		ConfigFile cfg = new ConfigFile(new File(Main.folder + "MinecraftLogin.txt"), ConfigFile.READ | ConfigFile.WRITE);

		if(!cfg.containsSection(name)){
			return "You ("+ name +") have not successfully linked your account yet!";
		}

		if(!cfg.getSection(name).getField("login").getValue().equals("true")){
			return "You ("+ name +") have not logged in for this session!";
		}

		cfg.getSection(name).setField("login", false + "");
		cfg.flush();
		return null;
	}

	public static String link(String in, String auth, Server srv) {
		ConfigFile cfg = new ConfigFile(new File(Main.folder + "MinecraftLogin.txt"), ConfigFile.READ | ConfigFile.WRITE);
		if(in.split(" ").length == 1){
			return "Illegal arguments!";
		}

		/* resolve parameters */
		String user = in.split(" ")[1], pass = null;
		if(in.split(" ").length > 2){
			pass = in.split(" ")[2];

			if(pass.replace(" ", "").equals("")){
				pass = null;
			}
		}

		/* if user pre-exists, check these things */
		if(cfg.containsSection(user)) {
			if (contains(cfg.getSection(user).getField("with").getValue().split(" "), auth)) {
				return "Already linked!";
			}

			if(!cfg.getSection(user).getField("pass").getValue().replace(" ", "").equals("") && (pass == null ||
					!cfg.getSection(user).getField("pass").getValue().equals(pass)) && cfg.getSection(user).getField("requirepass").getValue().equals("true")){
				return "Password invalid or incorrect!";
			}

			if(pass == null && !srv.getUser(auth).isLoggedIn){
				return "Password required for unregistered users!";
			}

			if((pass == null || !cfg.getSection(user).getField("pass").getValue().equals(pass)) &&
					(!srv.getUser(auth).isLoggedIn || !srv.getUser(auth).loggedInAs.equals(cfg.getSection(user).getField("owner").getValue()))){
				return "Can not link from different account without providing correct password!";
			}

			if(pass == null && cfg.getSection(user).getField("requirepass").getValue().equals("true")){
				return "Password required for this user!";
			}

			cfg.getSection(user).setField("with", cfg.getSection(user).getField("with") +" "+ auth);
			cfg.flush();

			return "'"+ auth +"' linked successfully!";

		} else {
			cfg.createSection(user).setField("with", auth);
			cfg.getSection(user).setField("owner", srv.getUser(auth).isLoggedIn ? srv.getUser(auth).loggedInAs : " ");
			cfg.getSection(user).setField("pass", pass == null ? "" : pass);
			cfg.getSection(user).setField("requirepass", "false");
			cfg.getSection(user).setField("login", false + "");
			cfg.flush();

			return "New link created successfully!";
		}
	}

	private static boolean contains(String[] src, String res) {
		for(String s : src){
			if(s.equals(res)){
				return true;
			}
		}

		return false;
	}

	public static String login(String in, String auth, Server srv) {
		ConfigFile cfg = new ConfigFile(new File(Main.folder + "MinecraftLogin.txt"), ConfigFile.READ | ConfigFile.WRITE);
		/* resolve parameters */
		String user = null, pass = null;
		if(in.split(" ").length > 1){
			pass = in.split(" ")[1];

			if(pass.replace(" ", "").equals("")){
				pass = null;
			}
		}

		/* try to find correct Minecraft username */
		for(ConfigFile.Section sec : cfg.getSections()){
			for(String s : sec.getField("with").getValue().split(" ")){
				if(s.equalsIgnoreCase(auth)){
					user = sec.getName();
				}
			}
		}

		/* if user does not exist */
		if(user == null) {
			return "Your nickname is not linked with any account!";
		}

		if (!contains(cfg.getSection(user).getField("with").getValue().split(" "), auth)) {
			return "You are not linked!";
		}

		if(!cfg.getSection(user).getField("pass").getValue().replace(" ", "").equals("") && (pass == null ||
				!cfg.getSection(user).getField("pass").getValue().equals(pass)) && cfg.getSection(user).getField("requirepass").getValue().equals("true")){
			return "Password invalid or incorrect!";
		}

		if(pass == null && !srv.getUser(auth).isLoggedIn){
			return "Password required for unregistered users!";
		}

		if((pass == null || !cfg.getSection(user).getField("pass").getValue().equals(pass)) &&
				(!srv.getUser(auth).isLoggedIn || !srv.getUser(auth).loggedInAs.equals(cfg.getSection(user).getField("owner").getValue()))){
			return "Can not login from different account without providing correct password!";
		}

		if(pass == null && cfg.getSection(user).getField("requirepass").getValue().equals("true")){
			return "Password required for this user!";
		}

		cfg.getSection(user).setField("login", true + "");
		cfg.flush();
		return "Login successful! One-time ticket granted.";
	}

	public static String ghost(String in, String auth, Server srv, BufferedWriter bf) throws IOException {
		ConfigFile cfg = new ConfigFile(new File(Main.folder + "MinecraftLogin.txt"), ConfigFile.READ | ConfigFile.WRITE);
		/* resolve parameters */
		String user = null, pass = null;
		if(in.split(" ").length > 1){
			pass = in.split(" ")[1];

			if(pass.replace(" ", "").equals("")){
				pass = null;
			}
		}

		/* try to find correct Minecraft username */
		for(ConfigFile.Section sec : cfg.getSections()){
			for(String s : sec.getField("with").getValue().split(" ")){
				if(s.equalsIgnoreCase(auth)){
					user = sec.getName();
				}
			}
		}

		/* if user does not exist */
		if(user == null) {
			return "Your nickname is not linked with any account!";
		}

		if (!contains(cfg.getSection(user).getField("with").getValue().split(" "), auth)) {
			return "You are not linked!";
		}

		if(!cfg.getSection(user).getField("pass").getValue().replace(" ", "").equals("") && (pass == null ||
				!cfg.getSection(user).getField("pass").getValue().equals(pass)) && cfg.getSection(user).getField("requirepass").getValue().equals("true")){
			return "Password invalid or incorrect!";
		}

		if(pass == null && !srv.getUser(auth).isLoggedIn){
			return "Password required for unregistered users!";
		}

		if((pass == null || !cfg.getSection(user).getField("pass").getValue().equals(pass)) &&
				(!srv.getUser(auth).isLoggedIn || !srv.getUser(auth).loggedInAs.equals(cfg.getSection(user).getField("owner").getValue()))){
			return "Can not ghost from different account without providing correct password!";
		}

		if(pass == null && cfg.getSection(user).getField("requirepass").getValue().equals("true")){
			return "Password required for this user!";
		}

		bf.write("kick " + user + " GHOST by " + auth);
		bf.flush();
		cfg.getSection(user).setField("login", false + "");
		cfg.flush();

		return "'"+ user +"' has been kicked.";
	}
}
