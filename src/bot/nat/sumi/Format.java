package bot.nat.sumi;

public final class Format {
	public final static char BOLD =		0x02;
	public final static char ITALIC =	0x1D;
	public final static char ULINE =	0x1F;
	public final static char SWAP =		0x16;
	public final static char RESET =	0x0F;

	public static final class Color {
		private final static char colorChar = 3;

		public final static String WHITE =		colorChar +"00";
		public final static String BLACK =		colorChar +"01";
		public final static String BLUE =		colorChar +"02";
		public final static String GREEN =		colorChar +"03";
		public final static String RED =		colorChar +"04";
		public final static String BROWN =		colorChar +"05";
		public final static String PURPLE =		colorChar +"06";
		public final static String ORANGE =		colorChar +"07";
		public final static String YELLOW =		colorChar +"08";
		public final static String LIME =		colorChar +"09";
		public final static String TEAL =		colorChar +"10";
		public final static String CYAN =		colorChar +"11";
		public final static String LBLUE =		colorChar +"12";
		public final static String PINK =		colorChar +"13";
		public final static String GRAY =		colorChar +"14";
		public final static String SILVER =		colorChar +"15";
		public final static String DEF =		colorChar +"99";
	}
}
