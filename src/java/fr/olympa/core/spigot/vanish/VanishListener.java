package fr.olympa.core.spigot.vanish;

import java.lang.reflect.Field;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import com.mojang.authlib.GameProfile;

import fr.olympa.api.spigot.vanish.IVanishApi;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.module.CoreModules;
import io.netty.channel.ChannelDuplexHandler;
import net.minecraft.server.v1_16_R3.EnumGamemode;
import net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo;

public class VanishListener implements Listener {

	private Field datasField;
	private Field modeField;
	private Field profileField;

	public VanishListener() {
		try {
			datasField = PacketPlayOutPlayerInfo.class.getDeclaredField("b");
			datasField.setAccessible(true);
			modeField = Class.forName(PacketPlayOutPlayerInfo.class.getName() + "$PlayerInfoData").getDeclaredField("c");
			modeField.setAccessible(true);
			profileField = Class.forName(PacketPlayOutPlayerInfo.class.getName() + "$PlayerInfoData").getDeclaredField("d");
			profileField.setAccessible(true);
		}catch (ReflectiveOperationException ex) {
			ex.printStackTrace();
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		OlympaCore plugin = OlympaCore.getInstance();
		//		player.getActivePotionEffects().removeIf(p -> p.getType() == PotionEffectType.INVISIBILITY && p.getDuration() == 0);

		CoreModules.VANISH.getApi().getVanished().forEach(vanishPlayer -> player.hidePlayer(plugin, vanishPlayer));
		
		if (datasField == null) return;
		((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline().addBefore("packet_handler", "hide_spectators", new ChannelDuplexHandler() {
			public void write(io.netty.channel.ChannelHandlerContext ctx, Object msg, io.netty.channel.ChannelPromise promise) throws Exception {
				if (msg instanceof PacketPlayOutPlayerInfo) {
					PacketPlayOutPlayerInfo packet = (PacketPlayOutPlayerInfo) msg;
					List<Object> infos = (List<Object>) datasField.get(packet);
					for (Object data : infos) {
						if (modeField.get(data) == EnumGamemode.SPECTATOR) {
							if (((GameProfile) profileField.get(data)).getId().equals(player.getUniqueId())) continue;
							modeField.set(data, EnumGamemode.ADVENTURE);
						}
					}
				}
				super.write(ctx, msg, promise);
			};
		});
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		IVanishApi vanishHandler = CoreModules.VANISH.getApi();
		if (vanishHandler != null && vanishHandler.isVanished(player)) {
			event.setQuitMessage(null);
			vanishHandler.removeVanishMetadata(player);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player) || event.getCause() != DamageCause.MAGIC)
			return;
		Player player = (Player) event.getEntity();
		IVanishApi vanishHandler = CoreModules.VANISH.getApi();
		if (vanishHandler != null && vanishHandler.isVanished(player))
			event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityTarget(EntityTargetEvent event) {
		if (!(event.getTarget() instanceof Player))
			return;
		Player player = (Player) event.getTarget();
		IVanishApi vanishHandler = CoreModules.VANISH.getApi();
		if (vanishHandler != null && vanishHandler.isVanished(player))
			event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		IVanishApi vanishHandler = CoreModules.VANISH.getApi();
		if (vanishHandler != null && vanishHandler.isVanished(player))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		IVanishApi vanishHandler = CoreModules.VANISH.getApi();
		if (vanishHandler != null && vanishHandler.isVanished(player))
			return;
		if (event.getAction() == Action.PHYSICAL && event.getClickedBlock().getType() == Material.FARMLAND)
			event.setCancelled(true);
		else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block block = event.getClickedBlock();
			Inventory inventory = null;
			BlockState blockState = block.getState();
			switch (block.getType()) {
			case TRAPPED_CHEST:
			case CHEST:
				Chest chest = (Chest) blockState;
				inventory = chest.getInventory();
				break;
			case ENDER_CHEST:
				inventory = player.getEnderChest();
				break;
			default:
				return;
			}
			event.setCancelled(true);
			player.openInventory(inventory);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPotionSplash(PotionSplashEvent event) {
		for (LivingEntity entity : event.getAffectedEntities()) {
			if (!(entity instanceof Player))
				continue;
			Player player = (Player) entity;
			IVanishApi vanishHandler = CoreModules.VANISH.getApi();
			if (vanishHandler != null && vanishHandler.isVanished(player))
				event.setIntensity(player, 0);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPotionSplash(FoodLevelChangeEvent event) {
		Player player;
		HumanEntity entity = event.getEntity();
		if (!(entity instanceof Player))
			return;
		player = (Player) entity;
		IVanishApi vanishHandler = CoreModules.VANISH.getApi();
		if (vanishHandler != null && vanishHandler.isVanished(player))
			return;
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerPickupItem(EntityPickupItemEvent event) {
		if (event.getEntityType() != EntityType.PLAYER)
			return;
		
		//		Location locationOfItem = event.getEntity().getLocation();
		Player player = (Player) event.getEntity();
		if (!(event.getEntity() instanceof Player))
			return;
		IVanishApi vanishHandler = CoreModules.VANISH.getApi();
		if (vanishHandler != null && vanishHandler.isVanished(player))
			event.setCancelled(true);
	}
}


