package fr.olympa.core.common;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SwearWord {

	private final static char SWEAR_CHAR = '*';

	public static String getSwearChar(int size) {
		StringBuilder sb = new StringBuilder();
		while (size > 0) {
			sb.append(SWEAR_CHAR);
			size--;
		}
		return sb.toString();
	}

	public static String stringToRegex(String s) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		char[] array = s.toLowerCase().toCharArray();
		while (i < array.length) {
			String out = null;
			if (array[i] != ' ') {
				switch (array[i]) {
				case 'o':
					out = "(0|au|eau|" + s + ")";
					break;
				case 'f':
					out = "(ph|" + s + ")";
					break;
				case 'k':
				case 'q':
					out = "(qu|q|k)";
					break;
				case 'g':
					if (array.length > i + 1 && array[i + 1] == 'u')
						i++;
					out = "(g|gu)";
					break;
				default:
					break;
				}
				if (out != null)
					sb.append(out);
				else
					sb.append(array[i]);
				sb.append("+[^a-zA-Z]*");
			} else
				sb.append("([^a-zA-Z]| )*");
		}
		return sb.toString();
	}

	String name;
	String replaceWord;
	String regex;
	Pattern pattern;
	boolean defaultMode = false;
	List<String> blackListPrefix;
	List<Pattern> whiteListPattern = new ArrayList<>();

	/**
	 *
	 * @param swear start or/and end with | to match word with start/end of word is defined
	 */
	public void add(String swear, boolean defaultMode, List<String> blackListPrefix, List<String> whitelistListPrefix) {
		this.defaultMode = defaultMode;
		this.blackListPrefix = blackListPrefix;
		String start = "";
		String end = "";
		if (swear.startsWith("|")) {
			start = "\\b";
			swear = swear.substring(1);
		}
		if (swear.endsWith("|")) {
			end = "\\b";
			swear = swear.substring(0, swear.length() - 1);
		}
		String nameInRegex = stringToRegex(swear);
		StringJoiner blRegex = new StringJoiner("|", "(", ")" + (defaultMode ? "?" : ""));
		blackListPrefix.forEach(bl -> blRegex.add(stringToRegex(bl)));
		regex = "(?iu)" + start + blackListPrefix.toString() + nameInRegex + end;
		pattern = Pattern.compile(regex);
		whitelistListPrefix.forEach(bl -> whiteListPattern.add(Pattern.compile(stringToRegex(bl))));
	}

	public String match(String msg) {
		Matcher match = pattern.matcher(msg);
		String newMsg = null;
		while (match.find()) {
			String group = match.group();
			newMsg = msg.replace(group, getSwearChar(group.length()));
		}
		return newMsg;
	}
}
