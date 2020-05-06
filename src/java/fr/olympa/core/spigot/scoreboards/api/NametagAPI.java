package fr.olympa.core.spigot.scoreboards.api;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.scoreboard.tab.FakeTeam;
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

	public NametagAPI(NametagManager manager) {
		this.manager = manager;
	}

	@Override
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
	public void setPrefix(String player, String prefix) {
		FakeTeam fakeTeam = manager.getFakeTeam(player);
		manager.setNametag(player, prefix, fakeTeam == null ? null : fakeTeam.getSuffix());
	}

	@Override
	public void setSuffix(String player, String suffix) {
		FakeTeam fakeTeam = manager.getFakeTeam(player);
		manager.setNametag(player, fakeTeam == null ? null : fakeTeam.getPrefix(), suffix);
	}

	public boolean testCompat() {
		PacketWrapper wrapper = new PacketWrapper("TEST", "&f", "", 0, new ArrayList<>());
		wrapper.send();
		if (wrapper.error == null) {
			return true;
		}
		Bukkit.getLogger().severe(new StringBuilder()
				.append("\n------------------------------------------------------\n")
				.append("[WARNING] ScoreboardTeam").append(" Failed to load! [WARNING]")
				.append("\n------------------------------------------------------")
				.append("\nThis might be an issue with reflection. REPORT this:\n> ")
				.append(wrapper.error)
				.append("\n\n------------------------------------------------------")
				.toString());
		return false;
	}

	@Override
	public void updateFakeNameTag(Player player, Nametag nametag, Collection<? extends Player> toPlayers) {
		updateFakeNameTag(player.getName(), nametag, toPlayers);
	}

	@Override
	public void updateFakeNameTag(String player, Nametag nametag, Collection<? extends Player> toPlayers) {
		manager.changeFakeNametag(player, nametag, toPlayers);
	}

}