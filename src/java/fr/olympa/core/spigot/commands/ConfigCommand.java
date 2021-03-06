package fr.olympa.core.spigot.commands;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.stream.Collectors;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.spigot.command.ComplexCommand;
import fr.olympa.api.spigot.config.CustomConfig;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsSpigot;

public class ConfigCommand extends ComplexCommand {

	public ConfigCommand(Plugin plugin) {
		super(plugin, "config", "Gère les configs spigot.", OlympaCorePermissionsSpigot.SPIGOT_CONFIG_COMMAND);
		addArgumentParser("CONFIGS", (sender, arg) -> CustomConfig.getConfigs().stream().map(CustomConfig::getName).collect(Collectors.toList()), x -> {
			return CustomConfig.getConfig(x);
		}, x -> String.format("&cLa config &4%s&c n'existe pas.", x));
	}

	@Cmd(args = { "CONFIGS", "reload|save" }, otherArg = true, syntax = "<config> <reload|save>", min = 2)
	public void other(CommandContext cmd) {
		CustomConfig config = cmd.getArgument(0);
		String reloadOrSave = ((String) cmd.getArgument(1)).toLowerCase();
		if (reloadOrSave.equals("reload")) {
			long time = System.nanoTime();
			try {
				config.reload();
				time = System.nanoTime() - time;
				sendMessage(Prefix.DEFAULT_GOOD, "Config &2%s&a chargé en &2%s ms", config.getName(), new DecimalFormat("0.#").format(time / 10000000d));
			} catch (IOException | InvalidConfigurationException e) {
				sendMessage(Prefix.ERROR, "Impossible de charger la config &4%s&c : &4%s&c.", config.getName(), e.getMessage());
				e.printStackTrace();
			}
			sendSuccess("Configuration reload.");
		} else if (reloadOrSave.equals("save")) {
			long time = System.nanoTime();
			try {
				config.saveUnSafe();
				time = System.nanoTime() - time;
				sendMessage(Prefix.DEFAULT_GOOD, "Config &2%s&a sauvegarder en &2%s ms", config.getName(), new DecimalFormat("0.#").format(time / 10000000d));
			} catch (IOException e) {
				sendMessage(Prefix.ERROR, "Impossible de sauvegarder la config &4%s&c sur le disque : &4%s&c.", config.getName(), e.getMessage());
				e.printStackTrace();
			}
			sendSuccess("Configuration reload.");
		}
	}
}
