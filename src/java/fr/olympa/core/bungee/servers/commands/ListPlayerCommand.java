package fr.olympa.core.bungee.servers.commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.server.ServerInfoAdvancedBungee;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsBungee;
import fr.olympa.core.common.provider.AccountProvider;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class ListPlayerCommand extends BungeeCommand {

	public ListPlayerCommand(Plugin plugin) {
		super(plugin, "listplayer", OlympaCorePermissionsBungee.PLAYER_LIST_COMMAND);
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		Collection<ProxiedPlayer> globalPlayers = ProxyServer.getInstance().getPlayers();
		TxtComponentBuilder out = new TxtComponentBuilder("§7➤ &6Liste des joueurs : §a(" + globalPlayers.size() + ")").extraSpliterBN();

		Map<ServerInfoAdvancedBungee, List<ProxiedPlayer>> servers = new HashMap<>();
		globalPlayers.forEach(player -> {
			servers.computeIfAbsent(player.getServer() == null ? null : OlympaBungee.getInstance().getMonitoring().getMonitor(player.getServer().getInfo()), server -> new ArrayList<>()).add(player);
		});

		boolean canSeeVanish = OlympaCorePermissionsBungee.VANISH_SEE_ADMIN.hasSenderPermissionBungee(sender);
		servers.entrySet().stream().sorted((o1, o2) -> Integer.compare(o2.getValue().size(), o1.getValue().size())).forEach(entry -> {
			@Nullable
			ServerInfoAdvancedBungee server = entry.getKey();
			List<ProxiedPlayer> players = new ArrayList<>();
			players.addAll(entry.getValue());
			if (!canSeeVanish)
				players.removeIf(p -> {
					try {
						OlympaPlayer op = new AccountProvider(p.getUniqueId()).get();
						return op != null && op.isVanish();
					} catch (SQLException e) {
						e.printStackTrace();
					}
					return false;
				});
			TxtComponentBuilder out2 = new TxtComponentBuilder().extraSpliter(" ");

			if (server != null) {
				out2.extra("§7[%s§7]", server.getStatus().getNameColored());
				out2.extra("%s%s", server.getStatus().getColor(), server.getName());
			} else
				out2.extra("§7[%s§7]", "§cEn connexion");
			out2.extra("§e(%d):", players.size());

			for (int i = 0; i < players.size(); i++) {
				ProxiedPlayer player = players.get(i);
				OlympaPlayer oplayer;
				AccountProvider account = new AccountProvider(player.getUniqueId());
				//				oplayer = new AccountProvider(player.getUniqueId()).get();
				oplayer = account.getFromCache();
				if (oplayer == null)
					oplayer = account.getFromRedis();

				TxtComponentBuilder playerC;
				if (oplayer == null)
					playerC = new TxtComponentBuilder("§f%s", player.getName()).onHoverText("Impossible de trouver un OlympaPlayer");
				else
					playerC = new TxtComponentBuilder("%s%s", oplayer.getGroup().getColor(), oplayer.getName()).onHoverText(oplayer.getGroupNameColored());
				if (i + 1 < players.size())
					playerC.extra(",");
				out2.extra(playerC);
			}

			out.extra(out2);
		});

		sender.sendMessage(out.build());
	}

}
