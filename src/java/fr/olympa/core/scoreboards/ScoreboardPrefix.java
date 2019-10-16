package fr.olympa.core.scoreboards;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;

import fr.olympa.api.objects.OlympaGroup;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.utils.Utils;

public class ScoreboardPrefix {

	private static Set<FakeTeam> teams = new HashSet<>();

	public static void create(OlympaPlayer olympaPlayer) {
		OlympaGroup group = olympaPlayer.getGroup();
		String prefix = group.getPrefix();
		String suffix = "";
		String entry = olympaPlayer.getName();
		String teamName = Utils.getLetterOfNumber(group.getId());

		String playerName = entry.toLowerCase();
		while (playerName.length() + teamName.length() >= 15) {
			playerName = playerName.substring(0, playerName.length() - 2);
		}
		teamName += playerName;

		try {
			if (prefix != null && prefix.length() > 16) {
				throw new Exception("Prefix lenghts are greater than 16 (" + prefix.length() + ")");
			}
			if (teamName != null && teamName.length() > 16) {
				throw new Exception("Name lenghts are greater than 16 (" + teamName.length() + ")");
			}
			if (suffix != null && suffix.length() > 16) {
				throw new Exception("Suffix lenghts are greater than 16 (" + suffix.length() + ")");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		FakeTeam fakeTeam = getTeamOfPlayer(entry);

		if (fakeTeam != null && !fakeTeam.getName().equals(teamName)) {
			deleteTeam(fakeTeam);
			fakeTeam = null;
		}

		if (fakeTeam == null) {
			fakeTeam = new FakeTeam(teamName, suffix, "", prefix, new HashSet<>(Arrays.asList(entry)));
			fakeTeam.create();
			teams.add(fakeTeam);
		} else {
			FakeTeam fakeTeam2 = fakeTeam.clone();
			fakeTeam.setPrefix(prefix);
			fakeTeam.setSuffix(prefix);
			if (!fakeTeam2.equals(fakeTeam)) {
				fakeTeam.update();
			}
		}
	}

	public static void deleteTeam(FakeTeam fakeTeam) {
		fakeTeam.delete();
		teams.remove(fakeTeam);
	}

	public static void deleteTeams() {
		teams.stream().forEach(fakeTeam -> fakeTeam.delete());
		teams.clear();
	}

	public static Set<FakeTeam> getRegisterTeams() {
		return Collections.unmodifiableSet(teams);
	}

	public static FakeTeam getTeamOfPlayer(Player player) {
		return getTeamOfPlayer(player.getName());
	}

	public static FakeTeam getTeamOfPlayer(String name) {
		return teams.stream().filter(fakeTeam -> fakeTeam.hasPlayer(name)).findFirst().orElse(null);
	}

	public static void removeFromTeam(Player player) {
		removeFromTeam(player.getName());
	}

	public static void removeFromTeam(String name) {
		FakeTeam fakeTeam = getTeamOfPlayer(name);
		if (fakeTeam != null) {

			if (fakeTeam.getPlayers().size() == 1) {
				deleteTeam(fakeTeam);
				return;
			}
			fakeTeam.removePlayer(name);
		}
	}

	public static void removeFromView(Player player) {
		teams.forEach(fakeTeam -> fakeTeam.remove(player));
	}

	public static void sendCurrentTeams(Player player) {
		for (FakeTeam fakeTeam : teams) {
			fakeTeam.addViewer(player);
		}
	}
}