package fr.tristiisch.olympa.core.permission.scoreboard;

import java.util.Collection;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import fr.tristiisch.olympa.api.objects.OlympaGroup;
import fr.tristiisch.olympa.api.utils.SpigotUtils;
import fr.tristiisch.olympa.core.permission.groups.FakeTeam;

public class ScoreboardList {

	public static Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

	public static void deleteTeam(Player player) {
		FakeTeam team = FakeTeam.getRegisterTeams().stream().filter(team2 -> team2.getName().replaceFirst("(^\\d)", "").startsWith(player.getName().toLowerCase())).findFirst().orElse(null);
		if (team != null) {
			team.delete();
		}
	}

	private Team team;

	public ScoreboardList(Player player, OlympaGroup group) {
		String prefix = group.getPrefix();
		String suffix = "";
		String entry = player.getName();
		String teamName = String.valueOf(group.getId());

		String playerName = player.getName().toLowerCase();
		while (playerName.length() + teamName.length() >= 16) {
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

		/**FakeTeam fakeTeam = new FakeTeam(teamName, suffix, teamName, prefix);
		fakeTeam.create();
		fakeTeam.addPlayer(player);**/

		this.team = scoreboard.getTeam(teamName);
		// this.team = FakeTeam.getRegisterTeams().stream().filter(t
		// ->t.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
		if (this.team == null) {
			// this.team = new FakeTeam(name); this.team.create();
			this.team = scoreboard.registerNewTeam(teamName);
		}
		// this.team.setCanSeeFriendlyInvisibles(true);

		this.team.setPrefix(SpigotUtils.color(prefix));
		this.team.addEntry(entry);

		// this.team.setDisplayName(SpigotUtils.color(suffix));
		// this.team.setSuffix(entry);
	}

	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	public void remove(Player player) {
		this.team.removeEntry(player.getName());
	}

	public void removeTeam() {
		/* this.team.delete(); */
		this.team.unregister();
	}

	public void resetTagUtils(UUID uuid) {
		this.remove(Bukkit.getPlayer(uuid));
	}

	public void set(Player player) {
		player.setScoreboard(scoreboard);
	}

	public void setAll(Collection<Player> players) {
		for (Player player : players) {
			this.set(player);
		}
	}

	public void setAll(Player[] players) {
		Player[] arrayOfPlayer;
		int j = (arrayOfPlayer = players).length;
		for (int i = 0; i < j; i++) {
			Player player = arrayOfPlayer[i];
			this.set(player);
		}
	}
}