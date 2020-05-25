package fr.olympa.core.spigot.status;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.maintenance.MaintenanceStatus;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.core.spigot.OlympaCore;

public class SetStatusCommand extends OlympaCommand {

	public SetStatusCommand(Plugin plugin) {
		super(plugin, "setstatus", OlympaCorePermissions.SETSTATUS_COMMAND, "setstate");
		usageString = "<" + String.join(", ", MaintenanceStatus.getNames()) + ">";
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		MaintenanceStatus status = OlympaCore.getInstance().getStatus();
		if (args.length == 0) {
			this.sendMessage(Prefix.DEFAULT, "Le serveur est actuellement en mode " + status.getNameColored() + "&7.");
			return true;
		}
		MaintenanceStatus status2 = MaintenanceStatus.get(args[0]);
		if (status2 == null) {
			this.sendUsage(label);
			return true;
		}
		if (status == status2) {
			sendError("Le serveur est déjà en mode " + status.getNameColored() + "&c.");
			return true;
		}
		OlympaPermission needPermission = status2.getPermission();
		if (needPermission != null && !needPermission.hasSenderPermission(sender)) {
			sendError("Tu n'a pas la permission d'être connecter si tu met le mode " + status.getNameColored() + "&c.");
			return true;
		}
		OlympaCore.getInstance().setStatus(status2);
		if (needPermission != null) {
			Consumer<? super Set<Player>> succes = players -> {
				this.sendMessage(players, Prefix.ERROR, "Le serveur est désormais en mode %s&c.", status.getNameColored());
			};
			Consumer<? super Collection<? extends Player>> empty = players -> {
				players.forEach(player -> player.kickPlayer(SpigotUtils.connectScreen("&eDésolé le serveur est désormais en mode " + status.getNameColored() + "&e.\nEt tu n'y a plus accès. (pas d'inquiétudes, c'est temporaire !)")));
				sendSuccess("Tu as kick " + players.size() + " joueur, qui n'ont pas la permission &6&n" + status.getPermission().toString() + "&a.");
			};
			needPermission.getPlayers(succes, empty);
		}
		sendSuccess("Le serveur est désormais en mode " + status2.getNameColored() + "&a, il était avant en mode " + status.getNameColored() + "&a.");
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1) {
			List<String> postentielNames = Utils.startWords(args[0], MaintenanceStatus.getNames());
			return postentielNames;
		} else if (args.length == 2) {
			List<String> postentielGroups = Utils.startWords(args[1], Arrays.stream(OlympaGroup.values()).map(OlympaGroup::getName).collect(Collectors.toList()));
			return postentielGroups;
		}
		return null;
	}

}
