package bot.nat.sumi;

import java.util.ArrayList;
import java.util.Arrays;

public class Chan {

	public enum ULevel {
		VOP("+", "voice"), HOP("%", "half-op"), OP("@", "op"), AOP("&", "admin"), QOP("~", "owner"),
		VOPP("+%@&~", "voice or higher"), HOPP("%@&~", "half-op or higher"), OPP("@", "op or higher"), AOPP("&~", "admin or owner");

		public final String value;
		public final String text;

		ULevel(String ex, String text) {
			value = ex;
			this.text = text;
		}
	}

	public String name;
	public String level;

	public Chan(String chan) {
		name = User.exUserMode(chan);
		level = User.getUserMode(chan);
	}

	/* checks if user level matches */
	public boolean isUserLevel(ULevel u){
		if (!level.equals("")) {
			for (char c : u.value.toCharArray()) {
				if (c == level.toCharArray()[0]) {
					return true;
				}
			}
		}

		return false;
	}

	/* adds user level specified to user level stack */
	public void addUserLevel(ULevel l) {
		char[] userModeOrder = "~&@%+".toCharArray();
		char[] lev = l.value.toCharArray();
		ArrayList<Character> cur = new ArrayList<Character>();

		for(char c : level.toCharArray()){
			cur.add(c);
		}

		for(char add : lev){
			for(char own : cur.toArray(new Character[cur.size()])){
				if(add == own){
					break;
				}

				for(char chk : userModeOrder){
					if(own == chk){
						cur.add(cur.indexOf(own), add);
					}
				}

				cur.add(cur.size(), add);
			}

			if(cur.size() == 0){
				cur.add(add);
			}
		}

		level = Arrays.toString(cur.toArray(new Character[cur.size()])).replace(", ", "").replace("[", "").replace("]", "");
	}

	/* remove user level */
	public void rmvUserLevel(ULevel l){
		for(char value : l.value.toCharArray()){
			level = level.replace(""+ value, "");
		}
	}

	@Override
	public String toString(){
		return (level.equals("") ? "" : level.charAt(0)) + name;
	}
}
