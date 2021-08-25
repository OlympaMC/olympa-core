package fr.olympa.core.bungee.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.bungee.permission.OlympaBungeePermission;
import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.plugin.PluginInfoAdvanced;
import fr.olympa.api.common.server.ServerInfoAdvanced;
import fr.olympa.api.common.server.ServerInfoAdvancedBungee;
import fr.olympa.core.bungee.OlympaBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class CreditCommand extends BungeeCommand {

	public CreditCommand(Plugin plugin) {
		super(plugin, "credit", "Affiche tous les devs ayant participé au serveur", (OlympaBungeePermission) null, "author", "auteur");
		minArg = 0;
	}

	public static List<String> convert(List<PluginInfoAdvanced> pia) {
		return pia.stream().map(pl -> pl.getAllAuthors()).flatMap(list -> list.stream()).distinct().collect(Collectors.toList());
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		TxtComponentBuilder out = new TxtComponentBuilder("&eListe non exhaustive des développeurs de nos plugins > ").extraSpliter("\n\n");
		Collection<ServerInfoAdvancedBungee> allServers = OlympaBungee.getInstance().getMonitoring().getServers();
		List<String> serversNames = allServers.stream().filter(mib -> mib.hasFullInfos()).map(mib -> mib.getOlympaServer().getNameCaps()).distinct().collect(Collectors.toList());
		ServerInfoAdvancedBungee bungeeServerInfo = new ServerInfoAdvancedBungee(OlympaBungee.getInstance());
		List<String> authorPluginOlympaList = new ArrayList<>();
		List<String> otherAuthorsList = new ArrayList<>();
		//		StringJoiner otherAuthorsList = new StringJoiner(", ", "&7Devs Plugin Public ", "");
		if (bungeeServerInfo.getPlugins() != null && !bungeeServerInfo.getPlugins().isEmpty()) {
			Map<Boolean, List<PluginInfoAdvanced>> pluginsOlympaOrOther = bungeeServerInfo.getPlugins().stream().collect(Collectors.partitioningBy(pl -> pl.getName().startsWith("Olympa")));
			authorPluginOlympaList.addAll(convert(pluginsOlympaOrOther.get(true)));
			otherAuthorsList.addAll(convert(pluginsOlympaOrOther.get(false)));
		}
		serversNames.stream().forEach(name -> {
			List<ServerInfoAdvanced> servers = allServers.stream().filter(mib -> mib.hasFullInfos() && mib.getPlugins() != null && !mib.getPlugins().isEmpty()
					&& name.equals(mib.getOlympaServer().getNameCaps()))
					.collect(Collectors.toList());
			List<PluginInfoAdvanced> allPlugins = new ArrayList<>();
			servers.stream().forEach(serv -> {
				if (!serv.getPlugins().isEmpty())
					serv.getPlugins().stream().forEach(pl -> {
						if (!pl.getAuthors().isEmpty())
							allPlugins.add(pl);
					});
			});
			//			List<PluginInfoAdvanced> allPlugins = servers.stream().map(mib -> mib.getServerDebugInfo().getPlugins())
			//					.flatMap(list -> list.stream()).distinct().filter(pl -> !pl.getAuthors().isEmpty()).collect(Collectors.toList());
			Map<Boolean, List<PluginInfoAdvanced>> pluginsOlympaOrOther = allPlugins.stream().collect(Collectors.partitioningBy(pl -> pl.getName().startsWith("Olympa")));
			authorPluginOlympaList.addAll(convert(pluginsOlympaOrOther.get(true)));
			otherAuthorsList.addAll(convert(pluginsOlympaOrOther.get(false)));
			//			String authorPluginOlympa = pluginsOlympaOrOther.get(true).stream().map(pl -> pl.getAllAuthors()).flatMap(list -> list.stream()).distinct().collect(Collectors.joining(", "));
			//			String otherAuthors = pluginsOlympaOrOther.get(false).stream().map(pl -> pl.getAllAuthors()).flatMap(list -> list.stream()).distinct().collect(Collectors.joining(", "));
			//			out.extra(new TxtComponentBuilder("&6" + name + "\n&2Devs Olympa &a" + authorPluginOlympa + "\n&7Devs Plugin Public " + otherAuthors));
		});

		out.extra(new TxtComponentBuilder("&2Devs Olympa &a" + authorPluginOlympaList.stream().distinct().collect(Collectors.joining("&2, &a"))
				+ "\n&7Devs Plugin Public " + otherAuthorsList.stream().distinct().collect(Collectors.joining(", "))));
		sendMessage(out.build());
	}
}
