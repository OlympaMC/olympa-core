package fr.olympa.core.spigot.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SwearHandler {

	private List<Pattern> regexSwear;

	public SwearHandler(List<String> swears) {
		regexSwear = new ArrayList<>();
		for (String swear : swears) {
			StringBuilder sb = new StringBuilder();
			String start = new String();
			String end = new String();
			if (swear.startsWith("|")) {
				start = "\\b";
				swear = swear.substring(1);
			}
			if (swear.endsWith("|")) {
				end = "\\b";
				swear = swear.substring(0, swear.length() - 2);
			}
			for (char s : swear.toCharArray()) {
				String out;
				switch (s) {
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
				default:
					out = String.valueOf(s);
					break;
				}
				sb.append(out + "+[^a-zA-Z]*");
			}
			regexSwear.add(Pattern.compile("(?iu)" + start + "(" + sb.toString() + end + ")"));
		}
	}

	public List<Pattern> getRegexSwear() {
		return regexSwear;
	}

	public void test(String msg) {
		for (Pattern pattern : regexSwear) {
			Matcher matcher = pattern.matcher(msg);
			if (matcher.find()) {
				System.out.println("[M] " + matcher.group() + " 1: " + matcher.group(1) + " '" + msg + "'");
			}
		}
	}
}
