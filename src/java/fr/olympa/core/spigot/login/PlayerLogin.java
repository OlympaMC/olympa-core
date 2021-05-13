package fr.olympa.core.spigot.login;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import fr.olympa.api.captcha.MapCaptcha;
import fr.olympa.api.captcha.PlayerContents;
import fr.olympa.api.config.CustomConfig;
import fr.olympa.api.task.OlympaTask;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;

public class PlayerLogin {

	static OlympaCore core = OlympaCore.getInstance();
	private static Map<Player, PlayerLogin> w8forCaptcha = new HashMap<>();
	static CaptchaListener listener = new CaptchaListener();

	public static Map<Player, PlayerLogin> getW8forCaptcha() {
		return w8forCaptcha;
	}

	public static void add(Player player, PlayerLogin pl) {
		if (w8forCaptcha.isEmpty())
			core.getServer().getPluginManager().registerEvents(listener, core);
		PlayerLogin.w8forCaptcha.put(player, pl);
	}

	public static boolean isIn(Player p) {
		return w8forCaptcha.containsKey(p);
	}

	public static void remove(Player player) {
		PlayerLogin.w8forCaptcha.remove(player);
		if (w8forCaptcha.isEmpty())
			HandlerList.unregisterAll(listener);
	}

	public static CustomConfig contentsConfig = new CustomConfig(OlympaCore.getInstance(), "loginContents.yml");

	public PlayerLogin(PlayerContents playerContents, Location location) {
		this.playerContents = playerContents;
		this.location = location;
	}

	@Nullable
	MapCaptcha map;
	PlayerContents playerContents;
	Location location;

	public void setMap(MapCaptcha map) {
		this.map = map;
	}

	public static boolean captchaGood(Player player, String answer) {
		PlayerLogin playerLogin = w8forCaptcha.get(player);
		OlympaTask task = core.getTask();
		if (playerLogin == null || playerLogin.map == null) {
			Prefix.DEFAULT_BAD.sendMessage(player, "Merci de patienter pendant la génération d'un nouveau captcha", answer);
			return false;
		}
		if (!playerLogin.map.getAnswer().equals(answer)) {
			playerLogin.setMap(null);
			Prefix.DEFAULT_BAD.sendMessage(player, "La réponse est mauvaise, ce n'est pas '%s'. Réessaye !", answer);
			task.runTaskAsynchronously(() -> {
				MapCaptcha map = new MapCaptcha();
				playerLogin.setMap(map);
				player.getInventory().clear();
				player.getInventory().addItem(map.getMap());
			});
			return false;
		}
		playerLogin.playerContents.clearInventory();
		playerLogin.playerContents.returnHisInventory();
		if (playerLogin.location != null)
			player.teleport(playerLogin.location);
		w8forCaptcha.remove(player);
		return true;
	}

	public static void captchaToPlayer(Player player) {
		OlympaTask task = core.getTask();
		Location location = null;
		if (core.getSpawn() != null) {
			location = player.getLocation();
			player.teleport(core.getSpawn());
		}
		PlayerContents playerContents = new PlayerContents(contentsConfig, player);
		PlayerLogin playerLogin = new PlayerLogin(playerContents, location);
		w8forCaptcha.put(player, playerLogin);
		playerContents.clearInventory();
		task.runTaskAsynchronously(() -> {
			MapCaptcha map = new MapCaptcha();
			playerLogin.setMap(map);
			ItemStack item = map.getMap();
			PlayerInventory playerInv = player.getInventory();
			for (int i = 1; i < 9; i++)
				playerInv.setItem(i, item);
		});
	}
}
