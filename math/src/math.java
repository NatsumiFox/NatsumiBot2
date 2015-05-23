import bot.nat.sumi.Main;
import bot.nat.sumi.Message;
import bot.nat.sumi.Module;
import bot.nat.sumi.Server;

import java.math.BigDecimal;
import java.math.BigInteger;

public class math extends Module {
	@Override
	public void command(Message m, Server srv) {
		if(m.text.equalsIgnoreCase("$math") || m.text.equalsIgnoreCase("$mathh") || m.text.equalsIgnoreCase("$matht") || m.text.equalsIgnoreCase("$mathht")){
			srv.send(m.channel, m.author, "NaN", m.channel);
			return;
		}

		/* start math */
		long mill = System.currentTimeMillis();
		String math = m.text.replace(Main.cmd +"mathh ", "").replace(Main.cmd + "math ", "").replace(Main.cmd + "matht ", "").replace(Main.cmd + "mathht ", "").replace(" ", "");

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

				/* this accounts for negative numbers */
				if(math.startsWith("-") && (math.indexOf('-') == math.lastIndexOf('-'))){
					break;
				}
			}
		}

		if(math.length() > 256){
			srv.send(m.channel, m.author, "too long to display or ∞ (Infinity)", m.channel);

			if(m.text.split(" ")[0].endsWith("t")){
				srv.send(m.channel, m.author, "Completed in "+ (System.currentTimeMillis() - mill) +"ms!", m.channel);
			}
			return;
		}

		/* do final string */
		if(math.equals("")){
			srv.send(m.channel, m.author, "NaN", m.channel);

			if(m.text.split(" ")[0].endsWith("t")){
				srv.send(m.channel, m.author, "Completed in "+ (System.currentTimeMillis() - mill) +"ms!", m.channel);
			}
			return;
		}

		if(m.text.startsWith(Main.cmd + "mathh ")){
			String prefix = "0x";

			if(math.contains("-")){
				math = math.replace("-", "");
				prefix = "-0x";
			}

			if(math.contains(".")){
				math = math.split("\\.")[0];
			}

			srv.send(m.channel, m.author, prefix + new BigInteger(math, 10).toString(16).toUpperCase(), m.channel);

		} else {
			srv.send(m.channel, m.author, math, m.channel);
		}

		if(m.text.split(" ")[0].endsWith("t")){
			srv.send(m.channel, m.author, "Completed in "+ (System.currentTimeMillis() - mill) +"ms!", m.channel);
		}
	}

	/* here is where it processes the math equates */
	private String doMath(String math, Server srv, Message m) {
		while(!math.equals("") && !math.replace(".", "").matches("[0-9]+")){
			math = math.replace("--", "");
			int pow = math.indexOf('$'), fac = math.indexOf("!");
			int mul = math.indexOf('*'), div = math.indexOf('/'), per = math.indexOf('%');
			int plus = math.indexOf('+'), minus = math.startsWith("-") ? math.indexOf('-', 1) : math.indexOf('-');
			int rotL = math.indexOf('<'), rotR = math.indexOf('>');
			int or = math.indexOf('|'), and = math.indexOf('&'), xor = math.indexOf('^');

			if(pow != -1 || fac != -1){
				if(pow != -1){
					math = nextPow(math, pow);

				} else if(fac != -1){
					math = nextFactorial(math, fac);
				}

			} else if(mul != -1 || div != -1 || per != -1){
				if(div != -1){
					math = nextDiv(math, div);

					if(math.contains("inf")){
						srv.send(m.channel, m.author, (math.startsWith("-") ? "-" : "") +"\u221E ("+
								(math.startsWith("-") ? "Negative" : "Positive") +" Infinity)", m.channel);
						return "%&exit";
					}

				} else if(mul != -1){
					math = nextMul(math, mul);

				} else {
					math = nextPer(math, per);
				}

			/* then check shifting */
			} else if(rotL != -1 || rotR != -1){
				if(rotL != -1){
					math = nextRotL(math, rotL);

				} else {
					math = nextRotR(math, rotR);
				}

			} else if(or != -1 || xor != -1 || and != -1) {
				if (or != -1) {
					math = nextOr(math, or);

				} else if (xor != -1) {
					math = nextXor(math, xor);

				} else {
					math = nextAnd(math, and);
				}

			/* then check addition and subtraction */
			} else if(plus != -1 || minus != -1){
				if(plus != -1){
					math = nextPlus(math, plus);

				} else {
					math = nextMinus(math, minus);
				}

			} else if(math.startsWith("-")){
				return math;

			} else if(math.startsWith("0x")){
				return hexToDec(math).toString();

			} else {
				return "";
			}
		}

		return math;
	}

	private String nextPow(String math, int pos) {
		BigDecimal[] val = getNextValues(math, pos);
		return calcReplace(math, val[2].doubleValue(), val[3].doubleValue(), val[0].pow(val[1].intValue()));
	}

	private String nextAnd(String math, int pos) {
		BigDecimal[] val = getNextValues(math, pos);
		return calcReplace(math, val[2].doubleValue(), val[3].doubleValue(), new BigDecimal(val[0].longValue() & val[1].longValue()));
	}

	private String nextXor(String math, int pos) {
		BigDecimal[] val = getNextValues(math, pos);
		return calcReplace(math, val[2].doubleValue(), val[3].doubleValue(), new BigDecimal(val[0].longValue() ^ val[1].longValue()));
	}

	private String nextOr(String math, int pos) {
		BigDecimal[] val = getNextValues(math, pos);
		return calcReplace(math, val[2].doubleValue(), val[3].doubleValue(), new BigDecimal(val[0].longValue() | val[1].longValue()));
	}

	private String nextRotL(String math, int pos) {
		BigDecimal[] val = getNextValues(math, pos);
		return calcReplace(math, val[2].doubleValue(), val[3].doubleValue(), new BigDecimal(val[0].longValue() << val[1].longValue()));
	}

	private String nextRotR(String math, int pos) {
		BigDecimal[] val = getNextValues(math, pos);
		return calcReplace(math, val[2].doubleValue(), val[3].doubleValue(), new BigDecimal(val[0].longValue() >> val[1].longValue()));
	}

	private String nextPlus(String math, int pos) {
		BigDecimal[] val = getNextValues(math, pos);
		return calcReplace(math, val[2].doubleValue(), val[3].doubleValue(), val[0].add(val[1]));
	}

	private String nextMinus(String math, int pos) {
		BigDecimal[] val = getNextValues(math, pos);
		return calcReplace(math, val[2].doubleValue(), val[3].doubleValue(), val[0].subtract(val[1]));
	}

	private String nextDiv(String math, int pos) {
		BigDecimal[] val = getNextValues(math, pos);

		if(val[1].doubleValue() == 0D){
			return (val[0].doubleValue() >= 0 ? "" : "-") +"inf";
		}

		return calcReplace(math, val[2].doubleValue(), val[3].doubleValue(), val[0].divide(val[1], BigDecimal.ROUND_HALF_UP));
	}

	private String nextMul(String math, int pos) {
		BigDecimal[] val = getNextValues(math, pos);
		return calcReplace(math, val[2].doubleValue(), val[3].doubleValue(), val[0].multiply(val[1]));
	}

	private String nextPer(String math, int pos) {
		BigDecimal[] val = getNextValues(math, pos);
		return calcReplace(math, val[2].doubleValue(), val[3].doubleValue(), new BigDecimal(val[0].doubleValue() % val[1].doubleValue()));
	}

	private String nextFactorial(String math, int pos) {
		int val1 = getBackward(math, pos - 1);
		return calcReplace(math, val1, pos + 1, factorial(hexToDec(math.substring(val1, pos))));
	}

	/* calculate factorial */
	private BigDecimal factorial(BigDecimal l) {
		if(l.longValue() >= 1024){
			return new BigDecimal(-0);

		} else if(l.longValue() <= 0){
			return new BigDecimal(0);
		}

		BigDecimal ret = new BigDecimal(1);
		for(int i = 1;i < l.longValue() + 1;i ++){
			ret = ret.multiply(new BigDecimal(i));
		}

		return ret;
	}

	/* gets next values to calculate */
	private BigDecimal[] getNextValues(String math, int pos) {
		int val1 = getBackward(math, pos - 1), val2 = getForward(math, pos + 1);
		return new BigDecimal[]{ hexToDec(math.substring(val1, pos)),
				hexToDec(math.substring(pos + 1, val2)), new BigDecimal(val1), new BigDecimal(val2), };
	}

	private BigDecimal hexToDec(String s) {
		if((s.startsWith("0x") || s.startsWith("-0x")) && !s.contains(".")){
			return new BigDecimal(new BigInteger(s.toLowerCase().replace("0x", ""), 16));

		} else if(!s.toLowerCase().contains("a") && !s.toLowerCase().contains("b") && !s.toLowerCase().contains("c") &&
				!s.toLowerCase().contains("d") && !s.toLowerCase().contains("e") && !s.toLowerCase().contains("f")){
			return new BigDecimal(s.contains(".") ? s : s +".0");

		} else {
			throw new NumberFormatException("Non-Hexadecimal string '"+ s +"' contains hexadecimal characters!");
		}
	}

	/* replace calculation with result */
	private String calcReplace(String math, double start, double end, BigDecimal rep) {
		String r = math.substring((int)start, (int)end);
		return math.replace(r, rep.toString() +"");
	}

	/* gets next instance of character */
	private int getForward(String math, int pos) {
		while (Character.isDigit(math.charAt(pos)) || math.charAt(pos) == '.' || math.charAt(pos) == '-' || math.charAt(pos) == 'x' ||
				math.charAt(pos) == 'a' || math.charAt(pos) == 'b' || math.charAt(pos) == 'c' || math.charAt(pos) == 'd' || math.charAt(pos) == 'e' || math.charAt(pos) == 'f' ||
				math.charAt(pos) == 'A' || math.charAt(pos) == 'B' || math.charAt(pos) == 'C' || math.charAt(pos) == 'D' || math.charAt(pos) == 'E' || math.charAt(pos) == 'F') {
			pos ++;
			if(pos >= math.length()){
				return math.length();
			}
		}

		return pos;
	}

	/* gets last instance of character before _pos_ */
	private int getBackward(String math, int pos) {
		while (Character.isDigit(math.charAt(pos)) || math.charAt(pos) == '.' || math.charAt(pos) == '-' || math.charAt(pos) == 'x' ||
				math.charAt(pos) == 'a' || math.charAt(pos) == 'b' || math.charAt(pos) == 'c' || math.charAt(pos) == 'd' || math.charAt(pos) == 'e' || math.charAt(pos) == 'f' ||
				math.charAt(pos) == 'A' || math.charAt(pos) == 'B' || math.charAt(pos) == 'C' || math.charAt(pos) == 'D' || math.charAt(pos) == 'E' || math.charAt(pos) == 'F') {
			pos --;
			if(pos <= 0){
				return 0;
			}
		}

		return pos + 1;
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
		return new String[]{ Main.cmd +"math", Main.cmd +"mathh", Main.cmd +"matht", Main.cmd +"mathht" };
	}
}
