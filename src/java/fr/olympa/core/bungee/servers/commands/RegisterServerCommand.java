package fr.olympa.core.bungee.servers.commands;

import java.io.File;
import java.io.IOException;
import java.net.SocketAddress;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsBungee;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class RegisterServerCommand extends BungeeCommand {
	
	public RegisterServerCommand(Plugin plugin) {
		super(plugin, "registerserver", "Ajoute un serveur à la volée et le sauvegarde en config.", OlympaCorePermissionsBungee.SERVER_REGISTER_COMMAND);
		
		minArg = 3;
		usageString = "<server name> <server ip i.e. localhost:11111> <motd>";
	}
	
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		String name = args[0];
		if (ProxyServer.getInstance().getServers().containsKey(name)) {
			sendError("Ce serveur existe déjà.");
			return;
		}
		SocketAddress address = Util.getAddr(args[1]);
		if (address == null) {
			sendError("IP invalide.");
			return;
		}
		String motd = buildText(2, args);
		ServerInfo serverInfo = ProxyServer.getInstance().constructServerInfo(name, address, motd, false);
		ProxyServer.getInstance().getServers().put(name, serverInfo);
		sendSuccess("Serveur %s ajouté en runtime. Ajout dans la config...", name);
		
		File file = new File(ProxyServer.getInstance().getPluginsFolder().getParentFile(), "config.yml");
		
		try {
			ConfigurationProvider configurationProvider = YamlConfiguration.getProvider(YamlConfiguration.class);
			Configuration bungeeConfig = configurationProvider.load(file);
			
			bungeeConfig.set("servers." + serverInfo.getName() + ".motd", serverInfo.getMotd().replace(ChatColor.COLOR_CHAR, '&'));
			bungeeConfig.set("servers." + serverInfo.getName() + ".address", args[1]);
			bungeeConfig.set("servers." + serverInfo.getName() + ".restricted", false);
			
			configurationProvider.save(bungeeConfig, file);
			sendSuccess("Serveur %s ajouté à la configuration!", name);
		}catch (IOException e) {
			sendError(e);
			e.printStackTrace();
		}
		OlympaBungee.getInstance().sendMessage("§lServeur %s ajouté!", name);
	}
	
}
