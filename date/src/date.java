import bot.nat.sumi.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class date extends Module {
	static String folder = Main.folder +"date/";

	@Override
	public void command(Message m, Server srv) {
		String[] arg = m.text.split(" ");

		if(arg.length <= 1 || arg[1].equalsIgnoreCase("help")){
			if(arg.length >= 3){
				help(srv, m, arg);

			} else {
				help(srv, m);
			}
		} else {
			switch (arg[1]){
				case "profile":
					Profile.command(srv, m, arg);
					return;

				case "new":
					DateUtil.newDate(srv, m, arg);
					return;

				case "stop":
					DateUtil.stopDate(srv, m, arg);
					return;

				case "info":
					info(srv, m, arg);
					return;

				case "q":
					q(srv, m, arg);
					return;

				case "qadd":
					Question.add(srv, m, arg);
					return;

				case "qrmv":case "qdel":
					Question.rmv(srv, m, arg);
					return;

				case "qsee":
					Question.see(srv, m, arg);
					return;

				default:
					help(srv, m);
			}
		}
	}

	private void q(Server srv, Message m, String[] arg) {
		ConfigFile cfg = Utils.openUserConfig(m.author);

		if(cfg == null){
			Utils.send(srv, m.channel, m.author, "You do not have a profile! Please create using '"+ Main.cmd +"date profile create'", m.channel);
			return;
		}

		if(!cfg.containsSection("date.current")){
			Utils.send(srv, m.channel, m.author, "You have no active date.", m.channel);
			return;
		}

		if(Utils.chkDateTimeout(cfg)){
			Utils.send(srv, m.channel, m.author, "Your date just ended.", m.channel);
			return;
		}

		if(cfg.getSection("date.current").getField("question").getValue().replace(" ", "").equals("")){
			Question.next(srv, m, arg);

		} else if(arg.length == 3){
			Question.answer(srv, m, arg[2]);

		} else {
			Utils.send(srv, m.channel, m.author, "Illegal arguments", m.channel);
		}
	}

	private void info(Server srv, Message m, String[] arg) {
		if(arg.length < 3){
			Utils.send(srv, m.channel, m.author, "Please supply date name!", m.channel);
			return;
		}

		if(Utils.openUserConfig(m.author) == null){
			Utils.send(srv, m.channel, m.author, "You do not have a profile! Please create using '$date profile create'", m.channel);
			return;
		}

		Utils.chkDateTimeout(Utils.openUserConfig(m.author));
		String[] fields = Utils.dateInfoFields();
		ConfigFile cfg = Utils.openDateConfig(arg[2]);

		if(cfg == null || !Utils.openUserConfig(m.author).getSection("people met").containsSection(arg[2])){
			Utils.send(srv, m.channel, m.author, "Unknown person: '"+ arg[2] +"'", m.channel);
			return;
		}

		ArrayList<ConfigFile.Field> fls = new ArrayList<>();

		for(String field : fields){
			if(cfg.containsField(field)){
				fls.add(cfg.getField(field));
			}
		}

		Utils.send(srv, m.channel, m.author, Arrays.toString(fls.toArray(new ConfigFile.Field[fls.size()])) +
				", currently "+ (cfg.getSection("var").getField("date").getValue().equals(" ") ? "isn't on a date" :
				"is on a date with '"+ cfg.getSection("var").getField("date").getValue() +"'"), m.channel);
	}

	private void help(Server srv, Message m, String[] arg) {
		switch (arg[2]){
			case "profile":
				Utils.send(srv, m.channel, m.author, "Set information about your profile", m.channel);
				Utils.send(srv, m.channel, m.author, "Available commands: create, reset, set, met, info, help", m.channel);
				return;

			case "new":
				Utils.send(srv, m.channel, m.author, "Arrange a new date. You can additionally supply person's name to date him/her, if you already know each other.", m.channel);
				return;

			case "stop":
				Utils.send(srv, m.channel, m.author, "Stops your current date. Person you are dating probably won't be happy about it.", m.channel);
				return;

			case "info":
				Utils.send(srv, m.channel, m.author, "Get information about a person you've met. You must supply a name", m.channel);
				return;

			case "q":
				Utils.send(srv, m.channel, m.author, "Makes your date ask you a question, and give 3 basic answers. After you use this, you use '"+
						Main.cmd +"date q 1/2/3' to answer it.", m.channel);
				return;


			default:
				help(srv, m);
		}
	}

	private void help(Server srv, Message m) {
		Utils.send(srv, m.channel, m.author, "Available commands: profile, info, new, stop, q, help", m.channel);
		Utils.send(srv, m.channel, m.author, "Usage: "+ Main.cmd +"date help _command_", m.channel);
	}

	@Override
	public String[] reserved() {
		return new String[]{ Main.cmd +"date" };
	}
}
