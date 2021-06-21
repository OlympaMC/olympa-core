package fr.olympa.core.spigot.module;

import org.bukkit.event.Listener;

import fr.olympa.api.common.module.OlympaModule;
import fr.olympa.api.common.module.SpigotModule;
import fr.olympa.api.spigot.afk.AfkHandler;
import fr.olympa.api.spigot.chat.ChatCatcher;
import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.api.spigot.command.essentials.AfkCommand;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.commands.TpsCommand;
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

	private static final TpsCommand tpsCommand = new TpsCommand(pl);
	public static final OlympaModule<TpsCommand, Listener, OlympaCore, OlympaCommand> TPS = new SpigotModule<>(pl, "tps", plugin -> tpsCommand).commandPreProcess()
			.cmd(tpsCommand.getClass()).listener(tpsCommand.getClass()).softDepend(NAME_TAG);
	private static final ChatCatcher chatCatcher = new ChatCatcher(pl);
	public static final OlympaModule<ChatCatcher, Listener, OlympaCore, OlympaCommand> CHAT_CATCHER = new SpigotModule<>(pl, "chatcatcher", plugin -> chatCatcher)
			.cmd(chatCatcher.getClass()).listener(chatCatcher.getClass());

	public CoreModules() {
		NAME_TAG.registerModule();
		VANISH.registerModule();
		AFK.registerModule();
		TPS.registerModule();
		CHAT_CATCHER.registerModule();
	}

}
