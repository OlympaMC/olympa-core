package fr.olympa.core.spigot.report.commands;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.google.gson.Gson;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.Matcher;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.report.OlympaReport;
import fr.olympa.core.spigot.report.connections.ReportMySQL;

// TODO
public class ReportSee extends OlympaCommand {

	public ReportSee(Plugin plugin) {
		super(plugin, "reportsee", OlympaCorePermissions.REPORT_SEE_COMMAND);
		addArgs(true, "joueur", "id");
		minArg = 1;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = this.player;
		String arg0 = args[0];

		if (Matcher.isInt(arg0)) {
			long id = Long.parseLong(arg0);
			OlympaReport report;
			try {
				report = ReportMySQL.getReport(id);
			} catch (SQLException e) {
				e.printStackTrace();
				sendError("Une erreur est survenu avec la base de donnés.");
				return true;
			}
			player.sendMessage(new Gson().toJson(report));
		} else if (Matcher.isUsername(arg0)) {
			String name = arg0;
		} else {
			sendUnknownPlayer(arg0);
			return true;
		}
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