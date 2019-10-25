package fr.olympa;

import org.bukkit.plugin.PluginManager;

import fr.olympa.api.gui.Inventories;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.plugin.OlympaPlugin;
import fr.olympa.core.ban.commands.BanCommand;
import fr.olympa.core.ban.commands.MuteCommand;
import fr.olympa.core.ban.commands.UnbanCommand;
import fr.olympa.core.ban.listeners.SanctionListener;
import fr.olympa.core.chat.ChatCommand;
import fr.olympa.core.chat.ChatListener;
import fr.olympa.core.datamanagment.listeners.DataManagmentListener;
import fr.olympa.core.datamanagment.redis.RedisAccess;
import fr.olympa.core.groups.GroupCommand;
import fr.olympa.core.groups.GroupListener;
import fr.olympa.core.report.commands.ReportCommand;
import fr.olympa.core.scoreboards.ScoreboardListener;

public class OlympaCore extends OlympaPlugin {

	public static OlympaCore getInstance() {
		return (OlympaCore) instance;
	}

	@Override
	public void onDisable() {
		this.disable();
		// ScoreboardPrefix.deleteTeams();
		RedisAccess.close();
		this.sendMessage("§4" + this.getDescription().getName() + "§c by Tristiisch (" + this.getDescription().getVersion() + ") is disabled.");
	}

	@Override
	public void onEnable() {
		OlympaPermission.registerPermissions(OlympaCorePermissions.class);
		this.enable(this);

		RedisAccess.init(this.getServer().getName());

		new GroupCommand(this).register();
		new ChatCommand(this).register();
		new ReportCommand(this).register();

		new BanCommand(this).register();
		new MuteCommand(this).register();
		new UnbanCommand(this).register();

		PluginManager pluginManager = this.getServer().getPluginManager();
		pluginManager.registerEvents(new DataManagmentListener(), this);
		pluginManager.registerEvents(new GroupListener(), this);
		pluginManager.registerEvents(new ChatListener(), this);
		pluginManager.registerEvents(new ScoreboardListener(), this);
		pluginManager.registerEvents(new TestListener(), this);
		pluginManager.registerEvents(new SanctionListener(), this);
		pluginManager.registerEvents(new Inventories(), this);

		this.sendMessage("§2" + this.getDescription().getName() + "§a by Tristiisch (" + this.getDescription().getVersion() + ") is activated.");
		// Thread.getAllStackTraces().keySet().forEach((t) ->
		// System.out.println(t.getName() + "\nIs Daemon " + t.isDaemon() + "\nIs Alive
		// " + t.isAlive()));
	}
}
