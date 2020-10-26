package fr.olympa.core.bungee.ban;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.olympa.api.match.RegexMatcher;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.ban.objects.BanExecute;
import net.md_5.bungee.config.Configuration;

public class SanctionUtils {

	private static Pattern matchDuration;
	public static List<List<String>> units = new ArrayList<>();

	static {
		Configuration config = OlympaBungee.getInstance().getConfig();
		Configuration unit = config.getSection("ban.units");
		for (String Sunit : unit.getKeys())
			units.add(config.getStringList("ban.units." + Sunit));
		List<String> units2 = new ArrayList<>();
		for (List<String> s2 : units)
			units2.add(String.join("|", s2));
		matchDuration = Pattern.compile("(?i)^(\\d+)\\s*(" + String.join("|", units2) + ")\\b");
	}

	public static Matcher matchDuration(String s) {
		return matchDuration.matcher(s);
	}

	public static long toTimeStamp(int time, String unit) {
		for (List<String> u : units)
			if (u.stream().filter(s -> s.equalsIgnoreCase(unit)).findFirst().isPresent()) {
				Calendar calendar = Calendar.getInstance();
				switch (u.get(0)) {
				case "year":
					calendar.add(Calendar.YEAR, time);
					break;
				case "month":
					calendar.add(Calendar.MONTH, time);
					break;
				case "day":
					calendar.add(Calendar.DAY_OF_MONTH, time);
					break;
				case "hour":
					calendar.add(Calendar.HOUR_OF_DAY, time);
					break;
				case "minute":
					calendar.add(Calendar.MINUTE, time);
					break;
				case "second":
					calendar.add(Calendar.SECOND, time);
					break;
				}
				return calendar.getTimeInMillis() / 1000;
			}
		return 0;
	}

	public static String formatReason(String reason) {
		return Utils.capitalize(reason.replaceAll(" {2,}", " "));
	}

	public static BanExecute formatArgs(String[] args) {
		BanExecute banExecute = new BanExecute();
		List<Object> targets = new ArrayList<>();
		Arrays.asList(args[0].split(",")).forEach(t -> {
			if (RegexMatcher.IP.is(t))
				try {
					targets.add(InetAddress.getByName(t));
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			else if (RegexMatcher.UUID.is(t))
				targets.add(UUID.fromString(t));
			else if (RegexMatcher.USERNAME.is(t))
				targets.add(t);
		});
		banExecute.setTargets(targets);
		long expire = 0;
		String allArgs = String.join(" ", args);
		String newAllArgs = null;
		Matcher matcherDuration = SanctionUtils.matchDuration(allArgs);
		if (matcherDuration.find()) {
			String time = matcherDuration.group(1);
			String unit = matcherDuration.group(2);
			newAllArgs = allArgs.replace(time + unit, "");
			if (newAllArgs.length() == allArgs.length()) {
				newAllArgs = newAllArgs.replace(time, "");
				newAllArgs = newAllArgs.replace(unit, "");
			}
			expire = SanctionUtils.toTimeStamp(Integer.parseInt(time), unit);
			banExecute.setExpire(expire);
		} else
			newAllArgs = allArgs;
		banExecute.setReason(newAllArgs);
		return banExecute;
	}
}
