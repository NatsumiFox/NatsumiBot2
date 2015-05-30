package bot.nat.sumi;

public enum Format {
	BOLD("\002", "§l", ",bold:false", false), ITALIC(String.valueOf((char) 0x1D), "§o", ",italic:true", false),
	ULINE(String.valueOf((char) 0x1F), "§n", ",underlined:true", false), OBFUS(String.valueOf((char) 0x16), "§k", ",obfuscated:true", false),
	STRIKE("\001", "§m", ",strikethrough:true", false), RESET(String.valueOf((char) 0x0F), "§r", ",color:\"gray\"", false),

	BLACK((char)3 +"01", "§0", ",color:\"black\"", true), DBLUE((char)3 +"02", "§1", ",color:\"dark_blue\"", true),
	DGREEN((char)3 +"03", "§2", ",color:\"dark_green\"", true), DAQUA((char)3 +"10", "§3", ",color:\"dark_aqua\"", true),
	DRED((char)3 +"05", "§4", ",color:\"dark_red\"", true), DPURPLE((char)3 +"06", "§5", ",color:\"dark_purple\"", true),
	GOLD((char)3 +"07", "§6", ",color:\"gold\"", true), GRAY((char)3 +"15", "§7", ",color:\"gray\"", true),
	DGRAY((char)3 +"14", "§8", ",color:\"dark_gray\"", true), BLUE((char)3 +"12", "§9", ",color:\"blue\"", true),
	GREEN((char)3 +"09", "§a", ",color:\"green\"", true), AQUA((char)3 +"11", "§b", ",color:\"aqua\"", true),
	RED((char)3 +"04", "§c", ",color:\"red\"", true), PURPLE((char)3 +"13", "§d", ",color:\"light_purple\"", true),
	YELLOW((char)3 +"08", "§e", ",color:\"yellow\"", true), WHITE((char)3 +"00", "§f", ",color:\"white\"", true);

	/* fields; i = irc String representation, c = Minecraft chat representation, j = JSON constant */
	public final String i, c, j;
	public final boolean icl;

	Format(String irc, String chat, String json, boolean isColor) {
		i = irc; c = chat; j = json;
		icl = isColor;
	}
}
