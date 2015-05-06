import bot.nat.sumi.Message;
		import bot.nat.sumi.Module;
		import bot.nat.sumi.Server;
		import bot.nat.sumi.User;

		import java.util.Arrays;

public class userData extends Module {
	@Override
	public void command(Message m, Server srv) {
		User u = srv.getUser(m.text.toLowerCase().replace("$userdata ", ""));

		if (u != null) {
			String s = "";

			for (String str : u.chans) {
				s += str + " ";
			}

			srv.send(m.channel, m.author, u.userName + "@" + u.ident + ": " + u.realName + " in " + u.chans, m.channel);

		} else {
			srv.send(m.channel, m.author, "user '" + m.text.toLowerCase().replace("!userdata ", "") + "' not found!", m.channel);
		}
	}

	@Override
	public String[] reserved() {
		return new String[]{ "$userdata " };
	}
}
