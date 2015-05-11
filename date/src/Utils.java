import bot.nat.sumi.ConfigFile;

import java.io.File;

public class Utils {
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
}
