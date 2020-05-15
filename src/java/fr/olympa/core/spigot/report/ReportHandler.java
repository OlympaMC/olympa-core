package fr.olympa.core.spigot.report;

import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.Prefix;
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

	public static void report(Player author, OfflinePlayer target, ReportReason reason, String note) {
		OlympaPlayer authorOlympaPlayer = AccountProvider.get(author.getUniqueId());
		OlympaPlayer targetOlympaPlayer;
		try {
			targetOlympaPlayer = new AccountProvider(target.getUniqueId()).get();
		} catch (SQLException e) {
			author.sendMessage(ColorUtils.color(Prefix.DEFAULT_BAD + "Une erreur est survenu, ton report n'a pas été enregistrer ..."));
			OlympaCore.getInstance().sendMessage("&4REPORT &cImpossible de récupérer l'id olympaPlayer de " + target.getName());
			e.printStackTrace();
			return;
		}
		OlympaReport report = new OlympaReport(targetOlympaPlayer.getId(), authorOlympaPlayer.getId(), reason, OlympaCore.getInstance().getServer().getName(), note);
		try {
			long id = ReportMySQL.createReport(report);
			report.setId(id);
			author.sendMessage(ColorUtils.color(Prefix.DEFAULT_GOOD + "Tu as signaler &2" + target.getName() + "&a pour &2" + reason.getReason() + "&a."));
		} catch (SQLException e) {
			e.printStackTrace();
			author.sendMessage(ColorUtils.color(Prefix.DEFAULT_BAD + "Une erreur est survenu, ton report n'a pas été sauvegardé mais le staff connecté est au courant."));
		}
		Bukkit.getPluginManager().callEvent(new OlympaReportAddEvent(author, target, report));
		sendAlert(report);
	}

	public static void sendAlert(OlympaReport report) {
		OlympaPlayerInformations author = AccountProvider.getPlayerInformations(report.getAuthorId());
		OlympaPlayer target1 = MySQL.getPlayer(report.getTargetId());
		OlympaPlayer targetRefresh = AccountProvider.get(target1.getUniqueId());
		if (targetRefresh == null) {
			targetRefresh = new AccountProvider(target1.getUniqueId()).getFromRedis();
		}
		if (targetRefresh != null) {
			target1 = targetRefresh;
		}
		OlympaPlayer target = target1;
		OlympaCorePermissions.REPORT_SEEREPORT.getPlayers(players -> {
			TextComponent out = new TextComponent();

			TextComponent tc = new TextComponent("[REPORT] ");
			tc.setColor(ChatColor.DARK_PURPLE);
			out.addExtra(tc);

			tc = new TextComponent(target.getName() + " ");
			if (target.isConnected()) {
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
