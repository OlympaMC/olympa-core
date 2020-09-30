package fr.olympa.core.spigot.afk;

import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.task.OlympaTask;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.scoreboards.api.NametagAPI;

public class AfkPlayer {

	boolean afk;
	String lastAction;
	String lastSuffix;
	long time;
	BukkitTask task;

	public AfkPlayer(Player player) {
		afk = false;
		launchTask(player);
	}

	public boolean disableTask() {
		if (task != null) {
			task.cancel();
			return true;
		}
		return false;
	}

	void launchTask(Player player) {
		disableTask();
		OlympaTask taskHandler = LinkSpigotBungee.Provider.link.getTask();
		int id = taskHandler.runTaskLater(() -> {
			setAfk(player);
		}, 20, TimeUnit.SECONDS);
		task = (BukkitTask) taskHandler.getTask(id);
	}

	public AfkPlayer(boolean afk, String lastAction) {
		this.lastAction = lastAction;
		this.afk = afk;
		time = Utils.getCurrentTimeInSeconds();
	}

	public void setAfk(boolean afk) {
		this.afk = afk;
	}

	public void setAfk(Player player) {
		afk = true;
		Prefix.DEFAULT_BAD.sendMessage(player, "Tu es désormais &4AFK&c.");
		NametagAPI api = (NametagAPI) OlympaCore.getInstance().getNameTagApi();
		if (api != null) {
			lastSuffix = api.getNametag(player).getSuffix();
			api.setSuffix(player.getName(), " §4[§cAFK§4]");
		}
	}

	public void setNotAfk(Player player) {
		afk = false;
		Prefix.DEFAULT_GOOD.sendMessage(player, "Tu n'es plus &2AFK&a.");
		launchTask(player);
		NametagAPI api = (NametagAPI) OlympaCore.getInstance().getNameTagApi();
		if (api != null)
			api.setSuffix(player.getName(), getLastSuffix());
	}

	private String getLastSuffix() {
		return lastSuffix != null ? lastSuffix : " ";
	}

	public boolean isAfk() {
		return afk;
	}

	public String getLastAction() {
		return lastAction;
	}

	public long getTime() {
		return time;
	}

	public void toggleAfk(Player player) {
		afk = !afk;
		if (afk)
			setAfk(player);
		else
			setNotAfk(player);
	}

}
