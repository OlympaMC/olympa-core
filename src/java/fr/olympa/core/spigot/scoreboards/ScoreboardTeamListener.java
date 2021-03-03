package fr.olympa.core.spigot.scoreboards;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent;
import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.customevents.PlayerSexChangeEvent;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.scoreboard.tab.FakeTeam;
import fr.olympa.api.scoreboard.tab.INametagApi;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.module.CoreModules;
import fr.olympa.core.spigot.scoreboards.packets.PacketWrapper;

public class ScoreboardTeamListener implements Listener {

	// HandlerList.unregisterAll(new ScoreboardTeamListener()) pour désactiver le
	// format de base

	@EventHandler(priority = EventPriority.LOWEST)
	public void on0PlayerConnect(PlayerJoinEvent event) {
		INametagApi nameTagApi = CoreModules.NAME_TAG.getApi();
		if (nameTagApi == null)
			return;
		
		Player player = event.getPlayer();
		nameTagApi.callNametagUpdate(AccountProvider.get(player.getUniqueId()), false);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void on0PlayerLoad(OlympaPlayerLoadEvent event) {
		INametagApi nameTagApi = CoreModules.NAME_TAG.getApi();
		if (nameTagApi == null)
			return;
		
		Player player = event.getPlayer();
		OlympaPlayer olympaPlayer = AccountProvider.get(player.getUniqueId());
		nameTagApi.callNametagUpdate(AccountProvider.get(player.getUniqueId()));
		List<OlympaPlayer> self = Arrays.asList(olympaPlayer);
		for (OlympaPlayer other : AccountProvider.getAll())
			if (other != olympaPlayer && other.getPlayer() != null && other.getPlayer().isOnline())
				nameTagApi.callNametagUpdate(other, self, true);
	}

	@EventHandler
	public void on1OlympaPlayerChangeGroup(AsyncOlympaPlayerChangeGroupEvent event) {
		Player player = event.getPlayer();
		INametagApi nameTagApi = CoreModules.NAME_TAG.getApi();
		if (player != null && player.isOnline() && nameTagApi != null)
			nameTagApi.callNametagUpdate(event.getOlympaPlayer());
	}

	@EventHandler
	public void on2PlayerSexChange(PlayerSexChangeEvent event) {
		Player player = event.getPlayer();
		OlympaPlayer olympaPlayer = AccountProvider.get(player.getUniqueId());
		INametagApi nameTagApi = OlympaCore.getInstance().getNameTagApi();
		if (nameTagApi != null)
			nameTagApi.callNametagUpdate(olympaPlayer);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		String playerName = player.getName();
		Set<FakeTeam> teams = NameTagManager.getTeamsOfPlayer(playerName);
		for (FakeTeam t : teams) {
			//			t.removeMember(playerName);
			PacketWrapper.delete(t).send(t.getViewers());
			//			t.removeViewers(t.getViewers());
			//			if (t.getViewers().isEmpty())
			NameTagManager.removeTeam(t);
		}
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
