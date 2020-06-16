package fr.olympa.core.bungee.login.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Matcher;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.login.HandlerLogin;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class EmailCommand extends BungeeCommand {
	
	public static boolean isDisposableEmail(String email) {
		String domain = email.replaceFirst("(.+)@", "").toLowerCase();
		File file = new File(OlympaBungee.getInstance().getDataFolder(), "DisposableEmail.txt");
		if (!file.exists()) {
			throw new RuntimeException("File not found");
		}
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				if (domain.equals(line)) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	
	public EmailCommand(Plugin plugin) {
		super(plugin, "email", "mail", "setemail", "changeemail");
		this.usageString = "<adresse@mail.fr>";
		this.description = "Ajouter son adresse mail";
		this.allowConsole = false;
		this.bypassAuth = false;
		HandlerLogin.command.add(this.command);
		for (String aliase : this.aliases) {
			HandlerLogin.command.add(aliase);
		}
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		this.olympaPlayer = this.getOlympaPlayer();
		if (this.olympaPlayer == null) {
			this.sendImpossibleWithOlympaPlayer();
		}
		String playerPasswordHash = this.olympaPlayer.getPassword();
		if (playerPasswordHash != null) {
			// TODO JSON NEEDED
			this.sendError("Tu as déjà une adresse mail. Pour la changer, rends-toi sur le site : www.olympa.fr/profile.");
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
		if (!Matcher.isEmail(email)) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Adresse mail incorrecte.");
			return;
		}

		if (isDisposableEmail(email)) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Désolé, les adresses mail temporaires sont interdites. Nous n'allons pas te spammer, ton adresse sert avant tout à sécuriser ton compte.");
			return;
		}

		this.olympaPlayer.setEmail(email);
		AccountProvider account = new AccountProvider(this.olympaPlayer.getUniqueId());
		account.saveToRedis(this.olympaPlayer);
		this.sendMessage(Prefix.DEFAULT_GOOD, "Ton adresse mail est désormais enregistrée !");
	}
}
