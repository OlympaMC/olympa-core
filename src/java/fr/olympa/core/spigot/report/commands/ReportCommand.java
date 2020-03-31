package fr.olympa.core.spigot.report.commands;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.report.gui.ReportGui;

public class ReportCommand extends OlympaCommand {

	public ReportCommand(Plugin plugin) {
		super(plugin, "report", "signaler");
		this.allowConsole = false;
		this.addArgs(true, "joueur");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = this.player;
		String targetString = args[0];
		OfflinePlayer target = Bukkit.getPlayer(targetString);

		if (target == null) {
			this.sendUnknownPlayer(targetString);
			return true;
		}

		ReportGui.open(player, target);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1) {
			List<String> postentielNames = Utils.startWords(args[0], Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
			return postentielNames;
		}
		return null;
	}

}
