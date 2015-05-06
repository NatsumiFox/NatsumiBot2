import bot.nat.sumi.Message;
import bot.nat.sumi.Module;
import bot.nat.sumi.Server;
import bot.nat.sumi.User;

public class userInfo extends Module {
	@Override
	public void command(Message m, Server srv) {
		String s = "";

		for(User u : srv.getUsers()){
			s += u.name +" ";
		}

		srv.send(m.channel, m.author, s, m.channel);
	}

	@Override
	public String[] reserved() {
		return new String[]{ "!listusers" };
	}
}
