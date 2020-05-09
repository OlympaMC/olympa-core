package fr.olympa.core.spigot.scoreboards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import fr.olympa.api.scoreboard.tab.FakeTeam;
import fr.olympa.api.scoreboard.tab.Nametag;
import fr.olympa.core.spigot.scoreboards.packets.PacketWrapper;

@SuppressWarnings("deprecation")
public class NametagManager {

	public static boolean DISABLE_PUSH_ALL_TAGS = true;

	private final HashMap<String, FakeTeam> TEAMS = new HashMap<>();
	private final HashMap<String, FakeTeam> CACHED_FAKE_TEAMS = new HashMap<>();

	/**
	 * Adds a player to a FakeTeam. If they are already on this team, we do NOT
	 * change that.
	 */
	private void addPlayerToTeam(String player, String prefix, String suffix, int sortPriority, boolean playerTag) {
		FakeTeam previous = getFakeTeam(player);
		if (previous != null && previous.isSimilar(prefix, suffix)) {
			return;
		}
		reset(player);
		FakeTeam joining = getFakeTeam(prefix, suffix);
		if (joining != null) {
			joining.addMember(player);
		} else {
			joining = new FakeTeam(prefix, suffix, sortPriority, playerTag);
			joining.addMember(player);
			TEAMS.put(joining.getName(), joining);
			addTeamPackets(joining);
		}
		Player adding = Bukkit.getPlayerExact(player);
		if (adding != null) {
			addPlayerToTeamPackets(joining, adding.getName());
			cache(adding.getName(), joining);
		} else {
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
			addPlayerToTeamPackets(joining, offlinePlayer.getName());
			cache(offlinePlayer.getName(), joining);
		}
	}

	private void addPlayerToTeamPackets(FakeTeam fakeTeam, String player) {
		new PacketWrapper(fakeTeam.getName(), 3, Collections.singletonList(player)).send();
	}

	private void addTeamPackets(FakeTeam fakeTeam) {
		new PacketWrapper(fakeTeam.getName(), fakeTeam.getPrefix(), fakeTeam.getSuffix(), 0, fakeTeam.getMembers()).send();
	}

	private void cache(String player, FakeTeam fakeTeam) {
		CACHED_FAKE_TEAMS.put(player, fakeTeam);
	}

	public void changeFakeNametag(String player, Nametag nameTag, Collection<? extends Player> toPlayers) {
		FakeTeam previous = getFakeTeam(player);
		String suffix = nameTag.getSuffix();
		String prefix = nameTag.getPrefix();
		if (previous == null || prefix == null && previous.getSuffix().equals(suffix) || suffix == null && previous.getPrefix().equals(prefix)) {
			return;
		}
		if (prefix == null) {
			prefix = previous.getPrefix();
		} else if (suffix == null) {
			suffix = previous.getSuffix();
		}
		new PacketWrapper(previous.getName(), prefix, suffix, 2, new ArrayList<>()).send(toPlayers);
	}

	private FakeTeam decache(String player) {
		return CACHED_FAKE_TEAMS.remove(player);
	}

	public FakeTeam getFakeTeam(String player) {
		return CACHED_FAKE_TEAMS.get(player);
	}

	/**
	 * Gets the current team given a prefix and suffix If there is no team similar
	 * to this, then a new team is created.
	 */
	private FakeTeam getFakeTeam(String prefix, String suffix) {
		for (FakeTeam fakeTeam : TEAMS.values()) {
			if (fakeTeam.isSimilar(prefix, suffix)) {
				return fakeTeam;
			}
		}
		return null;
	}

	private boolean removePlayerFromTeamPackets(FakeTeam fakeTeam, List<String> players) {
		new PacketWrapper(fakeTeam.getName(), 4, players).send();
		fakeTeam.getMembers().removeAll(players);
		return fakeTeam.getMembers().isEmpty();
	}

	private boolean removePlayerFromTeamPackets(FakeTeam fakeTeam, String... players) {
		return removePlayerFromTeamPackets(fakeTeam, Arrays.asList(players));
	}

	private void removeTeamPackets(FakeTeam fakeTeam) {
		new PacketWrapper(fakeTeam.getName(), fakeTeam.getPrefix(), fakeTeam.getSuffix(), 1, new ArrayList<>()).send();
	}

	public void reset() {
		for (FakeTeam fakeTeam : TEAMS.values()) {
			removePlayerFromTeamPackets(fakeTeam, fakeTeam.getMembers());
			removeTeamPackets(fakeTeam);
		}
		CACHED_FAKE_TEAMS.clear();
		TEAMS.clear();
	}

	public FakeTeam reset(String player) {
		return reset(player, decache(player));
	}

	private FakeTeam reset(String player, FakeTeam fakeTeam) {
		if (fakeTeam != null && fakeTeam.getMembers().remove(player)) {
			boolean delete;
			Player removing = Bukkit.getPlayerExact(player);
			if (removing != null) {
				delete = removePlayerFromTeamPackets(fakeTeam, removing.getName());
			} else {
				OfflinePlayer toRemoveOffline = Bukkit.getOfflinePlayer(player);
				delete = removePlayerFromTeamPackets(fakeTeam, toRemoveOffline.getName());
			}

			if (delete) {
				removeTeamPackets(fakeTeam);
				TEAMS.remove(fakeTeam.getName());
			}
		}

		return fakeTeam;
	}

	public void sendTeams(Player player) {
		for (FakeTeam fakeTeam : TEAMS.values()) {
			new PacketWrapper(fakeTeam.getName(), fakeTeam.getPrefix(), fakeTeam.getSuffix(), 0, fakeTeam.getMembers()).send(player);
		}
	}

	// ==============================================================
	// Below are public methods to modify certain data
	// ==============================================================
	public void setNametag(String player, String prefix, String suffix) {
		setNametag(player, prefix, suffix, -1);
	}

	public void setNametag(String player, String prefix, String suffix, int sortPriority) {
		setNametag(player, prefix, suffix, sortPriority, true);
	}

	public void setNametag(String player, String prefix, String suffix, int sortPriority, boolean playerTag) {
		addPlayerToTeam(player, prefix != null ? prefix : "", suffix != null ? suffix : "", sortPriority, playerTag);
	}
}