package fr.tristiisch.olympa.core.ban.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.tristiisch.olympa.api.command.BanOlympaCommand;
import fr.tristiisch.olympa.api.objects.OlympaConsole;
import fr.tristiisch.olympa.api.permission.OlympaPermission;
import fr.tristiisch.olympa.api.plugin.OlympaPlugin;
import fr.tristiisch.olympa.api.utils.Matcher;
import fr.tristiisch.olympa.api.utils.Utils;
import fr.tristiisch.olympa.core.ban.commands.methods.UnbanIp;
import fr.tristiisch.olympa.core.ban.commands.methods.UnbanPlayer;

@SuppressWarnings("deprecation")
public class UnbanCommand extends BanOlympaCommand {

	public UnbanCommand(final Plugin plugin) {
		super(plugin, "unban", OlympaPermission.BAN_UNBAN_COMMAND, "pardon");
		this.setMinArg(2);
		this.setUsageString("<joueur|uuid|ip> <motif>");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		UUID author;
		if (sender instanceof Player) {
			author = this.player.getUniqueId();
		} else {
			author = OlympaConsole.getUniqueId();
		}
		FileConfiguration config = OlympaPlugin.getInstance().getConfig();

		if (Matcher.isFakeIP(args[0])) {
			if (Matcher.isIP(args[0])) {
				UnbanIp.unBan(author, sender, args[0], args);

			} else {
				sender.sendMessage(config.getString("ban.ipinvalid").replaceAll("%ip%", args[0]));
				return true;
			}

		} else if (Matcher.isUsername(args[0])) {
			UnbanPlayer.unBan(author, sender, null, args[0], args);

		} else if (Matcher.isFakeUUID(args[0])) {

			if (Matcher.isUUID(args[0])) {
				UnbanPlayer.unBan(author, sender, UUID.fromString(args[0]), null, args);

			} else {
				sender.sendMessage(config.getString("ban.uuidinvalid").replaceAll("%uuid%", args[0]));
				return true;
			}
		} else {
			sender.sendMessage(config.getString("ban.typeunknown").replaceAll("%type%", args[0]));
			return true;
		}

	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1) {
			// -> usless code, change to banned players
			List<String> postentielNames = Utils.startWords(args[0], Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toSet()));
			return postentielNames;
		} else if (args.length == 2) {
			Set<String> reasons = new HashSet<>(Arrays.asList("Demande de déban", "Erreur", "Tromper de Joueur", "Augmentation de peine", "Réduction de peine"));
			return Utils.startWords(args[1], reasons);
		}
		return null;
	}
}
