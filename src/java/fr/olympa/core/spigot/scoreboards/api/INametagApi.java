package fr.olympa.core.spigot.scoreboards.api;

import java.util.List;

import org.bukkit.entity.Player;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.core.spigot.scoreboards.api.data.FakeTeam;
import fr.olympa.core.spigot.scoreboards.api.data.Nametag;

/**
 *
 */
public interface INametagApi {

	/**
	 * Removes a player's nametag in memory only.
	 * <p>
	 * Note: Only affects memory, does NOT add/remove from storage.
	 *
	 * @param player whose nametag to clear
	 */
	void clearNametag(Player player);

	/**
	 * Removes a player's nametag in memory only.
	 * <p>
	 * Note: Only affects memory, does NOT add/remove from storage.
	 *
	 * @param player whose nametag to clear
	 */
	void clearNametag(String player);

	/**
	 * Function gets the fake team data for player.
	 *
	 * @param player the player to check
	 * @return the fake team
	 */
	FakeTeam getFakeTeam(Player player);

	/**
	 * Function gets the nametag for a player if it exists. This will never return a
	 * null.
	 *
	 * @param player the player to check
	 * @return the nametag for the player
	 */
	Nametag getNametag(Player player);

	void reset();

	void reset(String player);

	void sendTeams(Player player);

	void setNametag(OlympaPlayer olympaPlayer);

	/**
	 * Sets the nametag for a player.
	 * <p>
	 * Note: Only affects memory, does NOT add/remove from storage.
	 *
	 * @param player the player whose nametag to change
	 * @param prefix the prefix to change to
	 * @param suffix the suffix to change to
	 */
	void setNametag(String player, String prefix, String suffix);

	/**
	 * Sets the prefix for a player. The previous suffix is kept if it exists.
	 * <p>
	 * Note: Only affects memory, does NOT add/remove from storage.
	 *
	 * @param player the player whose nametag to change
	 * @param prefix the prefix to change to
	 */
	void setPrefix(String player, String prefix);

	/**
	 * Sets the suffix for a player. The previous prefix is kept if it exists.
	 * <p>
	 * Note: Only affects memory, does NOT add/remove from storage.
	 *
	 * @param player the player whose nametag to change
	 * @param suffix the suffix to change to
	 */
	void setSuffix(String player, String suffix);

	void updateNametag(Player player, String suffix, List<Player> toPlayers);
}