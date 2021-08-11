package fr.olympa.core.spigot.status;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.api.common.server.ServerStatus;
import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.api.spigot.config.CustomConfig;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsSpigot;
import fr.olympa.core.spigot.OlympaCore;

public class SetStatusCommand extends OlympaCommand {

	public SetStatusCommand(Plugin plugin) {
		super(plugin, "setstatus", "Permet de modifier le statut d'un serveur spigot.", OlympaCorePermissionsSpigot.SETSTATUS_COMMAND, "setstatut");
		this.addArgs(false, ServerStatus.getNames());
		this.addArgs(false, "dontKick");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		OlympaCore coreInstance = OlympaCore.getInstance();
		ServerStatus oldStatus = coreInstance.getStatus();
		if (args.length == 0) {
			this.sendMessage(Prefix.DEFAULT, "Le serveur est actuellement en mode " + oldStatus.getNameColored() + "&7.");
			return true;
		}

		ServerStatus newStatus = ServerStatus.get(args[0]);
		if (newStatus == null) {
			sendUsage(label);
			return true;
		}
		if (oldStatus == newStatus) {
			sendError("Le serveur est déjà en mode " + oldStatus.getNameColored() + "&c.");
			return true;
		}
		if (args.length != 2 || !args[1].equalsIgnoreCase("dontKick")) {
			OlympaSpigotPermission needPermission = newStatus.getPermission() == null ? null : newStatus.getPermission().getUnderlying();
			if (needPermission != null && !needPermission.hasSenderPermission(sender)) {
				sendError("Tu n'a pas la permission d'être connecter si tu met le mode " + oldStatus.getNameColored() + "&c.");
				return true;
			}
			coreInstance.setStatus(newStatus);
			if (needPermission != null) {
				Consumer<? super Set<Player>> succes = players -> {
					this.sendMessage(players, Prefix.ERROR, "Le serveur est désormais en mode %s&c (Avant &4%s&c).", newStatus.getNameColored(), oldStatus.getNameColored());
				};
				Consumer<? super Collection<? extends Player>> empty = players -> {
					sendSuccess("Tu as kick " + players.size() + " joueur, qui n'ont pas la permission &6&n" + needPermission.getName() + "&a.");
					players.forEach(player -> player.kickPlayer(SpigotUtils.connectScreen("&eDésolé le serveur est désormais en mode " + newStatus.getNameColored() + "&e.\nEt tu n'y a plus accès. (pas d'inquiétudes, c'est temporaire !)")));
				};
				needPermission.getPlayers(succes, empty);
			}

		}
		sendSuccess("Le serveur est désormais en mode " + newStatus.getNameColored() + "&a, il était avant en mode " + oldStatus.getNameColored() + "&a.");
		CustomConfig config = coreInstance.getConfig();
		if (config == null)
			this.sendMessage(Prefix.BAD, "La config par default n'est pas charger, impossible de sauvegarder le changement de statut.");
		else {
			config.set("status", newStatus.getName());
			config.save();
			sendSuccess("La config a été également sauvegarder.");
		}
		return false;

	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
