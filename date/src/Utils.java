import bot.nat.sumi.ConfigFile;
import bot.nat.sumi.Format;
import bot.nat.sumi.Server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Utils {
	static final long DATE_TIME_MAX =	60*60*1000;
	static final long DATE_ACTION_MAX =	10*60*1000;

	/* list of trait types */
	public enum Traits {
		FAMILY(1, "FAMILY"), WEALTH(2, "WEALTH"), ENJOYMENT(4, "ENJOYMENT"),
		FAMILY_WEALTH(FAMILY.value | WEALTH.value, FAMILY.name +" & "+ WEALTH.name),
		FAMILY_ENJOYMENT(FAMILY.value | ENJOYMENT.value, FAMILY.name +" & "+ ENJOYMENT.name),
		WEALTH_ENJOYMENT(WEALTH.value | ENJOYMENT.value, WEALTH.name +" & "+ ENJOYMENT.name);

		final int value;
		final String name;

		Traits(int i, String s) {
			value = i;
			name = s;
		}

		public static Traits getTrait(int i){
			switch (i){
				case 1:
					return FAMILY;

				case 2:
					return WEALTH;

				case 3:
					return FAMILY_WEALTH;

				case 4:
					return ENJOYMENT;

				case 5:
					return FAMILY_ENJOYMENT;

				case 6:
					return WEALTH_ENJOYMENT;

				default:
					throw new IllegalArgumentException("Trait not recognized: '"+ i +"'!");
			}
		}
	}

	/* send text to server */
	public static boolean send(Server srv, String channel, String user, String msg, String sender) {
		return srv.send(channel, user, Format.Color.BROWN + msg, sender);
	}

	/* gets file which contains user information */
	public static File openUser(String file){
		return new File(date.folder +"users/"+ file +".txt");
	}

	/* opens user configuration */
	public static ConfigFile openUserConfig(String file){
		File f = openUser(file);
		if(f.exists()){
			return new ConfigFile(f, ConfigFile.READ | ConfigFile.WRITE);
		}

		return null;
	}

	public static ConfigFile openDateConfig(String file) {
		File f = new File(date.folder +"dates/"+ file +".txt");
		if(f.exists()){
			return new ConfigFile(f, ConfigFile.READ | ConfigFile.WRITE);
		}

		return null;
	}

	/* list required fields to date */
	public static String[] profileFieldsRequired() {
		return new String[]{ "name", "age", "age min", "age max", "gender", "interested", "skin color", "height", "weight", "body type" };
	}

	/* list about information of date */
	public static String[] dateInfoFields() {
		return new String[]{ "name", "age", "gender", "interested", "skin color", "height", "weight", "body type" };
	}

	/* check if profile contains all necessary fields */
	public static boolean isDateReady(ConfigFile cfg) {
		for(String s : profileFieldsRequired()){
			if(!cfg.containsField(s)){
				return false;
			}
		}

		return true;
	}

	/* checks if date should be timed out (and if so, times it out) */
	public static boolean getDateTimeout(ConfigFile get){
		return get != null && get.getSection("var").containsField("date") &&
				chkDateTimeout(openUserConfig(get.getSection("var").getField("date").getValue()));
	}

	/* checks if date should be timed out (and if so, times it out) */
	public static boolean chkDateTimeout(ConfigFile chk){
		if(chk == null || !chk.containsSection("date.current") || !chk.getSection("date.current").containsField("date")){
			return false;
		}

		/* get section and make sure contains appropriate fields */
		ConfigFile.Section s = chk.getSection("date.current");
		if(!s.containsField("action") || !s.containsField("time")){
			return false;
		}

		/* convert field Strings to values */
		long start = Long.parseLong(s.getField("time").getValue()), act = Long.parseLong(s.getField("action").getValue());
		/* check timers */
		if(start + DATE_TIME_MAX <= System.currentTimeMillis() || act + DATE_ACTION_MAX <= System.currentTimeMillis()){
			/* end date */
			calcDateEnd(chk, getDateFileFromName(s.getField("date").getValue()));
			return true;
		}

		return false;
	}

	/* does necessary operations at the end of a date */
	public static void calcDateEnd(ConfigFile cfg, ConfigFile date) {
		date.getSection("var").setField("date", " ");
		date.flush();

		cfg.rmvSection("date.current");
		cfg.flush();
	}

	/* makes sure date does exist and then loads it */
	public static ConfigFile getDateFileFromName(String person) {
		for(ConfigFile f : loadDates()){
			if(f.getField("name").getValue().equals(person)){
				return f;
			}
		}

		return null;
	}

	/* load all dates to array */
	public static ConfigFile[] loadDates() {
		ArrayList<ConfigFile> configs = new ArrayList<>();
		File[] files = new File(date.folder +"dates/").listFiles();

		assert files != null;
		for(File f : files){
			configs.add(new ConfigFile(f, ConfigFile.READ | ConfigFile.WRITE));
		}

		return configs.toArray(new ConfigFile[configs.size()]);
	}


}
