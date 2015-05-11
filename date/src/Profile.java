import bot.nat.sumi.ConfigFile;
import bot.nat.sumi.Message;
import bot.nat.sumi.Server;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class Profile {
	public static void command(Server srv, Message m, String[] arg) {
		if(arg.length <= 2 || arg[2].equalsIgnoreCase("help")){
			if(arg.length >= 4){
				help(srv, m, arg);

			} else {
				help(srv, m);
			}
		} else {
			switch (arg[2]){
				case "create":
					create(srv, m);
					return;

				case "reset":
					reset(srv, m);
					return;

				case "set":
					set(srv, m, arg);
					return;

				case "info":
					info(srv, m);
					return;

				default:
					help(srv, m);
			}
		}
	}

	/* send information about your profile */
	private static void info(Server srv, Message m) {
		String[] fields = Utils.profileFieldsRequired();
		ConfigFile cfg = Utils.openUserConfig(m.author);
		ArrayList<ConfigFile.Field> fls = new ArrayList<>();

		for(String field : fields){
			if(cfg.containsField(field)){
				fls.add(cfg.getField(field));
			}
		}

		srv.send(m.channel, m.author, Arrays.toString(fls.toArray(new ConfigFile.Field[fls.size()])), m.channel);
	}

	/* set data to fields */
	private static void set(Server srv, Message m, String[] arg) {
		if(arg.length <= 4){
			help(srv, m, arg);
			return;
		}

		ConfigFile cfg = Utils.openUserConfig(m.author);

		if(cfg == null){
			srv.send(m.channel, m.author, "You do not have a profile! Please create using '$date profile create'", m.channel);
			return;
		}

		switch (arg[3]){
			case "age":
				if(arg[4].matches("[0-9]+")){
					int age = Integer.parseInt(arg[4]);

					if(age < 13){
						srv.send(m.channel, m.author, "You sure you are old enough for this?", m.channel);
						return;

					} else if(age >= 40){
						srv.send(m.channel, m.author, "You old fuck, man!", m.channel);
						return;
					}

					cfg.setField("age", age +"");
					cfg.flush();
					srv.send(m.channel, m.author, "Your age is set to "+ age, m.channel);
					return;

				} else {
					srv.send(m.channel, m.author, "You must supply a number.", m.channel);
					return;
				}

			case "age_min":
				int age = getAge(srv, m, arg[4]);

				if(age != -1){
					if(cfg.containsField("age max")){
						int max = Integer.parseInt(cfg.getField("age max").getValue());

						if(age > max){
							srv.send(m.channel, m.author, "Maximum age is already set to '"+ max +"'!", m.channel);
							return;

						} else {
							cfg.setField("age min", age +"");
							cfg.flush();
							srv.send(m.channel, m.author, "Minimum accepted age is set to "+ age, m.channel);
						}

					} else {
						cfg.setField("age min", age +"");
						cfg.flush();
						srv.send(m.channel, m.author, "Minimum accepted age is set to "+ age, m.channel);
					}
				}

				return;

			case "age_max":
				int age2 = getAge(srv, m, arg[4]);

				if(age2 != -1){
					if(cfg.containsField("age min")){
						int min = Integer.parseInt(cfg.getField("age min").getValue());

						if(age2 < min){
							srv.send(m.channel, m.author, "Minimum age is already set to '"+ min +"'!", m.channel);
							return;

						} else {
							cfg.setField("age max", age2 +"");
							cfg.flush();
							srv.send(m.channel, m.author, "Maximum accepted age is set to "+ age2, m.channel);
						}

					} else {
						cfg.setField("age max", age2 +"");
						cfg.flush();
						srv.send(m.channel, m.author, "Maximum accepted age is set to "+ age2, m.channel);
					}
				}

				return;

			case "gender":
				switch (arg[4]){
					case "male":case "boy":case "man":
						cfg.setField("gender", "m");
						cfg.flush();
						srv.send(m.channel, m.author, "Your gender is set to "+ arg[4], m.channel);
						return;

					case "female":case "girl":case "woman":
						cfg.setField("gender", "f");
						cfg.flush();
						srv.send(m.channel, m.author, "Your gender is set to "+ arg[4], m.channel);
						return;

					default:
						srv.send(m.channel, m.author, "Gender '"+ arg[4] +"' not recognized!", m.channel);
						return;

				}

			case "orientation":
				if(!cfg.containsField("gender")){
					srv.send(m.channel, m.author, "You must set your gender first!", m.channel);
					return;
				}

				boolean isMale = cfg.getField("gender").getValue().equals("m");
				switch (arg[4]){
					case "bi":
						cfg.setField("interested", "mf");
						cfg.flush();
						srv.send(m.channel, m.author, "Your orientation is "+ arg[4], m.channel);
						return;

					case "gay":case "homo":case "homosexual":
						cfg.setField("interested", isMale ? "m" : "f");
						cfg.flush();
						srv.send(m.channel, m.author, "Your orientation is "+ arg[4], m.channel);
						return;

					case "straight":case "nohomo":
						cfg.setField("interested", isMale ? "f" : "m");
						cfg.flush();
						srv.send(m.channel, m.author, "Your orientation is "+ arg[4], m.channel);
						return;

					default:
						srv.send(m.channel, m.author, "Orientation '"+ arg[4] +"' not recognized!", m.channel);
						return;
				}

			case "skin_color":
				switch (arg[4]){
					case "black":
						cfg.setField("skin color", "black");
						cfg.flush();
						srv.send(m.channel, m.author, "Your skin color is "+ arg[4], m.channel);
						return;

					case "dark":
						cfg.setField("skin color", "dark");
						cfg.flush();
						srv.send(m.channel, m.author, "Your skin color is "+ arg[4], m.channel);
						return;

					case "white":
						cfg.setField("skin color", "white");
						cfg.flush();
						srv.send(m.channel, m.author, "Your skin color is "+ arg[4], m.channel);
						return;

					case "light":
						cfg.setField("skin color", "light");
						cfg.flush();
						srv.send(m.channel, m.author, "Your skin color is "+ arg[4], m.channel);
						return;

					default:
						srv.send(m.channel, m.author, "Skin color '"+ arg[4] +"' not recognized!", m.channel);
						return;
				}

			case "height":
				if(arg[4].matches("[0-9]+")){
					int height = Integer.parseInt(arg[4]);

					if(height < 125){
						srv.send(m.channel, m.author, "You are kinda short for a human being.", m.channel);
						return;

					} else if(height >= 250){
						srv.send(m.channel, m.author, "Who is this tall anyways?", m.channel);
						return;
					}

					cfg.setField("height", height +"");
					cfg.flush();
					srv.send(m.channel, m.author, "Your height is set to "+ height +"cm", m.channel);

					if(cfg.containsField("weight")){
						srv.send(m.channel, m.author, "Your body type is set to '"+ setBodyType(cfg) +"'", m.channel);
					}
					return;

				} else {
					srv.send(m.channel, m.author, "You must supply a number.", m.channel);
					return;
				}

			case "weight":
				if(arg[4].matches("[0-9]+")){
					int weight = Integer.parseInt(arg[4]);

					if(weight < 30){
						srv.send(m.channel, m.author, "We do not accept skeletons.", m.channel);
						return;

					} else if(weight >= 150){
						srv.send(m.channel, m.author, "I am sure you are not going to get anyone with that weight!", m.channel);
						return;
					}

					cfg.setField("weight", weight +"");
					cfg.flush();
					srv.send(m.channel, m.author, "Your weight is set to "+ weight +"kg", m.channel);

					if(cfg.containsField("height")){
						srv.send(m.channel, m.author, "Your body type is set to '"+ setBodyType(cfg) +"'", m.channel);
					}
					return;

				} else {
					srv.send(m.channel, m.author, "You must supply a number.", m.channel);
					return;
				}
		}
	}

	/* sub used by 2 modes in set command
	 * gets age from string and sends appropriate messages if illegal */
	private static int getAge(Server srv, Message m, String s) {
		if(s.matches("[0-9]+")){
			int age = Integer.parseInt(s);

			if(age < 13){
				srv.send(m.channel, m.author, "You a pedophile or something you fuck?", m.channel);
				return -1;

			} else if(age >= 40){
				srv.send(m.channel, m.author, "You shouldn't be dating mummies.", m.channel);
				return -1;
			}

			return age;

		} else {
			srv.send(m.channel, m.author, "You must supply a number.", m.channel);
			return -1;
		}
	}

	/* reset profile */
	private static void reset(Server srv, Message m) {
		File user = Utils.openUser(m.author);

		/* if file exists */
		if(!user.exists()){
			srv.send(m.channel, m.author, "You do not have a profile!", m.channel);

		} else {
			user.delete();
			srv.send(m.channel, m.author, "Your profile has been deleted!", m.channel);
		}
	}

	/* create a profile */
	private static void create(Server srv, Message m) {
		File user = Utils.openUser(m.author);

		/* if file exists */
		if(user.exists()){
			srv.send(m.channel, m.author, "Your profile already exists! Use '$date profile reset' to delete!", m.channel);
			return;
		}

		ConfigFile cfg = new ConfigFile(user.getAbsolutePath(), ConfigFile.READ | ConfigFile.WRITE);
		cfg.setField("name", m.author);
		cfg.createSection("people met");
		cfg.flush();
		srv.send(m.channel, m.author, "Your profile was created successfully!", m.channel);
	}

	private static void help(Server srv, Message m, String[] arg) {
		if(arg.length <= 2){
			help(srv, m);
			return;
		}

		switch (arg[2]){
			case "create":
				srv.send(m.channel, m.author, "Create a profile for yourself", m.channel);
				return;

			case "reset":
				srv.send(m.channel, m.author, "Clear your profile information. NOTE: All progress will be lost forever", m.channel);
				return;

			case "set":
				srv.send(m.channel, m.author, "Available fields: age, age_min, age_max, skin_color, gender, orientation, height, weight", m.channel);
				srv.send(m.channel, m.author, "Usage: $date profile set _field_ _text_", m.channel);
				return;

			case "info":
				srv.send(m.channel, m.author, "Lists everything known about your profile", m.channel);
				return;

			default:
				help(srv, m);
		}
	}

	private static void help(Server srv, Message m) {
		srv.send(m.channel, m.author, "Available commands: create, reset, info, help", m.channel);
		srv.send(m.channel, m.author, "Usage: $date profile help _command_", m.channel);
	}

	public static String setBodyType(ConfigFile cfg) {
		int type = (Integer.parseInt(cfg.getField("height").getValue()) - 100) - Integer.parseInt(cfg.getField("weight").getValue());

		if(type <= -15){
			cfg.setField("body type", "skinny");
			cfg.flush();
			return "skinny";

		} else if(type >= -15){
			cfg.setField("body type", "thin");
			cfg.flush();
			return "thin";

		} else if(type >= -5 && type <= 5){
			cfg.setField("body type", "average");
			cfg.flush();
			return "average";

		} else if(type <= 15){
			cfg.setField("body type", "chubby");
			cfg.flush();
			return "chubby";

		} else if(type <= 25){
			cfg.setField("body type", "fat");
			cfg.flush();
			return "fat";
		}

		/* wtf happened here */
		return null;
	}
}
