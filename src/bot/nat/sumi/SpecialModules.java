package bot.nat.sumi;

import java.util.*;

public class SpecialModules {
	public static ArrayList<Module> r() {
		ArrayList<Module> ret = new ArrayList<Module>();

		ret.add(r001());
		ret.add(r353());
		ret.add(r311());
		ret.add(r319());
		ret.add(r433());
		ret.add(nick());

		return ret;
	}

	private static Module r433() {
		return new Module() {
			@Override
			public void command(Message m, final Server srv) {
				srv.nextNick();
			}

			@Override
			public String[] reserved() {
				return new String[]{ "433" };
			}
		};
	}

	private static Module nick() {
		return new Module() {
			@Override
			public void command(Message m, final Server srv) {
				User u = srv.getUser(User.exUserMode(m.author));
				u.name = m.channel;
			}

			@Override
			public String[] reserved() {
				return new String[]{ "NICK" };
			}
		};
	}

	private static Module r319() {
		return new Module() {
			@Override
			public void command(Message m, final Server srv) {
				User u = srv.getUser(User.exUserMode(m.text.split(" ")[0]));
				Collections.addAll(u.chans, m.text.replace(User.exUserMode(u.name) +" :", "").split(" "));
			}

			@Override
			public String[] reserved() {
				return new String[]{ "319" };
			}
		};
	}

	private static Module r311() {
		return new Module() {
			@Override
			public void command(Message m, final Server srv) {
				User u = srv.getUser(User.exUserMode(m.text.split(" ")[0]));
				u.userName = m.text.split(" ")[1];
				u.ident =    m.text.split(" ")[2];
				u.realName = m.text.split(":")[1];
			}

			private String asd(User[] s) {
				String r = "[";

				for(User u : s){
					r += u.name +", ";
				}

				return r +"]";
			}

			@Override
			public String[] reserved() {
				return new String[]{ "311" };
			}
		};
	}

	private static Module r353() {
		return new Module() {
			@Override
			public void command(Message m, final Server srv) {
				/* load all users */
				for(String u : m.text.replace("= "+ m.text.split(" ")[1] +" :", "").split(" ")){
					srv.addUser(new User(u));
					srv.send("WHOIS "+ srv.IP +" "+ User.exUserMode(u), "");
				}
			}

			@Override
			public String[] reserved() {
				return new String[]{ "353" };
			}
		};
	}

	private static Module r001() {
		return new Module() {
			@Override
			public void command(Message m, final Server srv) {
				new Timer("").schedule(new TimerTask() {
					@Override
					public void run() {
						srv.joinChannels();
						if(!srv.password.equals("!")){
							srv.send("PASS"+ srv.password, "");
						}
					}
				}, 50);
			}

			@Override
			public String[] reserved() {
				return new String[]{ "001" };
			}
		};
	}
}
