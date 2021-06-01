package fr.olympa.core.spigot.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.common.player.Gender;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.provider.AccountProvider;
import fr.olympa.api.commun.permission.list.OlympaCorePermissionsSpigot;
import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.api.spigot.customevents.PlayerSexChangeEvent;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.OlympaCore;

public class GenderCommand extends OlympaCommand {
	
	public GenderCommand(Plugin plugin) {
		super(plugin, "genre", "Change ton genre. Accorde le grade et différents messages.", OlympaCorePermissionsSpigot.GENDER_COMMAND, "sexe");
		addArgs(true, Gender.getNames());
		allowConsole = false;
		minArg = 1;
		isAsynchronous = true;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		OlympaPlayer olympaPlayer = AccountProvider.get(player.getUniqueId());
		Gender gender = Gender.get(args[0]);
		if (gender == null)
			sendUsage(label);
		Gender olympaGender = olympaPlayer.getGender();
		if (olympaGender != Gender.UNSPECIFIED) {
			sendError("Tu as déjà choisi le genre &4%s&c.", Utils.capitalize(olympaGender.getName()));
			return false;
		}
		
		if (gender == olympaGender) {
			sendError("Tu as déjà le genre &4%s&c.", Utils.capitalize(gender.getName()));
			return false;
		}
		olympaPlayer.setGender(gender);
		new AccountProvider(player.getUniqueId()).saveToRedis(olympaPlayer);
		OlympaCore.getInstance().getServer().getPluginManager().callEvent(new PlayerSexChangeEvent(player, olympaPlayer, true));
		sendError("Tu as choisi le genre &2%s&a.", Utils.capitalize(gender.getName()));
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
}
