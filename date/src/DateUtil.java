import bot.nat.sumi.ConfigFile;
import bot.nat.sumi.Message;
import bot.nat.sumi.Server;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class DateUtil {
	static void stopDate(Server srv, Message m, String[] arg) {
		ConfigFile cfg = Utils.openUserConfig(m.author);

		if(cfg == null){
			srv.send(m.channel, m.author, "You do not have a profile! Please create using '$date profile create'", m.channel);
			return;
		}

		if(!cfg.containsSection("date.current")){
			srv.send(m.channel, m.author, "You have no active date.", m.channel);
			return;
		}

		ConfigFile date = getDateFileFromName(cfg.getSection("date.current").getField("date").getValue());
		date.getSection("var").getField("date").setValue(" ");
		date.flush();

		cfg.rmvSection("date.current");
		cfg.flush();

		srv.send(m.channel, m.author, "Your date has ended!", m.channel);
	}

	/* arranges new person to date with */
	static void newDate(Server srv, Message m, String[] arg) {
		ConfigFile cfg = Utils.openUserConfig(m.author);

		if(cfg == null){
			srv.send(m.channel, m.author, "You do not have a profile! Please create using '$date profile create'", m.channel);
			return;
		}

		if(cfg.containsSection("date.current")){
			srv.send(m.channel, m.author, "You can not date multiple people at once!", m.channel);
			return;
		}

		if(!Utils.isDateReady(cfg)){
			srv.send(m.channel, m.author, "You must still set more information about yourself!", m.channel);
			return;
		}

		if(arg.length >= 3){
			if(cfg.getSection("people met").containsSection(arg[2])){
				if(newDate(arg[2])){
					if(isFreeDate(arg[2])){
						initDate(cfg, arg[2], srv, m);

					} else {
						srv.send(m.channel, m.author, "Person '"+ arg[2] +"' is currently on a date.", m.channel);
					}

				} else {
					srv.send(m.channel, m.author, "Person '"+ arg[2] +"' did not want to date you at this time.", m.channel);
				}

			} else {
				srv.send(m.channel, m.author, "Unknown person: '" + arg[2] + "'", m.channel);
			}

		} else {
			int times = 0;
			Random r = new Random(System.currentTimeMillis());
			ConfigFile[] all = loadDates();

			for(int i = 0;i < 10;i ++){
				ConfigFile chk = all[r.nextInt() % all.length];

				if(!chkDateCompatibility(cfg, chk)){
					i --;
					times ++;

					if(times >= 50){
						break;
					}

				} else {
					if(newDate(chk)){
						if(isFreeDate(arg[2])){
							initDate(cfg, chk, srv, m);
							return;
						}
					}
				}
			}

			srv.send(m.channel, m.author, "Did not find anyone to date with!", m.channel);
		}
	}

	/* check if person is already not on a date */
	private static boolean isFreeDate(String person) {
		ConfigFile cfg = getDateFileFromName(person);
		return cfg != null && isFreeDate(cfg);
	}

	/* check if person is already not on a date */
	private static boolean isFreeDate(ConfigFile chk) {
		return chk.getSection("var").getField("date").getValue().equals(" ");
	}

	/* check if dates are compatible */
	private static boolean chkDateCompatibility(ConfigFile req, ConfigFile date) {
		return  (Integer.parseInt(req.getField("age min").getValue()) <= Integer.parseInt(date.getField("age").getValue())) &&
				(Integer.parseInt(req.getField("age max").getValue()) >= Integer.parseInt(date.getField("age").getValue())) &&
				(req.getField("interested").getValue().contains(date.getField("gender").getValue()) &&
						(date.getField("interested").getValue().contains(req.getField("gender").getValue())));
	}

	/* get ConfigFile of said date */
	private static boolean newDate(String person) {
		ConfigFile cfg = getDateFileFromName(person);
		return cfg != null && newDate(cfg);
	}

	/* randomize if date could be dated with */
	private static boolean newDate(ConfigFile chk) {
		return Math.random() < Long.parseLong(chk.getField("date rate").getValue()) / 100D;
	}

	/* sets variables about date */
	private static void initDate(ConfigFile cfg, String person, Server srv, Message m) {
		initDate(cfg, getDateFileFromName(person), srv, m);
	}

	/* sets variables about date */
	private static void initDate(ConfigFile cfg, ConfigFile date, Server srv, Message m) {
		ConfigFile.Section s = cfg.createSection("date.current");
		s.setField("time", System.currentTimeMillis() +"");
		s.setField("action", System.currentTimeMillis() +"");
		s.setField("date", date.getField("name").getValue());
		cfg.getSection("people met").createSection(date.getField("name").getValue());
		cfg.flush();

		date.getSection("var").setField("date", cfg.getField("name").getValue());
		date.flush();

		srv.send(m.channel, m.author, "Date started successfully with "+ date.getField("name").getValue() +"!", m.channel);
	}

	/* load all dates to array */
	private static ConfigFile[] loadDates() {
		ArrayList<ConfigFile> configs = new ArrayList<>();
		File[] files = new File(date.folder +"dates/").listFiles();

		assert files != null;
		for(File f : files){
			configs.add(new ConfigFile(f, ConfigFile.READ | ConfigFile.WRITE));
		}

		return configs.toArray(new ConfigFile[configs.size()]);
	}

	private static ConfigFile getDateFileFromName(String person) {
		for(ConfigFile f : loadDates()){
			if(f.getField("name").getValue().equals(person)){
				return f;
			}
		}

		return null;
	}
}
