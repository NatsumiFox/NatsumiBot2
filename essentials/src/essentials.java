import bot.nat.sumi.Message;
import bot.nat.sumi.Module;
import bot.nat.sumi.Server;

import java.util.Timer;
import java.util.TimerTask;

public class essentials extends Module {
	@Override
	public void command(Message m, Server srv) {
		String[] arg = m.text.split(" ");

		if(arg.length == 2){
			if(arg[0].equalsIgnoreCase("$join")){
				srv.send("JOIN "+ arg[1], m.channel);

			} else if(arg[0].equalsIgnoreCase("$quit")){
				srv.send("QUIT "+ arg[1], m.channel);
				quit();

			} else if(arg[0].equalsIgnoreCase("$part")){
				srv.send("PART "+ m.channel +" "+ arg[1], m.channel);

			} else if(arg[0].equalsIgnoreCase("$invite")){
				srv.send("INVITE "+ m.channel +" "+ arg[1], m.channel);

			}

		} else if(arg.length == 3){
			if(arg[0].equalsIgnoreCase("$part")){
				srv.send("PART "+ arg[1] +" "+ arg[2], m.channel);

			} else if(arg[0].equalsIgnoreCase("$invite")){
				srv.send("INVITE "+ arg[1] +" "+ arg[2], m.channel);
			}

		} else if(arg[0].equalsIgnoreCase("$quit")){
			srv.send("QUIT", m.channel);
			quit();

		} else if(arg[0].equalsIgnoreCase("$part")){
			srv.send("PART "+ m.channel, m.channel);

		} else {
			srv.send(m.channel, m.author, "Not enough arguments!", m.channel);
		}
	}

	private void quit() {
		new Timer("quit").schedule(new TimerTask() {
			@Override
			public void run() {
				System.exit(0);
			}
		}, 300);
	}

	@Override
	public String[] reserved() {
		return new String[]{ "$join", "$part", "$quit", "$invite", };
	}
}
