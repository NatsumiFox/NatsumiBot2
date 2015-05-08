package bot.nat.sumi;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class User {
	public String name;
	public String realName;
	public String userName;
	public String ident;
	public boolean isLoggedIn = false;
	public boolean isBotOp = false;
	public ArrayList<Chan> chan = new ArrayList<Chan>();

	public User(String name) {
		this.name = exUserMode(name);
	}

	public static String exUserMode(String name) {
		return name.replace("+", "").replace("%", "").replace("@", "").replace("&", "").replace("~", "");
	}

	public static String getUserMode(String name){
		return name.replace(exUserMode(name), "");
	}

	/* gets channel with name */
	public Chan getChan(String name) {
		for(Chan c : chan){
			if(c.name.equals(name)){
				return c;
			}
		}

		return null;
	}

	public void resolveBotOp(Server srv) {
		String[] list = Main.read(new File(Main.folder +"BOTOPS."+ srv.cfgName +".txt")).replace("\r", "").split("\\n");

		for(String n : list){
			if(n.equals(name)){
				isBotOp = true;
			}
		}
	}

	public String chanToString(){
		return Arrays.toString(chan.toArray(new Chan[chan.size()]));
	}

	@Override
	public String toString(){
		return userName +"@"+ ident +": "+ realName +" - in "+ chanToString() +" - vars: isLoggedIn "+ isLoggedIn +"; isBotOp "+ isBotOp;
	}
}
