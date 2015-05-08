import bot.nat.sumi.*;

public class user extends Module {
	@Override
	public void command(Message m, Server srv) {
		String[] arg = m.text.split(" ");

		if(arg.length <= 1){
			help(m, srv);
			return;
		}

		if(arg[1].equals("help") && arg.length == 3){
			help(m, srv, arg);

		} else if(arg[1].equals("info") && arg.length == 3){
			info(m, srv, arg);

		} else if(arg[1].equals("list")){
			list(m, srv);

		} else if(arg[1].equals("note") && arg.length >= 4){
			note(m, srv, arg);

		} else if(arg[1].equals("kick") && arg.length >= 3){
			kick(m, srv, arg);

		} else {
			help(m, srv);
		}
	}

	private void help(Message m, Server srv) {
		srv.send(m.channel, m.author, "Available commands: help, kick, list, info, note", m.channel);
		srv.send(m.channel, m.author, "Usage: $user help _command_", m.channel);
	}

	private void help(Message m, Server srv, String[] arg) {
		switch (arg[2].toLowerCase()) {
			case "help":
				srv.send(m.channel, m.author, "What are you doing with your life?", m.channel);
				break;

			case "info":
				srv.send(m.channel, m.author, "Usage: $user info _nick_", m.channel);
				srv.send(m.channel, m.author, "Function: lists everything known about said user", m.channel);
				break;

			case "list":
				srv.send(m.channel, m.author, "Usage: $user list", m.channel);
				srv.send(m.channel, m.author, "Function: lists all users on channels " + srv.nick.name + " is joined in.", m.channel);
				break;

			case "note":
				srv.send(m.channel, m.author, "Usage: $user note _nick_ _text_", m.channel);
				srv.send(m.channel, m.author, "Function: sends _text_ to _nick_ when (s)he is active next", m.channel);
				break;

			case "kick":
				srv.send(m.channel, m.author, "Usage: $user kick _nick_ [reason]", m.channel);
				srv.send(m.channel, m.author, "Function: kicks _nick_ from current channel. [reason] is optional.", m.channel);
				break;
		}
	}

	private void kick(Message m, Server srv, String[] arg) {
		if(!m.channel.startsWith("#")){
			srv.send(m.channel, m.author, "There is nobody to kick.", m.channel);
			return;

		}

		User u = srv.getUser(m.author);
		if(u.getChan(m.channel).isUserLevel(Chan.ULevel.VOPP)){
			if(srv.nick.getChan(m.channel).isUserLevel(Chan.ULevel.OPP)){

				if(srv.nick.name.equalsIgnoreCase(arg[2])){
					srv.send("KICK "+ m.channel +" "+ u.name +" :You don't get to kick me around.", m.channel);

				} else {
					User k = getUser(srv, arg[2], m.channel);

					if(k == null){
						srv.send(m.channel, m.author, arg[2] +" not found in "+ m.channel +"!", m.channel);
						return;
					}

					if(arg.length == 3){
						srv.send("KICK "+ m.channel +" "+ k.name +" :Sorry! I was forced to perform this action.", m.channel);

					} else if(arg.length >= 4){
						srv.send("KICK "+ m.channel +" "+ k.name +" :"+ m.text.replace("$user kick "+ k.name +" ", ""), m.channel);

					}
				}

			} else {
				srv.send(m.channel, m.author, "I am not "+ Chan.ULevel.OPP.text, m.channel);
			}

		} else {
			srv.send(m.channel, m.author, "You do know you must be "+ Chan.ULevel.VOPP.text +", right?", m.channel);
		}
	}

	private User getUser(Server srv, String name, String chan) {
		User u = srv.getUser(name);

		if(u != null){
			Chan c = u.getChan(chan);

			if(c != null){
				return u;
			}
		}

		return null;
	}

	private void note(Message m, Server srv, String[] arg) {
		srv.send(m.channel, m.author, "unimplemented feature!", m.channel);
	}

	private void list(Message m, Server srv) {
		String s = "";

		for(User u : srv.getUsers()){
			s += u.name +" ";
		}

		srv.send(m.channel, m.author, s, m.channel);
	}

	private void info(Message m, Server srv, String[] arg) {
		User u = srv.getUser(arg[2]);

		if (u != null) {
			srv.send(m.channel, m.author, u.toString(), m.channel);

		} else {
			srv.send(m.channel, m.author, "user '"+ arg[2] +"' not known!", m.channel);
		}
	}

	@Override
	public String[] reserved() {
		return new String[]{ "$user" };
	}
}
