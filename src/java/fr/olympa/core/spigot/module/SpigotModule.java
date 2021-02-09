package fr.olympa.core.spigot.module;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.afk.AfkHandler;
import fr.olympa.api.command.IOlympaCommand;
import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.command.essentials.AfkCommand;
import fr.olympa.api.module.OlympaModule;
import fr.olympa.api.module.PluginModule;
import fr.olympa.api.plugin.OlympaPluginInterface;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.scoreboards.NameTagCommand;
import fr.olympa.core.spigot.scoreboards.ScoreboardTeamListener;
import fr.olympa.core.spigot.scoreboards.api.NametagAPI;
import fr.olympa.core.spigot.vanish.VanishCommand;
import fr.olympa.core.spigot.vanish.VanishHandler;
import fr.olympa.core.spigot.vanish.VanishListener;

@SuppressWarnings("unchecked")
public class SpigotModule extends PluginModule {

	private static OlympaCore pl = OlympaCore.getInstance();

	public static OlympaModule<NametagAPI, Listener, OlympaCore, ComplexCommand> NAME_TAG = new OlympaModule<>(pl, "nameTag", plugin -> new NametagAPI(), ScoreboardTeamListener.class, NameTagCommand.class);
	public static OlympaModule<VanishHandler, Listener, OlympaCore, OlympaCommand> VANISH = new OlympaModule<>(pl, "vanish", plugin -> new VanishHandler(), VanishListener.class, VanishCommand.class);
	public static OlympaModule<AfkHandler, Listener, OlympaCore, OlympaCommand> AFK = new OlympaModule<>(pl, "afk", plugin -> new AfkHandler(), AfkHandler.class, AfkCommand.class);

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
