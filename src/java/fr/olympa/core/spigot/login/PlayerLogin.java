package fr.olympa.core.spigot.login;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.permission.list.OlympaCorePermissionsSpigot;
import fr.olympa.api.common.provider.AccountProvider;
import fr.olympa.api.common.task.OlympaTask;
import fr.olympa.api.spigot.captcha.MapCaptcha;
import fr.olympa.api.spigot.captcha.PlayerContents;
import fr.olympa.api.spigot.config.CustomConfig;
import fr.olympa.api.spigot.region.shapes.Cuboid;
import fr.olympa.api.spigot.scoreboard.tab.INametagApi.NametagHandler;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.OlympaCore;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketListenerPlayIn;
import net.minecraft.server.v1_16_R3.PacketPlayInBlockDig;
import net.minecraft.server.v1_16_R3.PacketPlayInChat;
import net.minecraft.server.v1_16_R3.PacketPlayInFlying.PacketPlayInLook;
import net.minecraft.server.v1_16_R3.PacketPlayInFlying.PacketPlayInPosition;
import net.minecraft.server.v1_16_R3.PacketPlayInFlying.PacketPlayInPositionLook;
import net.minecraft.server.v1_16_R3.PacketPlayInKeepAlive;
import net.minecraft.server.v1_16_R3.PacketPlayInTeleportAccept;
import net.minecraft.server.v1_16_R3.PacketPlayInWindowClick;

public class PlayerLogin {

