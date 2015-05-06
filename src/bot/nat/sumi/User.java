package bot.nat.sumi;

import java.util.ArrayList;

public class User {
	public String name;
	public String realName;
	public String userName;
	public String ident;
	public ArrayList<String> chans = new ArrayList<String>();

	public User(String name) {
		this.name = exUserMode(name);
	}

	public static String exUserMode(String name) {
		return name.replace("+", "").replace("%", "").replace("@", "").replace("&", "").replace("~", "");
	}

	public static String getUserMode(String name){
		return name.replace(getUserMode(name), "");
	}
}
