import bot.nat.sumi.ConfigFile;
import bot.nat.sumi.Main;
import bot.nat.sumi.Message;
import bot.nat.sumi.Server;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Question {
	/* asks a new question from dater */
	public static void next(Server srv, Message m, String[] arg) {
		ConfigFile cfg = Utils.openUserConfig(m.author);

		/* load target question */
		ConfigFile[] aq = loadQuestions();
		ConfigFile q = aq[Math.abs(new Random(System.currentTimeMillis()).nextInt() % aq.length)];

		/* set question */
		String[] t = q.getFile().replace("\\", "/").split("/");
		cfg.getSection("date.current").setField("question", t[t.length - 1].replace(".txt", ""));
		cfg.getSection("date.current").setField("action", ""+ System.currentTimeMillis());
		cfg.flush();

		/* print out question */
		Utils.send(srv, m.channel, m.author, cfg.getSection("date.current").getField("date").getValue() +" asks: "+ q.getField("q").getValue(), m.channel);
		Utils.send(srv, m.channel, m.author, "1: "+ q.getField("a1").getValue(), m.channel);
		Utils.send(srv, m.channel, m.author, "2: "+ q.getField("a2").getValue(), m.channel);
		Utils.send(srv, m.channel, m.author, "3: "+ q.getField("a3").getValue(), m.channel);
	}

	/* parses your answer */
	public static void answer(Server srv, Message m, String ans) {
		switch (ans) {
			case "1":case "2":case "3": {
				/* load configs */
				ConfigFile cfg = Utils.openUserConfig(m.author);
				ConfigFile date = Utils.getDateFileFromName(cfg.getSection("date.current").getField("date").getValue());
				ConfigFile q = openQuestion(cfg.getSection("date.current").getField("question").getValue());
				int response = 0;

				/* check positive response */
				if (chkTrait(Utils.Traits.getTrait(Integer.parseInt(q.getField("a" + ans + "e").getValue())),
						Utils.Traits.getTrait(Integer.parseInt(date.getField("desired trait").getValue())))) {
					response = 1;
					addRelation(cfg.getSection("people met").getSection(date.getField("name").getValue()), 1);
				}

				/* check negative response */
				if (chkTrait(Utils.Traits.getTrait(Integer.parseInt(q.getField("a" + ans + "e").getValue())),
						Utils.Traits.getTrait(Integer.parseInt(date.getField("undesired trait").getValue())))) {
					response = 2;
					addRelation(cfg.getSection("people met").getSection(date.getField("name").getValue()), -1);
				}

				addExp(cfg.getSection("people met").getSection(date.getField("name").getValue()), response << 4);
				cfg.getSection("date.current").setField("question", " ");
				cfg.getSection("date.current").setField("action", "" + System.currentTimeMillis());
				cfg.flush();

				Utils.send(srv, m.channel, m.author, "your answer '"+ q.getField("a" + ans).getValue() +"' was met with "+
						getDesc(response) +" response.", m.channel);
				break;
			}

			case "skip":
				ConfigFile cfg = Utils.openUserConfig(m.author);
				cfg.getSection("date.current").setField("question", " ");
				cfg.flush();

				Utils.send(srv, m.channel, m.author, "Question skipped.", m.channel);
				break;

			default:
				Utils.send(srv, m.channel, m.author, "answer must be 1, 2 or 3, or skip", m.channel);
				break;
		}
	}

	/* response type */
	private static String getDesc(int response) {
		switch (response){
			case 0:
				return "neutral";

			case 1:
				return "positive";

			case 2:
				return "negative";

			default:
				return null;
		}
	}

	/* adds experience */
	private static void addExp(ConfigFile.Section s, int add) {
		s.setField("experience", ""+ (Integer.parseInt(s.getField("experience").getValue()) + add));
	}

	/* add relation to already existing */
	private static void addRelation(ConfigFile.Section s, int add) {
		s.setField("relation", ""+ (Integer.parseInt(s.getField("relation").getValue()) + add));
	}

	/* check two traits */
	private static boolean chkTrait(Utils.Traits val, Utils.Traits and) {
		return (val.value & and.value) != 0;
	}

	/* gets question based on ID */
	private static ConfigFile openQuestion(String id) {
		File f = new File(date.folder +"questions/"+ id +".txt");
		if(f.exists()){
			return new ConfigFile(f, ConfigFile.READ);
		}

		return null;
	}

	/* load all dates to array */
	public static ConfigFile[] loadQuestions() {
		ArrayList<ConfigFile> configs = new ArrayList<>();
		File[] files = new File(date.folder +"questions/").listFiles();

		assert files != null;
		for(File f : files){
			configs.add(new ConfigFile(f, ConfigFile.READ));
		}

		return configs.toArray(new ConfigFile[configs.size()]);
	}

	/* remove question */
	public static void rmv(Server srv, Message m, String[] arg) {
		if(!srv.getUser(m.author).isBotOp){
			srv.send(m.channel, m.author, "you must be botOP to do this!", m.channel);
			return;
		}

		if(arg.length != 3){
			srv.send(m.channel, m.author, "Illegal arguments!", m.channel);
			return;
		}

		ConfigFile q = openQuestion(arg[2]);
		if(q == null){
			srv.send(m.channel, m.author, "Question '"+ arg[2] +"' not found!", m.channel);
			return;
		}

		/* delete the file */
		if(new File(q.getFile()).delete()){
			srv.send(m.channel, m.author, "Question '"+ q.getFile() +"' successfully deleted!", m.channel);

		} else {
			srv.send(m.channel, m.author, "Question '"+ q.getFile() +"' could not be deleted!", m.channel);
		}
	}

	/* add question */
	public static void add(Server srv, Message m, String[] arg) {
		if(!srv.getUser(m.author).isBotOp){
			srv.send(m.channel, m.author, "you must be botOP to do this!", m.channel);
			return;
		}

		if(arg.length < 3){
			srv.send(m.channel, m.author, "Illegal arguments!", m.channel);
			return;
		}

		ConfigFile cfg = new ConfigFile(date.folder +"questions/"+ loadQuestions().length +".txt", ConfigFile.WRITE | ConfigFile.READ);
		String tmp = m.text.replace(Main.cmd +"date qadd ", "");

		/* set question */
		cfg.setField("q", tmp.substring(0, tmp.indexOf(':')));
		tmp = tmp.substring(tmp.indexOf(':') + 1, tmp.length());
		/* set answer 1 */
		cfg.setField("a1e", tmp.substring(0, tmp.indexOf(';')));
		tmp = tmp.substring(tmp.indexOf(';') + 1, tmp.length());
		cfg.setField("a1", tmp.substring(0, tmp.indexOf(':')));
		tmp = tmp.substring(tmp.indexOf(':') + 1, tmp.length());
		/* set answer 3 */
		cfg.setField("a2e", tmp.substring(0, tmp.indexOf(';')));
		tmp = tmp.substring(tmp.indexOf(';') + 1, tmp.length());
		cfg.setField("a2", tmp.substring(0, tmp.indexOf(':')));
		tmp = tmp.substring(tmp.indexOf(':') + 1, tmp.length());
		/* set answer 3 */
		cfg.setField("a3e", tmp.substring(0, tmp.indexOf(';')));
		tmp = tmp.substring(tmp.indexOf(';') + 1, tmp.length());
		cfg.setField("a3", tmp);

		cfg.flush();
		srv.send(m.channel, m.author, "Question '"+ cfg.getFile() +"' created!", m.channel);
	}

	/* print information about the question */
	public static void see(Server srv, Message m, String[] arg) {
		if(!srv.getUser(m.author).isBotOp){
			srv.send(m.channel, m.author, "you must be botOP to do this!", m.channel);
			return;
		}

		if(arg.length != 3){
			srv.send(m.channel, m.author, "Illegal arguments!", m.channel);
			return;
		}

		ConfigFile q = openQuestion(arg[2]);
		if(q == null){
			srv.send(m.channel, m.author, "Question '"+ arg[2] +"' not found!", m.channel);
			return;
		}

		String[] arr = new String[]{ "q: "+ q.getField("q").getValue(),
				"1: "+ q.getField("a1e").getValue() +";"+ q.getField("a1").getValue(),
				"2: "+ q.getField("a2e").getValue() +";"+ q.getField("a2").getValue(),
				"3: "+ q.getField("a3e").getValue() +";"+ q.getField("a3").getValue(), };
		srv.send(m.channel, m.author, Arrays.toString(arr), m.channel);
	}
}
