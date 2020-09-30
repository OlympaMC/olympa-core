package fr.olympa.core.bungee.login.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.match.RegexMatcher;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.login.HandlerLogin;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class EmailCommand extends BungeeCommand {

	public static boolean isDisposableEmail(String email) {
		String domain = email.replaceFirst("(.+)@", "").toLowerCase();
		File file = new File(OlympaBungee.getInstance().getDataFolder(), "DisposableEmail.txt");
		if (!file.exists())
			throw new RuntimeException("File not found");
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null)
				if (domain.equals(line))
					return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return false;
	}

	public EmailCommand(Plugin plugin) {
		super(plugin, "email", "mail", "setemail", "changeemail");
		usageString = "<adresse@mail.fr>";
		description = "Ajouter son adresse mail";
		allowConsole = false;
		bypassAuth = false;
		HandlerLogin.command.add(command);
		for (String aliase : aliases)
			HandlerLogin.command.add(aliase);
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		olympaPlayer = getOlympaPlayer();
		if (olympaPlayer == null)
			sendImpossibleWithOlympaPlayer();
		String playerPasswordHash = olympaPlayer.getPassword();
		if (playerPasswordHash != null) {
			// TODO JSON NEEDED
			this.sendError("Tu as déjà une adresse mail. Pour la changer, rends-toi sur le site : &4&nwww.olympa.fr/profile &c.");
			return;
		}

		if (args.length == 0) {
			// TODO + annonce mais pas de spam
			this.sendMessage(Prefix.DEFAULT_GOOD, "Ajoute ton email pour augmenter la sécurité de ton compte.");
			return;
		}

		if (args.length > 2) {
			this.sendUsage();
			return;
		}

		String email = args[0];
		if (!RegexMatcher.EMAIL.is(email)) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Adresse mail incorrecte.");
			return;
		}

		if (isDisposableEmail(email)) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Désolé, les adresses mail temporaires sont interdites. Nous n'allons pas te spammer, ton adresse sert avant tout à sécuriser ton compte.");
			return;
		}

		olympaPlayer.setEmail(email);
		AccountProvider account = new AccountProvider(olympaPlayer.getUniqueId());
		account.saveToRedis(olympaPlayer);
		this.sendMessage(Prefix.DEFAULT_GOOD, "Ton adresse mail est désormais enregistrée !");
	}
}
