package fr.olympa.core.spigot.module;

import org.bukkit.event.Listener;

import fr.olympa.api.afk.AfkHandler;
import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.command.essentials.AfkCommand;
import fr.olympa.api.module.OlympaModule;
import fr.olympa.api.module.ClassLoader;
import fr.olympa.api.module.SpigotModule;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.scoreboards.NameTagCommand;
import fr.olympa.core.spigot.scoreboards.ScoreboardTeamListener;
import fr.olympa.core.spigot.scoreboards.api.NametagAPI;
import fr.olympa.core.spigot.vanish.VanishCommand;
import fr.olympa.core.spigot.vanish.VanishHandler;
import fr.olympa.core.spigot.vanish.VanishListener;

@SuppressWarnings("unchecked")
public class CoreModules extends ClassLoader {

	private static OlympaCore pl = OlympaCore.getInstance();

	public static final OlympaModule<NametagAPI, Listener, OlympaCore, OlympaCommand> NAME_TAG = new SpigotModule<>(pl, "nameTag", plugin -> new NametagAPI())
			.cmd(NameTagCommand.class).listener(ScoreboardTeamListener.class);
	public static final OlympaModule<VanishHandler, Listener, OlympaCore, OlympaCommand> VANISH = new SpigotModule<>(pl, "vanish", plugin -> new VanishHandler())
			.cmd(VanishCommand.class).listener(VanishListener.class).softDepend(NAME_TAG);
	public static final OlympaModule<AfkHandler, Listener, OlympaCore, OlympaCommand> AFK = new SpigotModule<>(pl, "afk", plugin -> new AfkHandler())
			.cmd(AfkCommand.class).listener(AfkHandler.class).softDepend(NAME_TAG);

	static {
		NAME_TAG.registerModule();
		VANISH.registerModule();
		AFK.registerModule();
	}
	//	static final List<OlympaModule<? extends Object, Listener, ? extends Plugin, ? extends IOlympaCommand>> modules = List.of(NAME_TAG, VANISH, AFK);

}
