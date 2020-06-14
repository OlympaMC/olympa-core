package fr.olympa.core.bungee.api.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeeCustomConfig {
	
	private Plugin plugin;
	private String fileName;
	private Configuration configuration;
	
	public BungeeCustomConfig(Plugin plugin, String fileName) {
		this.plugin = plugin;
		if (!fileName.contains(".yml"))
			fileName += ".yml";
		this.fileName = fileName;
	}
	
	public Configuration getConfig() {
		return configuration;
	}
	
	public void reload() {
		load();
		plugin.getProxy().getPluginManager().callEvent(new BungeeConfigReloadEvent(getConfig()));
	}
	
	/**
	 * Charge la config & met la met à jour si besoin
	 *
	 * @name = NomDeLaConfig.yml
	 */
	public void load() {
		File folder = plugin.getDataFolder();
		if (!folder.exists())
			folder.mkdir();
		File configFile = new File(folder, fileName);
		try {
			if (!configFile.exists()) {
				configFile.createNewFile();
				InputStream jarfile = plugin.getResourceAsStream(fileName);
				ByteStreams.copy(jarfile, new FileOutputStream(configFile));
				configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
			} else {
				InputStream jarfile = plugin.getResourceAsStream(fileName);
				Configuration jarconfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(jarfile);
				configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
				if (jarconfig.getDouble("version") > configuration.getDouble("version")) {
					configFile.renameTo(new File(folder, fileName + " V" + configuration.getDouble("version")));
					configFile = new File(folder, fileName);
					configFile.createNewFile();
					ByteStreams.copy(plugin.getResourceAsStream(fileName), new FileOutputStream(configFile));
					configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
				}
			}
			
		} catch (IOException e) {
			ProxyServer.getInstance().getLogger().log(Level.SEVERE, ChatColor.RED + "Impossible de charger la config: " + fileName);
			e.printStackTrace();
		}
	}
	
	/**
	 * Sauvegarde la config
	 *
	 * @name = NomDeLaConfig.yml
	 */
	public void save() {
		try {
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, new File(plugin.getDataFolder(), fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
