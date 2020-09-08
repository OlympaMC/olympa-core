package fr.olympa.core.spigot.report.commands;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.report.ReportReason;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.report.gui.ReportGui;
import fr.olympa.core.spigot.report.gui.ReportGuiChoose;
import fr.olympa.core.spigot.report.gui.ReportGuiConfirm;

public class ReportCommand extends OlympaCommand {

	public ReportCommand(Plugin plugin) {
		super(plugin, "report", "signaler");
		allowConsole = false;
		addArgs(true, "joueur");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = this.player;
		if (args.length == 0) {
			ReportGuiChoose.open(player);
			return true;
		}

		String targetString = args[0];
		OfflinePlayer target = Bukkit.getPlayer(targetString);

		if (target == null) {
			sendUnknownPlayer(targetString);
			return true;
		}
		String note = null;
		if (args.length >= 2) {
			ReportReason reportReason = ReportReason.get(args[1]);
			if (reportReason != null) {
				if (args.length >= 3) {
					note = buildText(2, args);
				}
				ReportGuiConfirm.open(player, target, reportReason, note);
				return true;
			}
			note = buildText(1, args);
		}
		ReportGui.open(player, target, note);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1) {
			List<String> postentielNames = Utils.startWords(args[0], Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
			return postentielNames;
		} else if (args.length == 2) {
			List<String> postentielNames = Utils.startWords(args[0], Arrays.asList(ReportReason.values()).stream().map(ReportReason::getReason).collect(Collectors.toList()));
			return postentielNames;
		}
		return null;
	}

}
