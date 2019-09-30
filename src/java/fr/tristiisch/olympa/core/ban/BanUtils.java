package fr.tristiisch.olympa.core.ban;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BanUtils {

	private static Pattern matchunit;
	private static Pattern matchduration = Pattern.compile("\\b[0-9]+");
	private static List<List<String>> units = new ArrayList<>();

	static {

		final Collection<String> unit = BungeeConfigUtils.getDefaultConfig().getSection("commun.units").getKeys();
		for (final String Sunit : unit) {
			units.add(BungeeConfigUtils.getStringList("commun.units." + Sunit));
		}

		final List<String> units2 = new ArrayList<>();
		for (final List<String> s2 : units) {
			units2.add(String.join("|", s2));
		}
		matchunit = Pattern.compile("(?i)(" + String.join("|", units2) + ")\\b");
	}

	public static Matcher matchDuration(final String s) {
		return matchduration.matcher(s);
	}

	public static Matcher matchUnit(final String s) {
		return matchunit.matcher(s);
	}

	public static long toTimeStamp(final int time, final String unit) {
		for (final List<String> u : units) {
			if (u.stream().filter(s -> s.equalsIgnoreCase(unit)).findFirst().isPresent()) {

				final Calendar calendar = Calendar.getInstance();
				switch (u.get(0)) {

				case "year":
					calendar.add(Calendar.YEAR, time);
					return calendar.getTimeInMillis() / 1000;
				case "month":
					calendar.add(Calendar.MONTH, time);
					return calendar.getTimeInMillis() / 1000;
				case "day":
					calendar.add(Calendar.DAY_OF_MONTH, time);
					return calendar.getTimeInMillis() / 1000;
				case "hour":
					calendar.add(Calendar.HOUR_OF_DAY, time);
					return calendar.getTimeInMillis() / 1000;
				case "minute":
					calendar.add(Calendar.MINUTE, time);
					return calendar.getTimeInMillis() / 1000;
				case "second":
					calendar.add(Calendar.SECOND, time);
					return calendar.getTimeInMillis() / 1000;
				}
			}
		}
		return 0;
	}
}
