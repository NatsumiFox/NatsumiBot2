import bot.nat.sumi.Message;
import bot.nat.sumi.Module;
import bot.nat.sumi.Server;

public class help extends Module {
	@Override
	public void command(Message m, Server srv) {
		srv.send(m.channel, m.author, list(srv), m.channel);
	}

	private String list(Server srv) {
		String ret = "";
		for(Module m : srv.getModules()){
			for(String c : m.reserved()){
				ret += c +" ";
			}
		}

		return ret.replace("  ", "");
	}

	@Override
	public String[] reserved() {
		return new String[]{ "$help" };
	}
}
