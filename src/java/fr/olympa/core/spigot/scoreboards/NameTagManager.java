package fr.olympa.core.spigot.scoreboards;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import fr.olympa.api.spigot.scoreboard.tab.FakeTeam;
import fr.olympa.api.spigot.scoreboard.tab.Nametag;
import fr.olympa.api.utils.CacheStats;
import fr.olympa.core.spigot.scoreboards.packets.PacketWrapper;

public class NameTagManager {

	public static boolean DISABLE_PUSH_ALL_TAGS = true;

	//	private final Map<String, Map<Player, FakeTeam>> playerTeams = new HashMap<>(); //TODO
	private final static Map<FakeTeam, Collection<? extends Player>> playerTeams = new ConcurrentHashMap<>();

	static {
		CacheStats.addDebugMap("nameTagTeams", playerTeams);
	}

	public static void removeTeam(FakeTeam team) {
		playerTeams.remove(team);
	}

	public static Set<FakeTeam> getTeamsOfPlayer(String playerName) {
		return playerTeams.keySet().stream().filter(team -> team.getMembers().contains(playerName)).collect(Collectors.toSet());
	}

	public static Set<FakeTeam> getTeamsOfViewer(Player player) {
		return playerTeams.entrySet().stream().filter(entry -> entry.getValue().contains(player)).map(Entry::getKey).collect(Collectors.toSet());
	}

	public static void deleteAllTeams() {
		playerTeams.forEach((fakeTeam, players) -> {
			PacketWrapper.delete(fakeTeam).send(players);
		});
		playerTeams.clear();
	}

	public void changeFakeNametag(String playerName, Nametag nameTag, int sortPriority, Collection<Player> toPlayers) {
		/*FakeTeam previous = getFakeTeam(player);
		String suffix = nameTag.getSuffix();
		String prefix = nameTag.getPrefix();
		if (previous == null || prefix == null && previous.getSuffix().equals(suffix) || suffix == null && previous.getPrefix().equals(prefix))
			return;
		if (prefix == null)
			prefix = previous.getPrefix();
		else if (suffix == null)
			suffix = previous.getSuffix();*/
		if (nameTag.isEmpty())
			return;
		//		new PacketWrapper(team.getName(), team.getPrefix(), team.getSuffix(), 0, team.getMembers()).send(toPlayers);
		Set<FakeTeam> allTeamsOfPlayer = getTeamsOfPlayer(playerName);
		String prefix = nameTag.getPrefix();
		String suffix = nameTag.getSuffix();
		/*String rawPrefix = nameTag.getPrefix();
		if (ChatColor.stripColor(rawPrefix).isBlank())
			prefix = rawPrefix;
		else
			prefix = rawPrefix + " ";
		suffix = nameTag.getSuffix().isBlank() ? "" : " " + nameTag.getSuffix();*/
		FakeTeam team = new FakeTeam(prefix, suffix, sortPriority);
		team.addMember(playerName);
		team.addViewers(toPlayers);
		PacketWrapper.create(team).send(toPlayers);
		playerTeams.put(team, toPlayers);
		for (FakeTeam t : allTeamsOfPlayer) {
			if (t.getId() == team.getId())
				continue;
			//			t.removeMember(playerName);
			PacketWrapper.delete(t).send(toPlayers);
			t.removeViewers(toPlayers);
			if (t.getViewers().isEmpty())
				playerTeams.remove(t);
		}

		//		List<FakeTeam> remove = new ArrayList<>();
		//		playerTeams.forEach((fakeTeam, toPlayersCache) -> {
		//		for (Entry<FakeTeam, Collection<? extends Player>> e : playerTeams.entrySet()) {
		//			FakeTeam fakeTeam = e.getKey();
		//			Collection<? extends Player> toPlayersCache = e.getValue();
		//			if (!fakeTeam.getMembers().contains(playerName))
		//				return;
		//			fakeTeam.getMembers().remove(playerName);
		//			if (fakeTeam.getMembers().isEmpty()) {
		//				PacketWrapper.delete(team).send(toPlayers);
		//				remove.add(fakeTeam);
		//				return;
		//			}
		//			List<? extends Player> removeToPlayers = toPlayersCache.stream().filter(p -> toPlayers.contains(p)).collect(Collectors.toList());
		//			if (removeToPlayers.size() != toPlayersCache.size())
		//				PacketWrapper.removeMember(team, Arrays.asList(playerName)).send(toPlayers);
		//			//				new PacketWrapper(team.getName(), 4, Arrays.asList(player)).send(toPlayers); // WARNING CAN CRASH -> need test
		//			else {
		//				PacketWrapper.delete(team).send(toPlayers);
		//				remove.add(fakeTeam);
		//			}
		//		}
		//		for (Entry<FakeTeam, Collection<? extends Player>> e : playerTeams.entrySet()) {
		//			FakeTeam fakeTeam = e.getKey();
		//			//			Collection<? extends Player> toPlayersCache = e.getValue();
		//			if (!fakeTeam.getMembers().contains(playerName))
		//				return;
		//			fakeTeam.getMembers().remove(playerName);
		//			if (fakeTeam.getMembers().isEmpty()) {
		//				PacketWrapper.delete(team).send(toPlayers);
		//				playerTeams.remove(fakeTeam);
		//			}
		//		}
		//		remove.forEach(fakeTeam -> );
	}

	/*private final HashMap<String, FakeTeam> TEAMS = new HashMap<>();
	private final HashMap<String, FakeTeam> CACHED_FAKE_TEAMS = new HashMap<>();

	private void addPlayerToTeam(String player, String prefix, String suffix, int sortPriority) {
		FakeTeam previous = getFakeTeam(player);
		if (previous != null && previous.isSimilar(prefix, suffix))
			return;
		reset(player);
		FakeTeam joining = getFakeTeam(prefix, suffix);
		if (joining != null)
			joining.addMember(player);
		else {
			joining = new FakeTeam(prefix, suffix, sortPriority);
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

	private FakeTeam decache(String player) {
		return CACHED_FAKE_TEAMS.remove(player);
	}

	public FakeTeam getFakeTeam(String player) {
		return CACHED_FAKE_TEAMS.get(player);
	}

	private FakeTeam getFakeTeam(String prefix, String suffix) {
		for (FakeTeam fakeTeam : TEAMS.values())
			if (fakeTeam.isSimilar(prefix, suffix))
				return fakeTeam;
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
			if (removing != null)
				delete = removePlayerFromTeamPackets(fakeTeam, removing.getName());
			else {
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
		for (FakeTeam fakeTeam : TEAMS.values())
			new PacketWrapper(fakeTeam.getName(), fakeTeam.getPrefix(), fakeTeam.getSuffix(), 0, fakeTeam.getMembers()).send(player);
	}

	public void setNametag(String player, String prefix, String suffix) {
		setNametag(player, prefix, suffix, -1);
	}

	public void setNametag(String player, String prefix, String suffix, int sortPriority) {
		addPlayerToTeam(player, prefix != null ? prefix : "", suffix != null ? suffix : "", sortPriority);
	}*/
}