package bot.nat.sumi;

import java.util.*;

public class SpecialModules {

	private static String getUser(String name) {
		return name.split("!")[0].replace(":", "");
	}

	public static ArrayList<Module> r() {
		ArrayList<Module> ret = new ArrayList<Module>();

		ret.add(nick());
		ret.add(join());
		ret.add(part());
		ret.add(quit());
		ret.add(mode());
		ret.add(r001());
		ret.add(r353());
		ret.add(r311());
		ret.add(r319());
		ret.add(r433());
		ret.add(r330());

		return ret;
	}

	private static Module mode() {
		return new Module() {
			@Override
			public void command(Message m, final Server srv) {
				if(m.text.split(" ").length < 2){
					return;
				}

				Chan c = srv.getUser(m.text.split(" ")[1]).getChan(m.channel);
				if(c == null){
					return;
				}

				if(m.text.startsWith("+")){
					c.addUserLevel(getLevel(m.text.split(" ")[0].replace("+", "")));

				} else if(m.text.startsWith("-")){
					c.rmvUserLevel(getLevel(m.text.split(" ")[0].replace("-", "")));
				}
			}

			/* translate mode level to symbol level */
			private Chan.ULevel getLevel(String lvl) {
				switch (lvl.charAt(0)){
					case 'v':
						return Chan.ULevel.VOP;

					case 'h':
						return Chan.ULevel.HOP;

					case 'o':
						return Chan.ULevel.OP;

					case 'a':
						return Chan.ULevel.AOP;

					case 'q':
						return Chan.ULevel.QOP;

					default:
						return null;
				}
			}

			@Override
			public String[] reserved() {
				return new String[]{ "mode" };
			}
		};
	}

	private static Module quit() {
		return new Module() {
			@Override
			public void command(Message m, final Server srv) {
				srv.rmvUser(getUser(m.author));
			}

			@Override
			public String[] reserved() {
				return new String[]{ "quit" };
			}
		};
	}

	private static Module part() {
		return new Module() {
			@Override
			public void command(Message m, final Server srv) {
				User u = srv.getUser(getUser(m.author));
				if(u != null){
					u.chan.remove(u.getChan(m.text.split(" ")[0]));

					if(u.chan.size() == 0){
						srv.rmvUser(getUser(m.author));
					}
				}
			}

			@Override
			public String[] reserved() {
				return new String[]{ "part" };
			}
		};
	}

	private static Module nick() {
		return new Module() {
			@Override
			public void command(Message m, final Server srv) {
				User u = srv.getUser(User.exUserMode(getUser(m.author)));
				u.name = m.channel;
			}

			@Override
			public String[] reserved() {
				return new String[]{ "nick" };
			}
		};
	}

	private static Module join() {
		return new Module() {
			@Override
			public void command(Message m, final Server srv) {
				srv.addUser(new User(getUser(m.author)));

				if(!getUser(m.author).equals(srv.nick.name)){
					srv.send("WHOIS "+ srv.IP +" "+ User.exUserMode(getUser(m.author)), "");
				}
			}

			@Override
			public String[] reserved() {
				return new String[]{ "join" };
			}
		};
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

	private static Module r330() {
		return new Module() {
			@Override
			public void command(Message m, final Server srv) {
				srv.getUser(m.text.split(" ")[0]).isLoggedIn = true;
				srv.getUser(m.text.split(" ")[0]).resolveBotOp(srv);
			}

			@Override
			public String[] reserved() {
				return new String[]{ "330" };
			}
		};
	}

	private static Module r319() {
		return new Module() {
			@Override
			public void command(Message m, final Server srv) {
				User u = srv.getUser(User.exUserMode(m.text.split(" ")[0]));

				for(String s : m.text.replace(u.name +" :", "").split(" ")){
					if(!chkContains(u, s)){
						u.chan.add(new Chan(s));
					}
				}
			}

			private boolean chkContains(User u, String s) {
				for(Chan c : u.chan){
					if(s.equals(c.name)){
						return true;
					}
				}

				return false;
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
				srv.addUser(new User(User.exUserMode(m.text.split(" ")[0])));
				User u = srv.getUser(User.exUserMode(m.text.split(" ")[0]));
				u.userName = m.text.split(" ")[1];
				u.ident =    m.text.split(" ")[2];
				u.realName = m.text.split(":")[1];
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
