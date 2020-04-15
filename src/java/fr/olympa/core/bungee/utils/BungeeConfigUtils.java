package fr.olympa.core.bungee.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import com.google.common.io.ByteStreams;

import fr.olympa.api.utils.ColorUtils;
import fr.olympa.core.bungee.OlympaBungee;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeeConfigUtils {

	private static HashMap<String, Configuration> configuration = new HashMap<>();
	private static String defaultConfig = "config";
	private static Plugin main = OlympaBungee.getInstance();

	/**
	 * Récupère donnés de la config
	 * @name = NomDeLaConfig.yml
	 */
	public static Configuration getConfig(String name) {
		name = name + ".yml";
		return configuration.get(name);
	}

	public static Configuration getDefaultConfig() {
		return getConfig(defaultConfig);
	}

	public static int getInt(String path) {
		return getDefaultConfig().getInt(path);
	}

	public static String getString(String path) {
		return ColorUtils.color(getDefaultConfig().getString(path));
	}

	public static String getString2(String path) {
		return getDefaultConfig().getString(path);
	}

	public static List<String> getStringList(String path) {
		return getDefaultConfig().getStringList(path);
	}

	/**
	 * Charge la config & met la met à jour si besoin
	 * @name = NomDeLaConfig.yml
	 */
	private static void loadConfig(String name) {
		name = name + ".yml";
		if (!main.getDataFolder().exists()) {
			main.getDataFolder().mkdir();
		}
		File configFile = new File(main.getDataFolder(), name);
		try {
			Configuration config;
			if (!configFile.exists()) {
				configFile.createNewFile();
				InputStream jarfile = main.getResourceAsStream(name);
				ByteStreams.copy(jarfile, new FileOutputStream(configFile));
				config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
			} else {
				InputStream jarfile = main.getResourceAsStream(name);
				Configuration jarconfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(jarfile);
				config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
				if (jarconfig.getDouble("version") > config.getDouble("version")) {
					configFile.renameTo(new File(main.getDataFolder(), name + " V" + config.getDouble("version")));
					configFile = new File(main.getDataFolder(), name);
					configFile.createNewFile();
					ByteStreams.copy(main.getResourceAsStream(name), new FileOutputStream(configFile));
					config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
				}
			}
			configuration.put(name, config);

			/*
			 * else { InputStream jarfile = main.getResourceAsStream(name); Configuration
			 * jarconfig =
			 * ConfigurationProvider.getProvider(YamlConfiguration.class).load(jarfile);
			 * config =
			 * ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
			 * if(jarconfig.getDouble("version") > config.getDouble("version")) {
			 * ByteStreams.copy(jarfile, new FileOutputStream(configFile)); } }
			 */

		} catch (IOException e) {
			ProxyServer.getInstance().getLogger().log(Level.SEVERE, ChatColor.RED + "Impossible de charger la config: " + name);
			e.printStackTrace();
		}
	}

	public static void loadConfigs() {
		configuration.clear();
		loadConfig(defaultConfig);
		loadConfig("maintenance");
	}

	/**
	 * Sauvegarde la config
	 * @name = NomDeLaConfig.yml
	 */
	public static void saveConfig(String name) {
		name = name + ".yml";
		try {
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration.get(name), new File(main.getDataFolder(), name));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
