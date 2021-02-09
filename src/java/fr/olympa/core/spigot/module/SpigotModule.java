package fr.olympa.core.spigot.module;

import java.util.Arrays;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.afk.AfkHandler;
import fr.olympa.api.command.IOlympaCommand;
import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.command.essentials.AfkCommand;
import fr.olympa.api.module.OlympaModule;
import fr.olympa.api.module.PluginModule;
import fr.olympa.api.plugin.OlympaPluginInterface;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.scoreboards.ScoreboardTeamListener;
import fr.olympa.core.spigot.scoreboards.api.NametagAPI;
import fr.olympa.core.spigot.vanish.VanishCommand;
import fr.olympa.core.spigot.vanish.VanishHandler;
import fr.olympa.core.spigot.vanish.VanishListener;

@SuppressWarnings("unchecked")
public class SpigotModule extends PluginModule {

	private static OlympaCore pl = OlympaCore.getInstance();

	public static final OlympaModule<NametagAPI, Listener, OlympaCore, OlympaCommand> NAME_TAG = new OlympaModule<>(pl, "nameTag", plugin -> new NametagAPI(), (plugin, api) -> plugin.setNameTagApi(api),
			Arrays.asList(ScoreboardTeamListener.class), null);

	public static final OlympaModule<VanishHandler, Listener, OlympaCore, OlympaCommand> VANISH = new OlympaModule<>(pl, "vanish", plugin -> new VanishHandler(), (plugin, api) -> plugin.setVanishApi(api),
			Arrays.asList(VanishListener.class), Arrays.asList(VanishCommand.class));

	public static final OlympaModule<AfkHandler, Listener, OlympaCore, OlympaCommand> AFK = new OlympaModule<>(pl, "afk", plugin -> new AfkHandler(), (plugin, api) -> plugin.setAfkApi(api),
			Arrays.asList(AfkHandler.class), Arrays.asList(AfkCommand.class));

	//	public static void registerListener() {
	//		for (OlympaModule<? extends Object, Listener, ? extends Plugin> module : modules)
	//			registerListener(module);
	//	}
	//
	//	public static void unregisterListener() {
	//		for (OlympaModule<? extends Object, Listener, ? extends Plugin> module : modules)
	//			unregisterListener(module);
	//	}

	public static void enable() {
		for (OlympaModule<? extends Object, Listener, ? extends OlympaPluginInterface, ? extends IOlympaCommand> module : modules)
			enableModule((OlympaModule<? extends Object, Listener, ? extends Plugin, ? extends IOlympaCommand>) module);
	}

	public static void disable() {
		for (OlympaModule<? extends Object, Listener, ? extends OlympaPluginInterface, ? extends IOlympaCommand> module : modules)
			disableModule((OlympaModule<? extends Object, Listener, ? extends Plugin, ? extends IOlympaCommand>) module);
	}

	static {
		PluginModule.addModule(NAME_TAG);
		PluginModule.addModule(VANISH);
		PluginModule.addModule(AFK);
	}
	//	static final List<OlympaModule<? extends Object, Listener, ? extends Plugin, ? extends IOlympaCommand>> modules = List.of(NAME_TAG, VANISH, AFK);

}
