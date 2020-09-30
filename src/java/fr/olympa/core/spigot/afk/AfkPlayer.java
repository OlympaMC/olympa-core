package fr.olympa.core.spigot.afk;

import org.bukkit.entity.Player;

import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.scoreboards.api.NametagAPI;

public class AfkPlayer {

	boolean afk;
	String lastAction;
	String lastSuffix;
	long time;

	public AfkPlayer() {
		afk = false;
	}

	public AfkPlayer(boolean afk, String lastAction) {
		update(afk, lastAction);
	}

	protected void update(boolean afk, String lastAction) {
		this.lastAction = lastAction;
		this.afk = afk;
		time = Utils.getCurrentTimeInSeconds();
	}

	public void setAfk(Player player) {
		afk = true;
		Prefix.DEFAULT_BAD.sendMessage(player, "Tu es désormais &4AFK&c.");
		NametagAPI api = (NametagAPI) OlympaCore.getInstance().getNameTagApi();
		lastSuffix = api.getNametag(player).getSuffix();
		if (api != null)
			api.setSuffix(player.getName(), " §4[§cAFK§4]");
	}

	public void setNotAfk(Player player) {
		afk = false;
		Prefix.DEFAULT_GOOD.sendMessage(player, "Tu n'es plus &2AFK&a.");
		NametagAPI api = (NametagAPI) OlympaCore.getInstance().getNameTagApi();
		if (api != null)
			api.setSuffix(player.getName(), getLastSuffix());
	}

	private String getLastSuffix() {
		return lastSuffix != null ? lastSuffix : new String();
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
