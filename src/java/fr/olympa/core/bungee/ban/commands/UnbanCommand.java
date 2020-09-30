package fr.olympa.core.bungee.ban.commands;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.player.OlympaConsole;
import fr.olympa.api.utils.Matcher;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.ban.commands.methods.UnbanIp;
import fr.olympa.core.bungee.ban.commands.methods.UnbanPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

@SuppressWarnings("deprecation")
public class UnbanCommand extends BungeeCommand {

	public UnbanCommand(Plugin plugin) {
		super(plugin, "unban", OlympaCorePermissions.BAN_UNBAN_COMMAND, "pardon");
		minArg = 2;
		usageString = "<joueur|uuid|ip> <motif>";
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		UUID author;
		if (sender instanceof Player)
			author = ((Player) sender).getUniqueId();
		else
			author = OlympaConsole.getUniqueId();

		Configuration config = OlympaBungee.getInstance().getConfig();
		if (Matcher.isFakeIP(args[0])) {
			if (Matcher.isIP(args[0]))
				UnbanIp.unBan(author, sender, args[0], args);
			else {
				sender.sendMessage(config.getString("default.ipinvalid").replace("%ip%", args[0]));
				return;
			}

		} else if (Matcher.isUsername(args[0]))
			UnbanPlayer.unBan(author, sender, null, args[0], args);
		else if (Matcher.isFakeUUID(args[0])) {

			if (Matcher.isUUID(args[0]))
				UnbanPlayer.unBan(author, sender, UUID.fromString(args[0]), null, args);
			else {
				sender.sendMessage(config.getString("default.uuidinvalid").replace("%uuid%", args[0]));
				return;
			}
		} else {
			sender.sendMessage(config.getString("default.typeunknown").replace("%type%", args[0]));
			return;
		}
		return;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, BungeeCommand command, String[] args) {
		if (args.length == 1) {
			// -> usless code, change to banned players
			List<String> postentielNames = Utils.startWords(args[0], OlympaBungee.getInstance().getProxy().getPlayers().stream().map(ProxiedPlayer::getName).collect(Collectors.toSet()));
			return postentielNames;
		} else if (args.length == 2) {
			List<String> reasons = Arrays.asList("Demande de déban", "Erreur", "Tromper de Joueur", "Augmentation de peine", "Réduction de peine");
			return Utils.startWords(args[1], reasons);
		}
		return null;
	}
}
