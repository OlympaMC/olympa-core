package fr.olympa.core.spigot.module;

import org.bukkit.event.Listener;

import fr.olympa.api.afk.AfkHandler;
import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.command.essentials.AfkCommand;
import fr.olympa.api.module.OlympaModule;
import fr.olympa.api.module.SpigotModule;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.scoreboards.NameTagCommand;
import fr.olympa.core.spigot.scoreboards.ScoreboardTeamListener;
import fr.olympa.core.spigot.scoreboards.api.NametagAPI;
import fr.olympa.core.spigot.vanish.VanishCommand;
import fr.olympa.core.spigot.vanish.VanishHandler;
import fr.olympa.core.spigot.vanish.VanishListener;

public class CoreModules {

	private static OlympaCore pl = OlympaCore.getInstance();

	public static final OlympaModule<NametagAPI, Listener, OlympaCore, OlympaCommand> NAME_TAG = new SpigotModule<>(pl, "nameTag", plugin -> new NametagAPI())
			.cmd(NameTagCommand.class).listener(ScoreboardTeamListener.class);
	public static final OlympaModule<VanishHandler, Listener, OlympaCore, OlympaCommand> VANISH = new SpigotModule<>(pl, "vanish", plugin -> new VanishHandler())
			.cmd(VanishCommand.class).listener(VanishListener.class).softDepend(NAME_TAG);
	public static final OlympaModule<AfkHandler, Listener, OlympaCore, OlympaCommand> AFK = new SpigotModule<>(pl, "afk", plugin -> new AfkHandler())
			.cmd(AfkCommand.class).listener(AfkHandler.class).softDepend(NAME_TAG);

	public CoreModules() {
		NAME_TAG.registerModule();
		VANISH.registerModule();
		AFK.registerModule();
	}

}
