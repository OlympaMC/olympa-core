package fr.tristiisch.olympa.api.utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import fr.tristiisch.olympa.api.objects.OlympaPlayer;
import fr.tristiisch.olympa.core.datamanagment.redis.access.OlympaAccountProvider;

public class SpigotUtils {
	public static Location addYToLocation(final Location location, final float y) {
		return new Location(location.getWorld(), location.getX(), location.getY() + 1, location.getZ(), location.getYaw(), location.getPitch());
	}

	public static int clearPlayer(final Player player) {
		final PlayerInventory inventory = player.getInventory();
		final int size = (int) (Arrays.stream(inventory.getContents()).filter(item -> item != null).count() + Arrays.stream(inventory.getArmorContents()).filter(item -> item != null).count());
		inventory.clear();
		inventory.setArmorContents(new ItemStack[inventory.getArmorContents().length]);
		return size;
	}

	public static List<String> color(final List<String> l) {
		return l.stream().map(s -> SpigotUtils.color(s)).collect(Collectors.toList());
	}

	public static String color(final String s) {
		return s != null ? ChatColor.translateAlternateColorCodes('&', s) : "";
	}

	public static String connectScreen(final String s) {
		return color("\n&e&m-------------------------------------------\n\n&e[&6Olympa&e]\n\n" + s + "\n\n&e&m-------------------------------------------");
	}

	public static List<Location> getBlockAround(final Location location, final int raduis) {
		final List<Location> locations = new ArrayList<>();
		for (int x = raduis; x >= -raduis; x--) {
			for (int y = raduis; y >= -raduis; y--) {
				for (int z = raduis; z >= -raduis; z--) {
					locations.add(location.getBlock().getRelative(x, y, z).getLocation());
				}
			}
		}
		return locations;
	}

	public static Location getFirstBlockUnderPlayer(final Player player) {
		Location location = player.getLocation();
		do {
			if (location.getBlockY() == 0) {
				return null;
			}
			location = new Location(location.getWorld(), location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());
		} while (!location.getBlock().getType().isOccluding());
		return location;
	}

	public static ChatColor getIntervalChatColor(final int i, final int min, final int max) {
		if (i == 0) {
			return ChatColor.GRAY;
		}
		if (i < min) {
			return ChatColor.GREEN;
		}
		if (i < max) {
			return ChatColor.GOLD;
		}
		return ChatColor.RED;
	}

	public static short getIntervalGlassPaneColor(final int i, final int min, final int max) {
		if (i == 0) {
			return 0;
		}
		if (i < min) {
			return 5;
		}
		if (i < max) {
			return 1;
		}
		return 14;
	}

	public static String getName(UUID playerUniqueId) {
		Player player = Bukkit.getPlayer(playerUniqueId);
		if (player != null) {
			return player.getName();
		}
		try {
			OlympaPlayer olympaPlayer = new OlympaAccountProvider(playerUniqueId).get();
			if (olympaPlayer != null) {
				return olympaPlayer.getName();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static Player getNearestPlayer(final Player checkNear) {
		Player nearest = null;
		for (final Player p : checkNear.getWorld().getPlayers()) {
			if (nearest == null) {
				nearest = p;
			} else if (p.getLocation().distance(checkNear.getLocation()) < nearest.getLocation().distance(checkNear.getLocation())) {
				nearest = p;
			}
		}
		return nearest;
	}

	public static boolean hasEnoughPlace(final Inventory inventory, final ItemStack... items) {
		final Inventory inventory2 = Bukkit.createInventory(null, inventory.getSize());
		inventory2.setContents(inventory.getContents());
		int amount1 = 0;
		for (final ItemStack item : inventory2.getContents()) {
			if (item != null) {
				amount1 += item.getAmount();
			}
		}

		int amount2 = 0;
		for (final ItemStack item : items) {
			amount2 += item.getAmount();
		}

		final int amount3 = amount1 + amount2;
		inventory2.addItem(items);

		int amount4 = 0;
		for (final ItemStack item : inventory2.getContents()) {
			if (item != null) {
				amount4 += item.getAmount();
			}
		}

		if (amount4 == amount3) {
			return true;
		}

		return false;
	}

	/*
	 * public static EmeraldPlayer getPlayer(final Player player) { final
	 * EmeraldPlayer emeraldPlayer = EmeraldPlayers.getPlayer(player.getUniqueId());
	 * if(emeraldPlayer != null) { return emeraldPlayer; } return
	 * MySQL.getPlayer(player.getUniqueId()); }
	 */

	public static boolean isOnGround(final Player player) {
		Location location = player.getLocation();
		location = new Location(location.getWorld(), location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());
		return location.getBlock().getType() == Material.AIR;
	}

	public static boolean isSameLocation(final Location location1, final Location location2) {
		return location1.getBlockX() == location2.getBlockX() && location1.getBlockY() == location2.getBlockY() && location1.getBlockZ() == location2.getBlockZ();
	}

	public static boolean isSamePlayer(final Player player, final Player target) {
		return player.getUniqueId().equals(target.getUniqueId());
	}

	public static boolean playerisIn(final Player player, final Location location) {
		final Location playerLocation = player.getLocation();
		return playerLocation.getBlockX() == location.getBlockX() && (playerLocation.getBlockY() == location.getBlockY() || playerLocation.getBlockY() + 1 == location.getBlockY()) && playerLocation.getBlockZ() == location.getBlockZ();
	}

}
