package fr.olympa.core.spigot.login;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import fr.olympa.api.common.task.OlympaTask;
import fr.olympa.api.spigot.captcha.MapCaptcha;
import fr.olympa.api.spigot.captcha.PlayerContents;
import fr.olympa.api.spigot.config.CustomConfig;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.OlympaCore;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import net.minecraft.server.v1_16_R3.PacketPlayInChat;

public class PlayerLogin {

	static OlympaCore core = OlympaCore.getInstance();
	private static Map<Player, PlayerLogin> w8forCaptcha = new HashMap<>();
	static CaptchaListener listener = new CaptchaListener();

	public static Map<Player, PlayerLogin> getW8forCaptcha() {
		return w8forCaptcha;
	}

	private static void unhandlePlayerPacket(Player p) {
		Channel channel = ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel;
		channel.eventLoop().submit(() -> {
			channel.pipeline().remove(p.getName() + "_Captcha");
			return null;
		});
	}

	private static void handlePlayerPackets(Player p) {
		ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
			@Override
			public void channelRead(ChannelHandlerContext channelHandlerContext, Object handledPacket) throws Exception {
				if (w8forCaptcha.containsKey(p) && !(handledPacket instanceof PacketPlayInChat))
					return;
				super.channelRead(channelHandlerContext, handledPacket);
			}
		};

		ChannelPipeline pipeline = ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel.pipeline();
		pipeline.addBefore("packet_handler", p.getName() + "_Captcha", channelDuplexHandler);
	}

	public static void add(Player player, PlayerLogin pl) {
		if (w8forCaptcha.isEmpty())
			core.getServer().getPluginManager().registerEvents(listener, core);
		PlayerLogin.w8forCaptcha.put(player, pl);
		handlePlayerPackets(player);
		Prefix.DEFAULT_BAD.sendMessage(player, "Tu dois répondre au captcha dans le chat.");
	}

	public static boolean isIn(Player p) {
		return w8forCaptcha.containsKey(p);
	}

	public static void remove(Player player) {
		PlayerLogin.w8forCaptcha.remove(player);
		if (w8forCaptcha.isEmpty())
			HandlerList.unregisterAll(listener);
		unhandlePlayerPacket(player);
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
	long timestamp;

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
		long timeTaken = Utils.getCurrentTimeInSeconds() - playerLogin.timestamp;
		Prefix.DEFAULT_GOOD.sendMessage(player, "La réponse était bien '%s'. Tu as réussi en %d secondes !", answer, timeTaken);
		playerLogin.playerContents.clearInventory();
		playerLogin.playerContents.returnHisInventory();
		if (playerLogin.location != null)
			player.teleport(playerLogin.location);
		remove(player);
		return true;
	}

	public static void captchaToPlayer(Player player) {
		OlympaTask task = core.getTask();
		Location location = null;
		if (core.getSpawn() != null) {
			location = player.getLocation();
			player.teleport(core.getSpawn());
		}
		player.getLocation().setPitch(40);
		PlayerContents playerContents = new PlayerContents(contentsConfig, player);
		PlayerLogin playerLogin = new PlayerLogin(playerContents, location);
		add(player, playerLogin);
		playerContents.clearInventory();
		task.runTaskAsynchronously(() -> {
			MapCaptcha map = new MapCaptcha();
			playerLogin.setMap(map);
			ItemStack item = map.getMap();
			PlayerInventory playerInv = player.getInventory();
			for (int i = 0; i <= 8; i++)
				playerInv.setItem(i, item);
			playerLogin.timestamp = Utils.getCurrentTimeInSeconds();
		});
	}
}
