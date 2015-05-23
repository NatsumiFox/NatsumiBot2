package bot.nat.sumi;

import java.util.ArrayList;

public class Servers {
	/* servers */
	private static ArrayList<Server> srv = new ArrayList<Server>();

	/* add name to pool */
	public static void addServer(Server c){
		srv.add(c);
	}

	public static Server get(String IP){
		for(Server s : srv){
			if(s.IP.equals(IP)){
				return s;
			}
		}

		return null;
	}
}
