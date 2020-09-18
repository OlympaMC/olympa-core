package fr.olympa.core.bungee.api.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeeCustomConfig {

	private static List<BungeeCustomConfig> configs = new ArrayList<>();

	public static List<BungeeCustomConfig> getConfigs() {
		return configs;
	}

	public static BungeeCustomConfig getConfig(String name) {
		return configs.stream().filter(c -> c.getName().equals(name)).findFirst().orElse(configs.stream().filter(c -> c.fileName.equals(name)).findFirst().orElse(null));
	}

	private Plugin plugin;
	private String fileName;
	private Configuration configuration;

	public BungeeCustomConfig(Plugin plugin, String fileName) {
		this.plugin = plugin;
		if (!fileName.contains(".yml"))
			fileName += ".yml";
		this.fileName = fileName;
		configs.add(this);
	}

	public Configuration getConfig() {
		return configuration;
	}

	public void reload() throws IOException {
		load();
		plugin.getProxy().getPluginManager().callEvent(new BungeeConfigReloadEvent(getConfig()));
	}

	public void loadSafe() {
		try {
			load();
		} catch (IOException e) {
			ProxyServer.getInstance().getLogger().log(Level.SEVERE, ChatColor.RED + "Impossible de charger la config : " + fileName);
			e.printStackTrace();
		}
	}

	public void load() throws IOException {
		File folder = plugin.getDataFolder();
		if (!folder.exists())
			folder.mkdir();
		File configFile = new File(folder, fileName);
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
	}

	public void saveSafe() {
		try {
			save();
		} catch (IOException e) {
			ProxyServer.getInstance().getLogger().log(Level.SEVERE, ChatColor.RED + "Impossible de sauvegarder la config : " + fileName);
			e.printStackTrace();
		}
	}

	public void save() throws IOException {
		ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, new File(plugin.getDataFolder(), fileName));
	}

	public String getName() {
		return plugin.getDescription().getName() + "/" + fileName;
	}

}
