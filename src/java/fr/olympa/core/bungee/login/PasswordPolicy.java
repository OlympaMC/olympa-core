package fr.olympa.core.bungee.login;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PasswordPolicy {

	private static final String prefixMsg = "Le mot de passe";
	private static final Set<String> disallowPassword = new HashSet<>(Arrays.asList("azert", "qwert", "0123", "1234"));
	public static final PasswordPolicy MIN_CHAR = new PasswordPolicy((m, playerName) -> m.length() < 5, m -> "doit avoir 5 caratères minimum");
	public static final PasswordPolicy MAX_CHAR = new PasswordPolicy((m, playerName) -> m.length() > 100, m -> "doit avoir 100 caratères maximum");
	public static final PasswordPolicy BAD_COMBINE = new PasswordPolicy((m, playerName) -> m.contains(playerName) || m.length() < 20
			&& (m.charAt(m.length() - 1) == '!' || disallowPassword.contains(m)), m -> "ne doit pas simple a deviner");

	public static final PasswordPolicy SPECIAL_CHAR = new PasswordPolicy(m -> "contenir un caratère spécial",
			(m, playerName) -> m.chars().filter(c -> !Character.isLetter(c) && c != ' ').count());
	public static final PasswordPolicy NUMBER_CHAR = new PasswordPolicy(m -> "contenir un chiffre",
			(m, playerName) -> m.chars().filter(c -> Character.isDigit(c)).count());
	public static final PasswordPolicy MAJ_CHAR = new PasswordPolicy(m -> "contenir une majuscule",
			(m, playerName) -> m.chars().filter(c -> Character.isLetter(c) && Character.isUpperCase(c)).count());

	private static final List<PasswordPolicy> minimal = Arrays.asList(MIN_CHAR, MAX_CHAR, BAD_COMBINE);
	private static final List<PasswordPolicy> optional = Arrays.asList(MAJ_CHAR, SPECIAL_CHAR, NUMBER_CHAR);

	BiPredicate<String, String> isGood;
	BiFunction<String, String, Long> isGoodAmount;
	Function<String, String> msg;

	public PasswordPolicy(BiPredicate<String, String> isGood, UnaryOperator<String> msg) {
		this.isGood = isGood;
		this.msg = msg;
	}

	public PasswordPolicy(Function<String, String> msg, BiFunction<String, String, Long> isGoodAmount) {
		this.isGoodAmount = isGoodAmount;
		this.msg = msg;
	}

	public boolean check(String password, String playerName) {
		return isGood.test(password, playerName);
	}

	public String getMsg(String password) {
		return msg.apply(password);
	}

	public static double getPossibility(String password) {
		int charPossibility = 0;
		if (password.chars().anyMatch(c -> Character.isLetter(c) && Character.isLowerCase(c)))
			charPossibility += 26;
		if (password.chars().anyMatch(c -> Character.isLetter(c) && Character.isUpperCase(c)))
			charPossibility += 26;
		if (password.chars().anyMatch(c -> Character.isDigit(c)))
			charPossibility += 10;
		String specialChars = "/*!@#$%^&*()\"{}_[]|\\?/<>,.";
		if (password.chars().anyMatch(c -> specialChars.chars().anyMatch(sc -> sc == c)))
			charPossibility += specialChars.length(); // 27
		return Math.pow(charPossibility, password.length());
	}

	public static boolean isPasswordValid(ProxiedPlayer player, String password, boolean force) {
		List<String> msg = new ArrayList<>();
		minimal.forEach(passwordPolicy -> {
			if (passwordPolicy.check(password, player.getName()))
				msg.add(passwordPolicy.getMsg(password));
		});

		if (msg.isEmpty()) {
			int goodPassword = 0;
			for (PasswordPolicy passwordPolicy : optional)
				if (passwordPolicy.check(password, player.getName()))
					goodPassword++;
			if (goodPassword == 3 && password.length() > 30)
				player.sendMessage(Prefix.DEFAULT_GOOD.formatMessageB("&7Tu utilise un mot de passe &2très sécurisé&7."));
			if (goodPassword == 3 || password.length() > 20)
				player.sendMessage(Prefix.DEFAULT_GOOD.formatMessageB("&7Tu utilise un mot de passe &2sécurisé&7."));
			else if (goodPassword == 2 || password.length() > 13)
				player.sendMessage(Prefix.DEFAULT_GOOD.formatMessageB("&7Tu utilise un mot de passe &2assez sécurisé&7."));
			else if (password.length() > 13)
				player.sendMessage(Prefix.DEFAULT_GOOD.formatMessageB("&7Tu utilise un mot de passe &2moyennement sécurisé&7."));
			if (password.length() > 13)
				player.sendMessage(Prefix.DEFAULT_GOOD.formatMessageB("&7Tu utilise un mot de passe &2moyennement sécurisé&7."));
			return true;
		}
		player.sendMessage(Prefix.DEFAULT_BAD.formatMessageB("&c%s %s.", prefixMsg, String.join(", ", msg)));
		return false;
	}
}
