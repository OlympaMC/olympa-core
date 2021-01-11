package fr.olympa.core.bungee.ban;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.chat.BaseComponent;
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
		matchDuration = Pattern.compile("^(?i)(\\d+)(\\s*)(" + String.join("|", units2) + ")\\s");
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

	public static BaseComponent[] getDisconnectScreen(OlympaSanction sanction) {
		return getDisconnectScreen(Arrays.asList(sanction));
	}

	public static BaseComponent[] getDisconnectScreen(List<OlympaSanction> bans) {
		OlympaSanction sanction = bans.stream().sorted((s1, s2) -> Boolean.compare(s2.isPermanent(), s1.isPermanent())).findFirst().orElse(null);
		StringJoiner sjDisconnect = new StringJoiner("\n");
		String typeAction = sanction.getType().getNameForPlayer();
		boolean permanant = false;
		if (sanction.isPermanent()) {
			permanant = true;
			typeAction += " &npermanent&c";
		}
		sjDisconnect.add(String.format("&cTu a été %s", typeAction));
		sjDisconnect.add("");
		sjDisconnect.add(String.format("&cRaison : &4%s", ColorUtils.joinRedEt(bans.stream().map(OlympaSanction::getReason).collect(Collectors.toList()))));
		sjDisconnect.add("");
		if (permanant) {
			sjDisconnect.add(String.format("&cDurée restante : &4%s&c", Utils.timestampToDuration(sanction.getExpires())));
			sjDisconnect.add("");
		}
		sjDisconnect.add(String.format("&cID : &4%s&c", ColorUtils.joinRedEt(bans.stream().map(OlympaSanction::getId).map(String::valueOf).collect(Collectors.toList()))));
		sjDisconnect.add("");
		BaseComponent[] msgDisconnect = BungeeUtils.connectScreen(sjDisconnect.toString());
		return msgDisconnect;
	}
}
