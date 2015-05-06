package bot.nat.sumi;

import java.util.ArrayList;

public class Servers {
	/* servers */
	private static ArrayList<Server> srv = new ArrayList<Server>();

	/* add channel to pool */
	public static void addServer(Server c){
		srv.add(c);
	}
}
