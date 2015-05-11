import bot.nat.sumi.Message;
import bot.nat.sumi.Module;
import bot.nat.sumi.Server;

public class math extends Module {
	@Override
	public void command(Message m, Server srv) {
		if(m.text.equalsIgnoreCase("$math") || m.text.equalsIgnoreCase("$math ")){
			srv.send(m.channel, m.author, "0", m.channel);
			return;
		}

		/* start math */
		long nano = System.nanoTime();
		String math = m.text.replace("$math ", "").replace(" ", "");

		/* loop until there is only numbers in the string */
		while(!math.equals("") && !math.replace(".", "").matches("[0-9]+")){
			if(math.contains("(")){
				/* get starting and ending positions of math statement */
				int startPos = findLastBracket(math) + 1, endPos = findClosingBracket(math, startPos);
				/* get current String being processed */
				String cur = math.substring(startPos, endPos);

				/* do math statement */
				String ret = math.replace("("+ cur +")", doMath(cur, srv, m));

				if(ret.equals("%&exit")){
					return;
				}

				math = ret;

			} else {
				/* else do the rest of the math */
				String ret = math.replace(math, doMath(math, srv, m));

				if(ret.equals("%&exit")){
					return;
				}

				math = ret;
			}
		}

		srv.send(m.channel, m.author, math.equals("") ? "NaN" : math, m.channel);
	}

	/* here is where it processes the math equates */
	private String doMath(String math, Server srv, Message m) {
		while(!math.equals("") && !math.replace(".", "").matches("[0-9]+")){
			int pow = math.indexOf('$');
			int mul = math.indexOf('*'), div = math.indexOf('/'), per = math.indexOf('%');
			int plus = math.indexOf('+'), minus = math.indexOf('-');
			int rotL = math.indexOf('<'), rotR = math.indexOf('>');
			int or = math.indexOf('|'), and = math.indexOf('&'), xor = math.indexOf('^');

			if(pow != -1){
				if(pow != -1){
					math = nextPow(math, pow);
				}

			} else if(mul != -1 || div != -1 || per != -1){
				if(div != -1){
					math = nextDiv(math, div);

					if(math.equalsIgnoreCase("Infinity")){
						srv.send(m.channel, m.author, "\u221E (Infinity)", m.channel);
						return "%&exit";
					}

				} else if(mul != -1){
					math = nextMul(math, mul);

				} else {
					math = nextPer(math, per);
				}

			/* then check addition and subtraction */
			} else if(plus != -1 || minus != -1){
				if(minus != -1){
					math = nextMinus(math, minus);

				} else {
					math = nextPlus(math, plus);
				}

			/* then check shifting */
			} else if(rotL != -1 || rotR != -1){
				if(rotL != -1){
					math = nextRotL(math, rotL);

				} else {
					math = nextRotR(math, rotR);
				}

			} else if(or != -1 || xor != -1 || and != -1){
					if(or != -1){
						math = nextOr(math, or);

					} else if(xor != -1){
						math = nextXor(math, xor);

					} else {
						math = nextAnd(math, and);
					}

			} else {
				return "";
			}
		}

		return math;
	}

	private String nextPow(String math, int pos) {
		double[] val = getNextValues(math, pos);
		return calcReplace(math, val[2], val[3], Math.pow(val[0], val[1]));
	}

	private String nextAnd(String math, int pos) {
		double[] val = getNextValues(math, pos);
		return calcReplace(math, val[2], val[3], (long)val[0] & (long)val[1]);
	}

	private String nextXor(String math, int pos) {
		double[] val = getNextValues(math, pos);
		return calcReplace(math, val[2], val[3], (long)val[0] ^ (long)val[1]);
	}

	private String nextOr(String math, int pos) {
		double[] val = getNextValues(math, pos);
		return calcReplace(math, val[2], val[3], (long)val[0] | (long)val[1]);
	}

	private String nextRotL(String math, int pos) {
		double[] val = getNextValues(math, pos);
		return calcReplace(math, val[2], val[3], (long)val[0] << (long)val[1]);
	}

	private String nextRotR(String math, int pos) {
		double[] val = getNextValues(math, pos);
		return calcReplace(math, val[2], val[3], (long)val[0] >> (long)val[1]);
	}

	private String nextPlus(String math, int pos) {
		double[] val = getNextValues(math, pos);
		return calcReplace(math, val[2], val[3], val[0] + val[1]);
	}

	private String nextMinus(String math, int pos) {
		double[] val = getNextValues(math, pos);
		return calcReplace(math, val[2], val[3], val[0] - val[1]);
	}

	private String nextDiv(String math, int pos) {
		double[] val = getNextValues(math, pos);

		if(val[1] == 0D){
			return "Infinity";
		}

		return calcReplace(math, val[2], val[3], val[0] / val[1]);
	}

	private String nextMul(String math, int pos) {
		double[] val = getNextValues(math, pos);
		return calcReplace(math, val[2], val[3], val[0] * val[1]);
	}

	private String nextPer(String math, int pos) {
		double[] val = getNextValues(math, pos);
		return calcReplace(math, val[2], val[3], val[0] % val[1]);
	}

	/* gets next values to calculate */
	private double[] getNextValues(String math, int pos) {
		double val1 = Double.parseDouble(math.substring(getBackward(math, pos - 1), pos));
		double val2 = Double.parseDouble(math.substring(pos + 1, getForward(math, pos + 1)));

		return new double[]{ val1, val2, getBackward(math, pos - 1), getForward(math, pos + 1) };
	}

	/* replace calculation with result */
	private String calcReplace(String math, double start, double end, double rep) {
		String r = math.substring((int)start, (int)end);
		return math.replace(r, rep +"");
	}

	/* gets next instance of character */
	private int getForward(String math,int pos) {
		while (Character.isDigit(math.charAt(pos)) || math.charAt(pos) == '.' || math.charAt(pos) == '-') {
			pos ++;
			if(pos >= math.length()){
				return math.length();
			}
		}

		return pos;
	}

	/* gets last instance of character before _pos_ */
	private int getBackward(String math, int pos) {
		while (Character.isDigit(math.charAt(pos)) || math.charAt(pos) == '.' || math.charAt(pos) == '-') {
			pos --;
			if(pos <= 0){
				return 0;
			}
		}

		return pos;
	}

	/* get closing bracket of statement in math (after last opening bracket recorded) */
	private int findClosingBracket(String math, int startPos) {
		return math.indexOf(')', startPos);
	}

	/* get last opening bracket in string */
	private int findLastBracket(String math) {
		return math.lastIndexOf('(');
	}

	@Override
	public String[] reserved() {
		return new String[]{ "$math" };
	}
}
