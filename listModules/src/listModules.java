import bot.nat.sumi.Main;
import bot.nat.sumi.Message;
import bot.nat.sumi.Module;
import bot.nat.sumi.Server;

import java.net.URL;

public class listModules extends Module {
	@Override
	public void command(Message m, Server srv) {
		if(!srv.getUser(m.author).isBotOp){
			srv.send(m.channel, m.author, "You need to be a BotOP to perform this", m.channel);
		}

		String s = "";

		for (URL u : Main.getURLs()) {
			s += u.getFile().replace("/"+ Main.folder.replace("\\", "/") +"commands/", "") +" ";
		}

		srv.send(m.channel, m.author, s, m.channel);
	}

	@Override
	public String[] reserved() {
		return new String[]{ "$listmodules" };
	}
}
