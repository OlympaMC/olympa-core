package fr.olympa.core.spigot.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.machine.JavaInstanceInfo;
import fr.olympa.api.common.machine.TpsMessage;
import fr.olympa.api.common.module.OlympaModule.ModuleApi;
import fr.olympa.api.common.permission.list.OlympaCorePermissionsSpigot;
import fr.olympa.api.common.plugin.OlympaAPIPlugin;
import fr.olympa.api.common.task.OlympaTask;
import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.api.spigot.utils.TPS;
import fr.olympa.api.spigot.utils.TPSUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;

public class TpsCommand extends OlympaCommand implements Listener, ModuleApi<OlympaCore> {

	List<Player> players;
	Integer taskId;

	public TpsCommand(OlympaCore plugin) {
		super(plugin, "tps", "Affiche des informations sur l'état du serveur.", OlympaCorePermissionsSpigot.SPIGOT_LAG_COMMAND, "lag", "lagbar", "tpsbar");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (label.endsWith("bar") && !isConsole()) {
			if (toggle(player))
				sendMessage(Prefix.DEFAULT_GOOD, "La TPS ActionBar a été activée.");
			else
				sendMessage(Prefix.DEFAULT_BAD, "La TPS ActionBar a été désactivée.");
		} else
			sendComponents(new TpsMessage(player == null).getInfoMessage().build());
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (players.contains(player))
			disable(player);
	}

	public boolean toggle(Player player) {
		boolean contains = players.contains(player);
		if (contains)
			disable(player);
		else
			enable(player);
		return !contains;
	}

	public void enable(Player player) {
		players.add(player);
		if (players.size() == 1)
			enableTask();
	}

	public void disable(Player player) {
		players.remove(player);
		if (players.isEmpty())
			disableTask();
	}

	public boolean disableTask() {
		if (taskId == null)
			return false;
		OlympaTask task = ((OlympaAPIPlugin) plugin).getTask();
		task.cancelTaskById(taskId);
		taskId = null;
		return true;
	}

	public boolean enableTask() {
		if (taskId != null)
			return false;
		OlympaTask task = ((OlympaAPIPlugin) plugin).getTask();
		taskId = task.scheduleSyncRepeatingTask(() -> {
			double[] tps = TPS.getDoubleTPS();
			TxtComponentBuilder textBuilder = new TxtComponentBuilder();
			JavaInstanceInfo mi = new JavaInstanceInfo();
			textBuilder.extra("&3RAM: &b%s&3", mi.getMemUsage().replace("%", "%%"));
			textBuilder.extra(" ");
			textBuilder.extra("&3CPU Serv: &b%s&3", mi.getCPUUsage().replace("%", "%%"));
			textBuilder.extra(" ");
			textBuilder.extra("&3TPS: &b1m %s&b 5m %s&b 15m %s ", TPSUtils.getTpsColor(tps[0]), TPSUtils.getTpsColor(tps[1]), TPSUtils.getTpsColor(tps[2]));
			players.forEach(p -> p.sendActionBar(textBuilder.build()));
		}, 0, 1, TimeUnit.SECONDS);
		return true;
	}

	@Override
	public boolean disable(OlympaCore plugin) {
		disableTask();
		players.clear();
		players = null;
		return false;
	}

	@Override
	public boolean enable(OlympaCore plugin) {
		players = new ArrayList<>();
		return false;
	}

	@Override
	public boolean setToPlugin(OlympaCore plugin) {
		if (this.plugin == null)
			this.plugin = plugin;
		return true;
	}

	@Override
	public boolean isEnabled() {
		return players != null;
	}

}
