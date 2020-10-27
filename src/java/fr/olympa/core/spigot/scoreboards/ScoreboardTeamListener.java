package fr.olympa.core.spigot.scoreboards;

import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent;
import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.customevents.PlayerSexChangeEvent;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.scoreboard.tab.INametagApi;
import fr.olympa.core.spigot.OlympaCore;

public class ScoreboardTeamListener implements Listener {

	// HandlerList.unregisterAll(new ScoreboardTeamListener()) pour désactiver le
	// format de base

	@EventHandler(priority = EventPriority.MONITOR)
	public void on0PlayerLoad(OlympaPlayerLoadEvent event) {
		Player player = event.getPlayer();
		OlympaPlayer olympaPlayer = AccountProvider.get(player.getUniqueId());
		//OlympaCore.getInstance().getServer().getPluginManager().callEvent(new PlayerNameTagEditEvent(player, olympaPlayer, null, null));
		INametagApi nameTagApi = OlympaCore.getInstance().getNameTagApi();
		nameTagApi.callNametagUpdate(olympaPlayer);
		List<OlympaPlayer> self = Arrays.asList(olympaPlayer);
		for (OlympaPlayer other : AccountProvider.getAll()) {
			if (other != olympaPlayer && other.isConnected()) nameTagApi.callNametagUpdate(other, self);
		}
	}

	@EventHandler
	public void on1OlympaPlayerChangeGroup(AsyncOlympaPlayerChangeGroupEvent event) {
		Player player = event.getPlayer();
		if (player != null && player.isOnline()) {
			//OlympaCore.getInstance().getServer().getPluginManager().callEvent(new PlayerNameTagEditEvent(player, event.getOlympaPlayer(), null, null));
			OlympaCore.getInstance().getNameTagApi().callNametagUpdate(event.getOlympaPlayer());
		}
	}

	@EventHandler
	public void on2PlayerSexChange(PlayerSexChangeEvent event) {
		Player player = event.getPlayer();
		OlympaPlayer olympaPlayer = AccountProvider.get(player.getUniqueId());
		OlympaCore.getInstance().getNameTagApi().callNametagUpdate(olympaPlayer);
	}

	/*@EventHandler(priority = EventPriority.LOWEST)
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
	
		FakeTeam team = nameTagApi.getFakeTeam(player);
		if (team == null || event.isForceCreateTeam())
			nameTagApi.setNametag(player.getName(), nameTag.getPrefix(), nameTag.getSuffix(), event.getSortPriority());
		else if (team.getMembers().size() == 1)
			nameTagApi.updateFakeNameTag(player.getName(), nameTag, event.getTargets());
		else
			nameTagApi.setNametag(player.getName(), nameTag.getPrefix(), nameTag.getSuffix(), event.getSortPriority());
	}*/

}
