package fr.olympa.core.spigot.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SwearHandler {

	private List<Pattern> regexSwear;

	public SwearHandler(List<String> swearList) {
		regexSwear = new ArrayList<>();
		for (String swear : swearList) {
			String swears = "";
			String b = "\\b";
			if (swear.startsWith("|")) {
				b = "";
				swear = swear.substring(1);
			}
			for (char s : swear.toCharArray()) {
				swears += s + "+(\\W|\\d|_)*";
			}
			regexSwear.add(Pattern.compile("(?iu)" + b + "(" + swears + ")" + b));
		}
		System.out.println("swearList " + String.join(", ", regexSwear.stream().map(r -> r.pattern()).collect(Collectors.toSet())));
	}

	public List<Pattern> getRegexSwear() {
		return regexSwear;
	}
}