	static OlympaCore core = OlympaCore.getInstance();
	private static Map<Player, PlayerLogin> w8forCaptcha = new HashMap<>();
	private static NametagHandler nameTagHandler = (nametag, player, to) -> {
		if (w8forCaptcha.containsKey(player.getPlayer()))
			nametag.appendPrefix("§5[§dCAPTCHA§5]§r");
	};
	private static List<Class<? extends Packet<PacketListenerPlayIn>>> allowedPackets = List.of(PacketPlayInChat.class, PacketPlayInKeepAlive.class, PacketPlayInPositionLook.class,
			PacketPlayInPosition.class, PacketPlayInLook.class, PacketPlayInWindowClick.class, PacketPlayInTeleportAccept.class, PacketPlayInBlockDig.class);
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
				if (w8forCaptcha.containsKey(p) && allowedPackets.stream().noneMatch(clazz -> handledPacket.getClass().isAssignableFrom(clazz)))
					return;
				super.channelRead(channelHandlerContext, handledPacket);
			}
		};
		ChannelPipeline pipeline = ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel.pipeline();
		pipeline.addBefore("packet_handler", p.getName() + "_Captcha", channelDuplexHandler);
	}

	public static boolean captchaGood(Player player, String answer) {
		PlayerLogin playerLogin = w8forCaptcha.get(player);
		if (playerLogin == null || playerLogin.map == null) {
			Prefix.DEFAULT_BAD.sendMessage(player, "Merci de patienter pendant la génération d'un nouveau captcha", answer);
			return false;
		}
		if (!playerLogin.map.getAnswer().equals(answer)) {
			playerLogin.setMap(null);
			Prefix.DEFAULT_BAD.sendMessage(player, "La réponse est mauvaise, ce n'est pas '%s'. Réessaye !", answer);
			setMapToPlayer(player);
			return false;
		}
		long timeTaken = Utils.getCurrentTimeInSeconds() - playerLogin.timestamp;
		Prefix.DEFAULT_GOOD.sendMessage(player, "La réponse était bien '%s'. Tu as réussi en %d secondes !", answer, timeTaken);
		playerLogin.playerContents.clearInventory();
		playerLogin.playerContents.returnHisInventory();
		if (playerLogin.location != null)
			core.getTask().runTask(() -> player.teleport(playerLogin.location));
		remove(player);
		return true;
	}

	public static void captchaToPlayer(Player player) {
		if (!isIn(player)) {
			Location location = null;
			if (core.getSpawn() != null) {
				location = player.getLocation();
				Location newLoc = core.getSpawn().clone();
				newLoc.setPitch(40);
				newLoc.setX(0.5);
				newLoc.setZ(0.5);
				player.teleport(newLoc);
			} else {
				Location newLoc = player.getLocation().clone();
				newLoc.setPitch(40);
				newLoc.setX(0.5);
				newLoc.setZ(0.5);
				player.teleport(newLoc);
			}
			PlayerContents playerContents = new PlayerContents(contentsConfig, player);
			PlayerLogin playerLogin = new PlayerLogin(playerContents, location);
			playerContents.clearInventory();
			add(player, playerLogin);
		} else
			setMapToPlayer(player);
		Prefix.DEFAULT_BAD.sendMessage(player, "Tu dois répondre au captcha dans le chat.");
	}

	public static void add(Player player, PlayerLogin pl) {
		if (w8forCaptcha.isEmpty()) {
			core.getServer().getPluginManager().registerEvents(listener, core);
			if (core.getNameTagApi() != null)
				core.getNameTagApi().addNametagHandler(EventPriority.LOWEST, nameTagHandler);
		}
		if (!isIn(player)) {
			PlayerLogin.w8forCaptcha.put(player, pl);
			handlePlayerPackets(player);
		}
		setMapToPlayer(player);
		setHidingBlock(player);
		player.sendTitle(ColorUtils.color("&4Captcha"), ColorUtils.color("&cEcrit les lettres en minuscules dans le chat"), 0, 100, 0);
		player.setWalkSpeed(0);
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128, false, false));
		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 128, false, false));
		OlympaCorePermissionsSpigot.CAPCHAT_SEE_WAITING.getPlayers(success -> {}, withoutPermPlayers -> withoutPermPlayers.stream().filter(p -> !p.getUniqueId().equals(player.getUniqueId()))
				.forEach(p -> p.hidePlayer(core, player)));
		if (core.getNameTagApi() != null)
			core.getNameTagApi().callNametagUpdate(AccountProvider.getter().get(player.getUniqueId()));
	}

	public static void remove(Player player) {
		removeHidingBlock(player);
		PlayerLogin.w8forCaptcha.remove(player);
		if (w8forCaptcha.isEmpty()) {
			HandlerList.unregisterAll(listener);
			if (core.getNameTagApi() != null)
				core.getNameTagApi().removeNametagHandler(nameTagHandler);
		}
		unhandlePlayerPacket(player);
		player.setWalkSpeed(0.2f);
		core.getTask().runTask(() -> {
			player.removePotionEffect(PotionEffectType.JUMP);
			player.removePotionEffect(PotionEffectType.SLOW);
			Bukkit.getOnlinePlayers().forEach(p -> p.showPlayer(player));
		});
		core.getNameTagApi().callNametagUpdate(AccountProvider.getter().get(player.getUniqueId()));
	}

	public static void setHidingBlock(Player p) {
		core.getTask().runTask(() -> p.setGameMode(GameMode.ADVENTURE));
		Location loc = p.getLocation();
		PlayerLogin playerLogin = w8forCaptcha.get(p);
		if (playerLogin == null)
			throw new UnsupportedOperationException("PlayerLogin is not set for " + p.getName());
		if (playerLogin.blockLocs != null && !playerLogin.blockLocs.isEmpty()) {
			playerLogin.blockLocs.forEach(l -> p.sendBlockChange(l, l.getBlock().getBlockData()));
			playerLogin.blockLocs.clear();
		}
		BlockData blockdata = Material.SEA_LANTERN.createBlockData();
		playerLogin.blockLocs = new Cuboid(new Location(loc.getWorld(), loc.getBlockX() - 1, loc.getBlockY() - 1, loc.getBlockZ() - 1),
				new Location(loc.getWorld(), loc.getBlockX() + 1, loc.getBlockY() + 2, loc.getBlockZ() + 1)).getCubeLocations();
		playerLogin.blockLocs.forEach(l -> p.sendBlockChange(l, blockdata));
	}

	public static void removeHidingBlock(Player p) {
		core.getTask().runTask(() -> p.setGameMode(GameMode.SURVIVAL));
		PlayerLogin playerLogin = w8forCaptcha.get(p);
		if (playerLogin != null && playerLogin.blockLocs != null && !playerLogin.blockLocs.isEmpty()) {
			playerLogin.blockLocs.forEach(l -> p.sendBlockChange(l, l.getBlock().getBlockData()));
			playerLogin.blockLocs.clear();
		}
	}

	private static void setMapToPlayer(Player player) {
		OlympaTask task = core.getTask();
		task.runTaskAsynchronously(() -> {
			MapCaptcha map = new MapCaptcha();
			PlayerLogin playerLogin = w8forCaptcha.get(player);
			if (playerLogin == null)
				throw new UnsupportedOperationException("PlayerLogin is not set for " + player.getName());
			playerLogin.setMap(map);
			ItemStack item = map.getMap();
			PlayerInventory playerInv = player.getInventory();
			playerInv.clear();
			for (int i = 0; i <= 8; i++)
				playerInv.setItem(i, item);
			playerLogin.timestamp = Utils.getCurrentTimeInSeconds();
		});
	}

	public static boolean isIn(Player p) {
		return w8forCaptcha.containsKey(p);
	}

	public static CustomConfig contentsConfig = new CustomConfig(OlympaCore.getInstance(), "loginContents.yml");

	public PlayerLogin(PlayerContents playerContents, Location location) {
		this.playerContents = playerContents;
		this.location = location;
	}

	@Nullable
	MapCaptcha map;
	PlayerContents playerContents;
	@Nullable
	Location location;
	@Nullable
	List<Location> blockLocs;
	long timestamp;

	public void setMap(MapCaptcha map) {
		this.map = map;
	}
}
