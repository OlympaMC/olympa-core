package fr.olympa.core.bungee.commands;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.bungee.permission.OlympaBungeePermission;
import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.plugin.PluginInfoAdvanced;
import fr.olympa.core.bungee.servers.MonitorInfoBungee;
import fr.olympa.core.bungee.servers.MonitorServers;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class CreditCommand extends BungeeCommand {

	public CreditCommand(Plugin plugin) {
		super(plugin, "credit", "Affiche tous les devs ayant participé au serveur", (OlympaBungeePermission) null, "author", "auteur");
		minArg = 0;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		TxtComponentBuilder out = new TxtComponentBuilder("&eListe non exhaustive des développeurs de nos plugins > ").extraSpliterBN();
		Collection<MonitorInfoBungee> allServers = MonitorServers.getServers();
		List<String> serversNames = allServers.stream().filter(mib -> mib.getServerDebugInfo() != null).map(mib -> mib.getOlympaServer().getNameCaps()).distinct().collect(Collectors.toList());
		serversNames.forEach(name -> {
			List<MonitorInfoBungee> servers = allServers.stream().filter(mib -> mib.getServerDebugInfo() != null
					&& mib.getServerDebugInfo().getPlugins() != null && !mib.getServerDebugInfo().getPlugins().isEmpty()
					&& name.equals(mib.getOlympaServer().getNameCaps()))
					.collect(Collectors.toList());
			List<PluginInfoAdvanced> allPlugins = servers.stream().map(mib -> mib.getServerDebugInfo().getPlugins()).flatMap(list -> list.stream()).distinct().collect(Collectors.toList());
			String authorPluginOlympa = allPlugins.stream().filter(pl -> pl.getName().startsWith("Olympa")).map(pl -> pl.getAuthors()).flatMap(list -> list.stream()).distinct().collect(Collectors.joining(", "));
			String otherAuthors = allPlugins.stream().filter(pl -> !pl.getName().startsWith("Olympa")).map(pl -> pl.getAuthors()).flatMap(list -> list.stream()).distinct().collect(Collectors.joining(", "));
			out.extra(new TxtComponentBuilder("&6" + name + "\n&2Devs Olympa &a" + authorPluginOlympa + "\n&7Devs Plugin Public " + otherAuthors));
		});
		sendMessage(out.build());
	}
}
