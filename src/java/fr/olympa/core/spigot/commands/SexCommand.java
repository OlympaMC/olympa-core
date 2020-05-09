package fr.olympa.core.spigot.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.customevents.PlayerSexChangeEvent;
import fr.olympa.api.objects.Gender;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.OlympaCore;

public class SexCommand extends OlympaCommand {

	public SexCommand(Plugin plugin) {
		super(plugin, "sexe", "Change ton sexe. Accorde le grade et différents messages.");
		addArgs(true, Gender.getNames());
		allowConsole = false;
		minArg = 1;
		isAsynchronous = true;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		OlympaPlayer olympaPlayer = AccountProvider.get(player.getUniqueId());
		Gender gender = Gender.get(args[0]);
		if (gender == null) {
			sendUsage(label);
		}
		Gender olympaGender = olympaPlayer.getGender();
		if (olympaGender != Gender.NO_SPECIFED) {
			sendMessage(Prefix.DEFAULT_BAD, "Tu as déjà choisi le sexe &4" + Utils.capitalize(olympaGender.getName()) + "&c.");
			return false;
		}

		if (gender == olympaGender) {
			sendMessage(Prefix.DEFAULT_BAD, "Tu as déjà le sexe &4" + Utils.capitalize(gender.getName()) + "&c.");
			return false;
		}
		olympaPlayer.setGender(gender);
		new AccountProvider(player.getUniqueId()).saveToRedis(olympaPlayer);
		OlympaCore.getInstance().getServer().getPluginManager().callEvent(new PlayerSexChangeEvent(player, olympaPlayer, true));
		sendMessage(Prefix.DEFAULT_GOOD, "Tu as choisi le 	 &2" + Utils.capitalize(gender.getName()) + "&a.");
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
}
