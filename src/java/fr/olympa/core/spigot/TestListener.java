package fr.olympa.core.spigot;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.destroystokyo.paper.event.profile.PreFillProfileEvent;
import com.destroystokyo.paper.event.profile.ProfileWhitelistVerifyEvent;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import com.destroystokyo.paper.event.server.ServerExceptionEvent;

public class TestListener implements Listener {

	@EventHandler
	public void onPreLogin(AsyncPlayerPreLoginEvent event) {
		String eventName = event.getEventName();
		OlympaCore.getInstance().sendMessage("EVENT : " + eventName + " §6(" + event.getName() + ")");
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		String eventName = event.getEventName();
		OlympaCore.getInstance().sendMessage("EVENT : " + eventName + " §6(" + event.getPlayer().getName() + ")");
	}

	// Use maybe for crack skin
	@EventHandler
	public void onAsyncTabComplete(PreFillProfileEvent event) {

	}

	// TODO use for maintenance
	@EventHandler
	public void onAsyncTabComplete(ProfileWhitelistVerifyEvent event) {
	}

	// TODO use for error
	@EventHandler
	public void onAsyncTabComplete(ServerExceptionEvent event) {
		System.out.println(event.getEventName() + " ok");
		//				com.destroystokyo.paper.event.server.GS4QueryEvent.
	}

	// TODO use for command
	@EventHandler
	public void onAsyncTabComplete(AsyncTabCompleteEvent event) {
	}

	@EventHandler
	public void onPlayerStartSpectatingEntity(PlayerStartSpectatingEntityEvent event) {
		event.getPlayer().sendMessage("tu spec " + event.getNewSpectatorTarget().getName());
		event.setCancelled(false);
	}

	// TODO use to remove commands in /TabComplete
	@EventHandler
	public void onPlayerCommandSend(PlayerCommandSendEvent event) {
		System.out.println(event.getEventName() + " ok");
		List<String> blockedCommands = new ArrayList<>();
		//		blockedCommands.add("gamemode");
		//		blockedCommands.add("ban");
		//		blockedCommands.add("help");
		event.getCommands().removeAll(blockedCommands);
	}
}
