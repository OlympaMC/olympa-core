package fr.olympa.core.bungee.commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.ban.BanMySQL;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.vpn.OlympaVpn;
import fr.olympa.core.bungee.vpn.VpnHandler;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsBungee;
import fr.olympa.core.common.provider.AccountProvider;
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
				target = AccountProvider.getter().get(args[0]);
				if (target == null) {
					sendUnknownPlayer(args[0], AccountProvider.getter().getSQL().getNamesBySimilarChars(args[0]));
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
		out.addExtra(" §l| ");
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
		Map<Long, String> histName = target.getHistName();
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

		TxtComponentBuilder txtBuilder = new TxtComponentBuilder().extraSpliterBN();
		try {
			String ip = target.getIp();
			OlympaVpn ipInfo = VpnHandler.get(ip);
			List<String> users = ipInfo.getUsers();
			users.remove(target.getName());
			Map<Boolean, List<OlympaPlayer>> usersAll = AccountProvider.getter().getSQL().getPlayersByAllIp(ip, target).stream().collect(Collectors.partitioningBy(op -> op.getIp().equals(ip)));
			List<OlympaPlayer> usersAllNow = usersAll.get(true);
			users.removeAll(usersAllNow.stream().map(OlympaPlayer::getName).toList());
			List<OlympaPlayer> usersAllHistory = usersAll.get(false);
			users.removeAll(usersAllHistory.stream().map(OlympaPlayer::getName).toList());
			if (!usersAllNow.isEmpty())
				txtBuilder.extra(new TxtComponentBuilder("§cIP partagée actuellement avec " + ColorUtils.joinRedEt(usersAllNow))
						.onHoverText("&eLa dernière IP utilisés par ces joueurs est identique"));
			if (!usersAllHistory.isEmpty())
				txtBuilder.extra(new TxtComponentBuilder("§cIP déjà partager avec " + ColorUtils.joinRedEt(usersAllHistory))
						.onHoverText("&eL'adresse IP a déjà été utilisé par ces joueurs"));
			if (!users.isEmpty())
				txtBuilder.extra(new TxtComponentBuilder("§cIP a déjà essayé utiliser les pseudos " + ColorUtils.joinRedEt(users))
						.onHoverText("&eUne des personnes derrière cette IP a essayé de se connecter ces comptes là"));
			if (!txtBuilder.isEmpty()) {
				out.addExtra(txtBuilder.build());
				out.addExtra("\n");
			}
			if (hasPermission(OlympaCorePermissionsBungee.INFO_COMMAND_EXTRA)) {
				out2 = new TextComponent(TextComponent.fromLegacyText("§3IP : §b[Cachée]"));
				StringJoiner sj = new StringJoiner("\n");
				sj.add("§c" + target.getIp());
				sj.add("§6Clique pour copier l'IP");
				if (hasPermission(OlympaCorePermissionsBungee.INFO_COMMAND_EXTRA_EXTRA))
					sj.add("§e" + new Gson().toJson(ipInfo));
				out2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(sj.toString())));
				out2.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, target.getIp()));
				out.addExtra(out2);
				out.addExtra("\n");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		List<OlympaSanction> sanctions = BanMySQL.getSanctionByOlympaPlayer(target);
		out.addExtra(new TxtComponentBuilder("§3Sanctions : §b" + sanctions.size()).onHoverText("&eClique pour voir les sanctions").onClickCommand("/banhist %s", target.getName()).build());
		sendMessage(out);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, BungeeCommand command, String[] args) {
		if (args.length == 1) {
			List<String> postentielNames;
			postentielNames = Utils.startWords(args[0], AccountProvider.getter().getSQL().getNamesBySimilarName(args[0]));
			return postentielNames;
		}
		return new ArrayList<>();
	}
}
