package fr.olympa.core.spigot.scoreboards;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent;
import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.customevents.PlayerNameTagEditEvent;
import fr.olympa.api.customevents.PlayerSexChangeEvent;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.scoreboard.tab.Nametag;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.scoreboards.api.NametagAPI;

public class ScoreboardTeamListener implements Listener {

	// HandlerList.unregisterAll(new ScoreboardTeamListener()) pour désactiver le
	// format de base

	@EventHandler(priority = EventPriority.MONITOR)
	public void on0PlayerLoad(OlympaPlayerLoadEvent event) {
		Player player = event.getPlayer();
		OlympaPlayer olympaPlayer = AccountProvider.get(player.getUniqueId());
		OlympaCore.getInstance().getServer().getPluginManager().callEvent(new PlayerNameTagEditEvent(player, olympaPlayer, null, null));
	}

	@EventHandler
	public void on1OlympaPlayerChangeGroup(AsyncOlympaPlayerChangeGroupEvent event) {
		Player player = event.getPlayer();
		OlympaCore.getInstance().getServer().getPluginManager().callEvent(new PlayerNameTagEditEvent(player, event.getOlympaPlayer(), null, null));
	}

	@EventHandler
	public void on2PlayerSexChange(PlayerSexChangeEvent event) {
		Player player = event.getPlayer();
		OlympaPlayer olympaPlayer = AccountProvider.get(player.getUniqueId());
		OlympaCore.getInstance().getServer().getPluginManager().callEvent(new PlayerNameTagEditEvent(player, olympaPlayer, null, null));
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void on3PlayerNameTagEdit(PlayerNameTagEditEvent event) {
		Player player = event.getPlayer();
		OlympaPlayer olympaPlayer = AccountProvider.get(player.getUniqueId());
		Nametag nameTag = event.getNameTag();
		nameTag.appendPrefix(olympaPlayer.getGroupPrefix());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void on4PlayerNameTagEdit(PlayerNameTagEditEvent event) {
		Player player = event.getPlayer();
		NametagAPI nameTagApi = (NametagAPI) OlympaCore.getInstance().getNameTagApi();
		Nametag nameTag = event.getNameTag();
		if (event.isCancelled())
			return;
		if (nameTagApi.getFakeTeam(player) == null || event.isForceCreateTeam())
			nameTagApi.setNametag(player.getName(), nameTag.getPrefix(), nameTag.getSuffix(), event.getSortPriority());
		else
			nameTagApi.updateFakeNameTag(player.getName(), nameTag, event.getTargets());
	}

}
