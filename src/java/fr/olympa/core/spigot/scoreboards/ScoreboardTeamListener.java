package fr.olympa.core.spigot.scoreboards;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import fr.olympa.api.groups.AsyncOlympaPlayerChangeGroupEvent;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.scoreboards.api.INametagApi;

public class ScoreboardTeamListener implements Listener {

	// HandlerList.unregisterAll(new ScoreboardTeamListener()) pour désactiver le
	// format de base

	@EventHandler
	public void on0PlayerLogin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		OlympaPlayer olympaPlayer = AccountProvider.get(player.getUniqueId());
		INametagApi nameTagApi = OlympaCore.getInstance().getNameTagApi();
		if (nameTagApi.getFakeTeam(player) == null) {
			nameTagApi.setNametag(olympaPlayer);
		}
		nameTagApi.updateNametag(player, " §4Gros Bg", Arrays.asList(player));
	}

	@EventHandler
	public void on1OlympaPlayerChangeGroup(AsyncOlympaPlayerChangeGroupEvent event) {
		Player player = event.getPlayer();
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		INametagApi nameTagApi = OlympaCore.getInstance().getNameTagApi();
		nameTagApi.setNametag(olympaPlayer);
		nameTagApi.updateNametag(player, " §4Gros Bg", Arrays.asList(player));
	}
}
