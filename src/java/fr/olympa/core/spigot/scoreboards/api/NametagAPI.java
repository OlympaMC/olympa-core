package fr.olympa.core.spigot.scoreboards.api;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.scoreboard.tab.INametagApi;
import fr.olympa.api.scoreboard.tab.Nametag;
import fr.olympa.core.spigot.scoreboards.NametagManager;
import fr.olympa.core.spigot.scoreboards.packets.PacketWrapper;

/**
 * Implements the INametagAPI interface. There only exists one instance of this
 * class.
 */
public final class NametagAPI implements INametagApi {

	private NametagManager manager;
	
	private List<Entry<EventPriority, NametagHandler>> handlers = new ArrayList<>();
	
	public NametagAPI(NametagManager manager) {
		this.manager = manager;
	}
	
	@Override
	public void addNametagHandler(EventPriority priority, NametagHandler handler) {
		handlers.add(new AbstractMap.SimpleEntry<>(priority, handler));
		handlers.sort((o1, o2) -> Integer.compare(o1.getKey().getSlot(), o2.getKey().getSlot()));
	}
	
	@Override
	public void callNametagUpdate(OlympaPlayer player) {
		callNametagUpdate(player, Bukkit.getOnlinePlayers().stream().map(onlinePlayer -> AccountProvider.<OlympaPlayer>get(onlinePlayer.getUniqueId())).collect(Collectors.toList()));
	}
	
	@Override
	public void callNametagUpdate(OlympaPlayer player, Collection<? extends OlympaPlayer> toPlayers) {
		if (!player.isConnected()) return;
		Map<Nametag, List<Player>> nametags = new HashMap<>();
		for (OlympaPlayer to : toPlayers) {
			if (!to.isConnected()) continue;
			Nametag tag = new Nametag();
			for (Entry<EventPriority, NametagHandler> handlerEntry : handlers) {
				NametagHandler handler = handlerEntry.getValue();
				handler.updateNameTag(tag, player, to);
			}
			List<Player> similarTags = nametags.get(tag);
			if (similarTags == null) {
				similarTags = new ArrayList<>();
				nametags.put(tag, similarTags);
			}
			similarTags.add(to.getPlayer());
		}
		nametags.forEach((tag, players) -> updateFakeNameTag(player.getPlayer(), tag, players));
	}
	
	@Override
	public void reset() {
		manager.reset();
	}
	
	@Override
	public void reset(String player) {
		manager.reset(player);
	}
	
	@Override
	public void sendTeams(Player player) {
		manager.sendTeams(player);
	}

	/*@Override
	public void clearNametag(Player player) {
		manager.reset(player.getName());
	}
	
	@Override
	public void clearNametag(String player) {
		manager.reset(player);
	}
	
	@Override
	public FakeTeam getFakeTeam(Player player) {
		return manager.getFakeTeam(player.getName());
	}
	
	@Override
	public Nametag getNametag(Player player) {
		FakeTeam team = manager.getFakeTeam(player.getName());
		boolean nullTeam = team == null;
		return new Nametag(nullTeam ? "" : team.getPrefix(), nullTeam ? "" : team.getSuffix());
	}
	
	@Override
	public void setNametag(OlympaPlayer olympaPlayer) {
		setNametag(olympaPlayer, null);
	}
	
	public void setNametag(OlympaPlayer olympaPlayer, String suffix) {
		manager.setNametag(olympaPlayer.getName(), olympaPlayer.getGroupPrefix(), suffix, olympaPlayer.getGroup().getIndex());
	}
	
	@Override
	public void setNametag(String player, String prefix, String suffix) {
		manager.setNametag(player, prefix, suffix);
	}
	
	@Override
	public void setNametag(String player, String prefix, String suffix, int sortPriority) {
		manager.setNametag(player, prefix, suffix, sortPriority);
	}
	
	@Override
	public void setPrefix(String player, String prefix) {
		FakeTeam fakeTeam = manager.getFakeTeam(player);
		manager.setNametag(player, prefix, fakeTeam == null ? null : fakeTeam.getSuffix());
	}
	
	@Override
	public void addPrefix(String player, String prefix) {
		FakeTeam fakeTeam = manager.getFakeTeam(player);
		manager.setNametag(player, fakeTeam == null ? null : fakeTeam.getPrefix() + prefix, fakeTeam == null ? null : fakeTeam.getSuffix());
	}
	
	@Override
	public void setSuffix(String player, String suffix) {
		FakeTeam fakeTeam = manager.getFakeTeam(player);
		manager.setNametag(player, fakeTeam == null ? null : fakeTeam.getPrefix(), suffix);
	}
	
	@Override
	public void addSuffix(String player, String suffix) {
		FakeTeam fakeTeam = manager.getFakeTeam(player);
		manager.setNametag(player, fakeTeam == null ? null : fakeTeam.getPrefix(), (fakeTeam == null ? null : fakeTeam.getSuffix()) + suffix);
	}
	
	@Override
	public void updateFakeNameTag(String player, Nametag nameTag) {
		manager.changeFakeNametag(player, nameTag, null);
	}*/
	
	//@Override
	public void updateFakeNameTag(Player player, Nametag nameTag, Collection<? extends Player> toPlayers) {
		updateFakeNameTag(player.getName(), nameTag, toPlayers);
	}
	
	//@Override
	public void updateFakeNameTag(String player, Nametag nameTag, Collection<? extends Player> toPlayers) {
		manager.changeFakeNametag(player, nameTag, toPlayers);
	}
	
	public boolean testCompat() {
		PacketWrapper wrapper = new PacketWrapper("TEST", "&f", "", 0, new ArrayList<>());
		wrapper.send();
		if (wrapper.error == null)
			return true;
		Bukkit.getLogger().severe(new StringBuilder()
				.append("\n------------------------------------------------------\n")
				.append("[WARNING] ScoreboardTeam").append(" Failed to load! [WARNING]")
				.append("\n------------------------------------------------------")
				.append("\nThis might be an issue with reflection:\n> ")
				.append(wrapper.error)
				.append("\n\n------------------------------------------------------")
				.toString());
		return false;
	}

}