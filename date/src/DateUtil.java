import bot.nat.sumi.ConfigFile;
import bot.nat.sumi.Main;
import bot.nat.sumi.Message;
import bot.nat.sumi.Server;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class DateUtil {
	/* stops any current dates you are having */
	static void stopDate(Server srv, Message m, String[] arg) {
		ConfigFile cfg = Utils.openUserConfig(m.author);

		if(cfg == null){
			Utils.send(srv, m.channel, m.author, "You do not have a profile! Please create using '"+ Main.cmd +"date profile create'", m.channel);
			return;
		}

		if(!cfg.containsSection("date.current")){
			Utils.send(srv, m.channel, m.author, "You have no active date.", m.channel);
			return;
		}

		ConfigFile date = Utils.getDateFileFromName(cfg.getSection("date.current").getField("date").getValue());
		Utils.calcDateEnd(cfg, date);
		Utils.send(srv, m.channel, m.author, "Your date has ended!", m.channel);
	}

	/* arranges new person to date with */
	static void newDate(Server srv, Message m, String[] arg) {
		Utils.chkDateTimeout(Utils.openUserConfig(m.author));
		ConfigFile cfg = Utils.openUserConfig(m.author);

		if(cfg == null){
			Utils.send(srv, m.channel, m.author, "You do not have a profile! Please create using '"+ Main.cmd +"date profile create'", m.channel);
			return;
		}

		if(cfg.containsSection("date.current")){
			Utils.send(srv, m.channel, m.author, "You can not date multiple people at once!", m.channel);
			return;
		}

		if(!Utils.isDateReady(cfg)){
			Utils.send(srv, m.channel, m.author, "You must still set more information about yourself!", m.channel);
			return;
		}

		if(arg.length >= 3){
			/* requested person mode */
			if(cfg.getSection("people met").containsSection(arg[2])){
				if(newDate(arg[2])){
					if(isFreeDate(arg[2])){
						initDate(cfg, arg[2], srv, m);

					} else {
						Utils.send(srv, m.channel, m.author, "Person '"+ arg[2] +"' is currently on a date.", m.channel);
					}

				} else {
					Utils.send(srv, m.channel, m.author, "Person '"+ arg[2] +"' did not want to date you at this time.", m.channel);
				}

			} else {
				Utils.send(srv, m.channel, m.author, "Unknown person: '"+ arg[2] +"'", m.channel);
			}

		} else {
			/* date finder mode */
			int times = 0;
			Random r = new Random(System.currentTimeMillis());
			ConfigFile[] all = Utils.loadDates();

			/* loop 10 times trying to find a person to date */
			for(int i = 0;i < 10;i ++){
				ConfigFile chk = all[Math.abs(r.nextInt() % all.length)];

				/* if not compatible, give another try */
				if(!chkDateCompatibility(cfg, chk)){
					i --;
					times ++;

					/* except if failed compatibility already 50 times, then stop trying */
					if(times >= 50){
						break;
					}

				} else {
					if(newDate(chk)){
						if(isFreeDate(chk)){
							initDate(cfg, chk, srv, m);
							return;
						}
					}
				}
			}

			Utils.send(srv, m.channel, m.author, "Did not find anyone to date with!", m.channel);
		}
	}

	/* check if person is already not on a date */
	private static boolean isFreeDate(String person) {
		ConfigFile cfg = Utils.getDateFileFromName(person);
		return cfg != null && isFreeDate(cfg);
	}

	/* check if person is already not on a date */
	private static boolean isFreeDate(ConfigFile chk) {
		return chk.getSection("var").getField("date").getValue().replace(" ", "").equals("") || Utils.getDateTimeout(chk);
	}

	/* check if dates are compatible */
	private static boolean chkDateCompatibility(ConfigFile req, ConfigFile date) {
		return  (Integer.parseInt(req.getField("age min").getValue()) <= Integer.parseInt(date.getField("age").getValue())) &&
				(Integer.parseInt(req.getField("age max").getValue()) >= Integer.parseInt(date.getField("age").getValue())) &&
				((date.getField("gender").getValue().equals("mf") || req.getField("interested").getValue().contains(date.getField("gender").getValue())) &&
				((req.getField("gender").getValue().equals("mf") || date.getField("interested").getValue().contains(req.getField("gender").getValue()))));
	}

	/* get ConfigFile of said date */
	private static boolean newDate(String person) {
		ConfigFile cfg = Utils.getDateFileFromName(person);
		return cfg != null && newDate(cfg);
	}

	/* randomize if date could be dated with */
	private static boolean newDate(ConfigFile chk) {
		return Math.random() < Long.parseLong(chk.getField("date rate").getValue()) / 100D;
	}

	/* sets variables about date */
	private static void initDate(ConfigFile cfg, String person, Server srv, Message m) {
		initDate(cfg, Utils.getDateFileFromName(person), srv, m);
	}

	/* sets variables about date */
	private static void initDate(ConfigFile cfg, ConfigFile date, Server srv, Message m) {
		ConfigFile.Section s = cfg.createSection("date.current");
		s.setField("time", System.currentTimeMillis() +"");
		s.setField("action", System.currentTimeMillis() +"");
		s.setField("date", date.getField("name").getValue());
		s.setField("question", " ");

		/* if already contains this section, don't reset values */
		if(!cfg.getSection("people met").containsSection(date.getField("name").getValue())) {
			/* else create it and initialize values */
			ConfigFile.Section met = cfg.getSection("people met").createSection(date.getField("name").getValue());
			met.setField("experience", "0");
			met.setField("relation", "0");
		}

		cfg.flush();

		date.getSection("var").setField("date", cfg.getField("name").getValue());
		date.flush();

		Utils.send(srv, m.channel, m.author, "Date started successfully with "+ date.getField("name").getValue() +"!", m.channel);
	}
}
