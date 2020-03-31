package fr.olympa.core.spigot.report;

import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.report.connections.ReportMySQL;
import fr.olympa.core.spigot.report.customevent.OlympaReportAddEvent;
import fr.olympa.core.spigot.report.items.ReportReason;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ReportHandler {

	public static void report(Player author, OfflinePlayer target, ReportReason reason) {
		OlympaReport report = new OlympaReport(target.getUniqueId(), author.getUniqueId(), reason, OlympaCore.getInstance().getServer().getName());
		try {
			long id = ReportMySQL.createReport(report);
			report.setId(id);
			author.sendMessage(SpigotUtils.color(Prefix.DEFAULT + "Tu as signaler &e" + target.getName() + "&6 pour &e" + reason.getReason() + "&6."));
		} catch (SQLException e) {
			e.printStackTrace();
			author.sendMessage(SpigotUtils.color(Prefix.DEFAULT + "Une erreur est survenu, ton report n'a pas été sauvegarder mais le staff connecté est au courant."));
		}
		Bukkit.getPluginManager().callEvent(new OlympaReportAddEvent(author, target, report));
		sendAlert(report);
	}

	public static void sendAlert(OlympaReport report) {
		OfflinePlayer author = Bukkit.getOfflinePlayer(report.getAuthor());
		OfflinePlayer target = Bukkit.getOfflinePlayer(report.getTarget());
		OlympaCorePermissions.REPORT_SEEREPORT.getPlayers(players -> {
			TextComponent out = new TextComponent();

			TextComponent tc = new TextComponent("[REPORT] ");
			tc.setColor(ChatColor.DARK_PURPLE);
			out.addExtra(tc);

			tc = new TextComponent(target.getName() + " ");
			if (target.isOnline()) {
				tc.setColor(ChatColor.LIGHT_PURPLE);
				tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Joueur encore connecté").color(ChatColor.GREEN).create()));
			} else {
				tc.setColor(ChatColor.RED);
				tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Joueur déconnecté").color(ChatColor.RED).create()));
			}
			out.addExtra(tc);

			tc = new TextComponent(" par ");
			tc.setColor(ChatColor.DARK_PURPLE);
			out.addExtra(tc);

			tc = new TextComponent(author.getName() + " ");
			tc.setColor(ChatColor.LIGHT_PURPLE);
			out.addExtra(tc);

			tc = new TextComponent(" pour ");
			tc.setColor(ChatColor.DARK_PURPLE);
			out.addExtra(tc);

			tc = new TextComponent(report.getReason().getReason());
			tc.setColor(ChatColor.LIGHT_PURPLE);
			out.addExtra(tc);

			tc = new TextComponent(".");
			tc.setColor(ChatColor.DARK_PURPLE);
			out.addExtra(tc);

			out.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Clique pour avoir plus d'info").color(ChatColor.YELLOW).create()));
			out.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "reportsee " + report.getId()));
			players.forEach(p -> p.spigot().sendMessage(out));
			Bukkit.getConsoleSender().spigot().sendMessage(out);
		});
	}
}
