package fr.olympa.core.bungee.commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import com.google.gson.Gson;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.commun.permission.list.OlympaCorePermissionsBungee;
import fr.olympa.api.commun.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.ban.BanMySQL;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.vpn.OlympaVpn;
import fr.olympa.core.bungee.vpn.VpnHandler;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;

public class InfoCommand extends BungeeCommand implements TabExecutor {

	public InfoCommand(Plugin plugin) {
		super(plugin, "info", OlympaCorePermissionsBungee.INFO_COMMAND);
		minArg = 0;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		OlympaPlayer target = null;
		ProxiedPlayer targetProxied = null;
		if (args.length != 0)
			try {
				target = AccountProvider.get(args[0]);
				if (target == null) {
					sendUnknownPlayer(args[0], AccountProvider.getSQL().getNamesBySimilarChars(args[0]));
					return;
				}
				targetProxied = ProxyServer.getInstance().getPlayer(target.getUniqueId());
			} catch (SQLException e) {
				sendError(e);
				e.printStackTrace();
				return;
			}
		else if (proxiedPlayer != null) {
			target = getOlympaPlayer();
			targetProxied = proxiedPlayer;
		} else {
			sendImpossibleWithConsole();
			return;
		}

		TextComponent out = new TextComponent();
		TextComponent out2 = new TextComponent(TextComponent.fromLegacyText(Prefix.DEFAULT_GOOD.formatMessage("§6Info §e%s #%d", target.getName(), target.getId())));
		out.addExtra(out2);
		out.addExtra("\n");
		out2 = new TextComponent(TextComponent.fromLegacyText("§3Première connexion : §b" + Utils.timestampToDuration(target.getFirstConnection())));
		out2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§3Le: §b" + Utils.timestampToDate(target.getFirstConnection()))));
		out.addExtra(out2);
		out.addExtra("\n");
		if (targetProxied == null) {
			out2 = new TextComponent(TextComponent.fromLegacyText("§3Dernière : §b" + Utils.timestampToDuration(target.getLastConnection())));
			out2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§3Le: §b" + Utils.timestampToDate(target.getLastConnection()))));
		} else
			out2 = new TextComponent(TextComponent.fromLegacyText("§3Connecté depuis : §b" + Utils.timestampToDuration(target.getLastConnection())));
		out.addExtra(out2);
		out.addExtra("\n");
		out2 = new TextComponent(TextComponent.fromLegacyText("§3Grades : §b" + target.getGroupsToHumainString()));
		out.addExtra(out2);
		out.addExtra("\n");
		Map<Long, String> histName = target.getHistHame();
		if (!histName.isEmpty()) {
			int size = histName.size();
			String s = Utils.withOrWithoutS(size);
			out2 = new TextComponent(TextComponent.fromLegacyText("§3Ancien%s pseudo%s : ".replace("%s", s)));
			out.addExtra(out2);
			for (Entry<Long, String> entry : histName.entrySet()) {
				out2 = new TextComponent(TextComponent.fromLegacyText("§b" + entry.getValue()));
				out2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
						String.format("§cChangé depuis §4%s§c, le §4%s", Utils.timestampToDuration(entry.getKey()), Utils.timestampToDate(target.getLastConnection())))));
				out.addExtra(out2);
				if (--size != 0)
					out.addExtra(new TextComponent(TextComponent.fromLegacyText("§3, §b")));
			}
			out.addExtra("\n");
		}
		if (target.isPremium())
			out.addExtra(TxtComponentBuilder.of(null, "&3Premium : &boui", ClickEvent.Action.COPY_TO_CLIPBOARD, target.getPremiumUniqueId().toString(), HoverEvent.Action.SHOW_TEXT,
					new Text("§eClique pour copier l'UUID premium dans le presse-papier")));
		else
			out.addExtra(new TextComponent(TextComponent.fromLegacyText("§3Premium : §bnon")));

		//		out.addExtra(out2);
		//		out.addExtra("\n");
		//		out2 = new TextComponent(TextComponent.fromLegacyText("§3Link Discord: §b" + (target.getDiscordId() != 0 ? "oui" : "non")));
		out.addExtra("\n");
		out.addExtra(
				TxtComponentBuilder.of(null, "&3UUID : &b" + target.getUniqueId(), ClickEvent.Action.COPY_TO_CLIPBOARD, target.getUniqueId().toString(), HoverEvent.Action.SHOW_TEXT,
						new Text("§eClique pour copier l'UUID dans le presse-papier")));
		out.addExtra("\n");
		if (hasPermission(OlympaCorePermissionsBungee.INFO_COMMAND_EXTRA)) {
			out2 = new TextComponent(TextComponent.fromLegacyText("§3IP : §b[Cachée]"));
			StringJoiner sj = new StringJoiner("\n");
			sj.add("§c" + target.getIp());
			try {
				String ip = target.getIp();
				OlympaVpn ipInfo = VpnHandler.get(ip);
				List<String> users = ipInfo.getUsers();
				users.remove(target.getName());
				Map<Boolean, List<OlympaPlayer>> usersAll = AccountProvider.getSQL().getPlayersByAllIp(ip);
				List<OlympaPlayer> usersAllNow = usersAll.get(true);
				usersAllNow.remove(target);
				List<OlympaPlayer> usersAllHistory = usersAll.get(false);
				usersAllHistory.remove(target);
				if (!usersAllNow.isEmpty())
					sj.add("§cIP partagée actuellement (dernière IP utilisé pour les deux) avec " + ColorUtils.joinRedEt(usersAllNow));
				if (!usersAllHistory.isEmpty())
					sj.add("§cIP déjà partager (IP dans l'historique) avec " + ColorUtils.joinRedEt(usersAllHistory));
				if (!users.isEmpty())
					sj.add("§cL'IP a déjà essayé utiliser les pseudo " + ColorUtils.joinRedEt(users));
				if (hasPermission(OlympaCorePermissionsBungee.INFO_COMMAND_EXTRA_EXTRA))
					sj.add("§e" + new Gson().toJson(ipInfo));
			} catch (SQLException e) {
				e.printStackTrace();
			}
			out2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(sj.toString())));
			out2.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, target.getIp()));
			out.addExtra(out2);
			out.addExtra("\n");
		}
		List<OlympaSanction> sanctions = BanMySQL.getSanctions(target.getUniqueId());
		out2 = new TextComponent(TextComponent.fromLegacyText("§3Sanctions : §b" + sanctions.size()));
		out.addExtra(out2);
		sendMessage(out);
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		if (args.length == 1) {
			List<String> postentielNames;
			postentielNames = Utils.startWords(args[0], AccountProvider.getSQL().getNamesBySimilarName(args[0]));
			return postentielNames;
		}
		return new ArrayList<>();
	}
}
