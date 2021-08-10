package fr.olympa.core.spigot.vanish;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.EnderChest;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
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
import org.bukkit.inventory.ItemStack;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.authlib.GameProfile;

import fr.olympa.api.common.module.OlympaModule;
import fr.olympa.api.common.permission.list.OlympaAPIPermissionsSpigot;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.spigot.vanish.IVanishApi;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.common.provider.AccountProvider;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.module.CoreModules;
import io.netty.channel.ChannelDuplexHandler;
import net.minecraft.server.v1_16_R3.EnumGamemode;
import net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo;

public class VanishListener implements Listener {

	private Field actionField;
	private Field datasField;
	private Field modeField;
	private Field pingField;
	private Field profileField;
	private Field nameField;
	private Constructor<?> playerDataConstructor;
	private Cache<PacketPlayOutPlayerInfo, PacketState> packets = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).initialCapacity(10).build();

	private Lock specLock = new ReentrantLock();

	public VanishListener() {
		try {
			actionField = PacketPlayOutPlayerInfo.class.getDeclaredField("a");
			actionField.setAccessible(true);
			datasField = PacketPlayOutPlayerInfo.class.getDeclaredField("b");
			datasField.setAccessible(true);
			modeField = Class.forName(PacketPlayOutPlayerInfo.class.getName() + "$PlayerInfoData").getDeclaredField("c");
			modeField.setAccessible(true);
			pingField = Class.forName(PacketPlayOutPlayerInfo.class.getName() + "$PlayerInfoData").getDeclaredField("b");
			pingField.setAccessible(true);
			profileField = Class.forName(PacketPlayOutPlayerInfo.class.getName() + "$PlayerInfoData").getDeclaredField("d");
			profileField.setAccessible(true);
			nameField = Class.forName(PacketPlayOutPlayerInfo.class.getName() + "$PlayerInfoData").getDeclaredField("e");
			nameField.setAccessible(true);
			playerDataConstructor = Class.forName(PacketPlayOutPlayerInfo.class.getName() + "$PlayerInfoData").getDeclaredConstructors()[0];
			playerDataConstructor.setAccessible(true);
		} catch (ReflectiveOperationException ex) {
			ex.printStackTrace();
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		OlympaCore plugin = OlympaCore.getInstance();
		OlympaPlayer olympaPlayer = AccountProvider.getter().get(player.getUniqueId());
		if (!OlympaAPIPermissionsSpigot.VANISH_COMMAND.hasPermission(olympaPlayer))
			CoreModules.VANISH.getApi().getVanished().forEach(vanishPlayer -> player.hidePlayer(plugin, vanishPlayer));
		else if (!OlympaAPIPermissionsSpigot.VANISH_SEE_ADMIN.hasPermission(olympaPlayer))
			CoreModules.VANISH.getApi().getVanished().filter(vanishPlayer -> OlympaAPIPermissionsSpigot.VANISH_SEE_ADMIN.hasPermission(vanishPlayer.getUniqueId()))
			.forEach(vanishPlayer -> player.hidePlayer(plugin, vanishPlayer));
		if (olympaPlayer.isVanish())
			if (OlympaAPIPermissionsSpigot.VANISH_COMMAND.hasPermission(olympaPlayer)) {
				event.setJoinMessage(null);
				CoreModules.VANISH.getApi().enable(olympaPlayer, true);
			} else
				olympaPlayer.setVanish(false);
		if (datasField == null)
			return;

		((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline().addBefore("packet_handler", "hide_spectators", new ChannelDuplexHandler() {
			@Override
			public void write(io.netty.channel.ChannelHandlerContext ctx, Object msg, io.netty.channel.ChannelPromise promise) throws Exception {
				if (msg instanceof PacketPlayOutPlayerInfo packet) {

					Object action = actionField.get(packet);
					if (action == PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_GAME_MODE || action == PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER) {
						specLock.lock();
						PacketState state = packets.getIfPresent(packet);
						if (state != PacketState.IGNORE) {

							List<Object> infos = (List<Object>) datasField.get(packet);

							for (Object data : infos) {
								EnumGamemode mode = (EnumGamemode) modeField.get(data);
								if (mode == EnumGamemode.SPECTATOR) {
									modeField.set(data, EnumGamemode.ADVENTURE);
									packets.put(packet, PacketState.EDITED);
									state = PacketState.EDITED;
									mode = EnumGamemode.ADVENTURE;
								}
								if (state == PacketState.EDITED) {
									GameProfile profile = (GameProfile) profileField.get(data);
									if (profile.getId().equals(player.getUniqueId())) {
										EnumGamemode realMode = EnumGamemode.getById(player.getGameMode().getValue());
										if (mode != realMode) {
											PacketPlayOutPlayerInfo newPacket = new PacketPlayOutPlayerInfo();
											actionField.set(newPacket, action);
											List<Object> newInfos = new ArrayList<>(infos.size());
											for (Object newData : infos) {
												if (data == newData)
													newData = playerDataConstructor.newInstance(newPacket, profile, pingField.getInt(data), realMode, nameField.get(data));
												newInfos.add(newData);
											}
											datasField.set(newPacket, newInfos);
											packets.put(newPacket, PacketState.IGNORE);
											((CraftPlayer) player).getHandle().playerConnection.sendPacket(newPacket);
											specLock.unlock();
											return;
										}
									}
								}
							}
						}
						specLock.unlock();
						if (OlympaModule.DEBUG)
							System.out.println("Sent PacketPlayOutPlayerInfo to " + player.getName() + " state " + Objects.toString(state) + " : " + packet.toString());
					}
				}

				super.write(ctx, msg, promise);
			}
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

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		IVanishApi vanishHandler = CoreModules.VANISH.getApi();
		if (vanishHandler == null || !vanishHandler.isVanished(player) || event.isCancelled() && player.getGameMode() != GameMode.CREATIVE)
			return;
		Block block = event.getBlock();
		if (player.getGameMode() == GameMode.CREATIVE)
			player.getInventory().addItem(block.getDrops().toArray(ItemStack[]::new));
		block.setType(Material.AIR);
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		IVanishApi vanishHandler = CoreModules.VANISH.getApi();
		if (vanishHandler == null || !vanishHandler.isVanished(player) || event.isCancelled() && player.getGameMode() != GameMode.CREATIVE)
			return;
		ItemStack itemInHand = event.getItemInHand();
		Block block = event.getBlock();
		if (player.getGameMode() != GameMode.CREATIVE)
			if (itemInHand.getAmount() > 1)
				itemInHand.setAmount(itemInHand.getAmount() - 1);
			else
				player.getInventory().removeItem(itemInHand);
		block.getWorld().getBlockAt(block.getLocation()).setType(itemInHand.getType());
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
		if (vanishHandler == null || !vanishHandler.isVanished(player))
			return;
		if (event.getAction() == Action.PHYSICAL)
			event.setCancelled(true);
		else if ((!player.isSneaking() || player.getInventory().getItemInMainHand().getType() == Material.AIR) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block block = event.getClickedBlock();
			BlockState blockState = block.getState();
			BlockData blockData = block.getBlockData();
			if (blockData instanceof EnderChest) {
				event.setCancelled(true);
				player.openInventory(player.getEnderChest());
				Prefix.VANISH.sendMessage(player, "Ouverture forcé et silencieuse de ton EnderChest");
			} else if (blockData instanceof Openable) {
				Openable openable = (Openable) blockData;
				event.setCancelled(true);
				if (openable.isOpen())
					Prefix.VANISH.sendMessage(player, "Fermeture forcé de %s", Utils.capitalize(block.getType().toString().replace("_", " ")));
				else
					Prefix.VANISH.sendMessage(player, "Ouverture forcé de %s", Utils.capitalize(block.getType().toString().replace("_", " ")));
				openable.setOpen(!openable.isOpen());
				blockState.setBlockData(openable);
				blockState.update();
			} else if (blockState instanceof Container) {
				Container container = (Container) blockState;
				event.setCancelled(true);
				player.openInventory(container.getInventory());
				Prefix.VANISH.sendMessage(player, "Ouverture forcé du %s", Utils.capitalize(block.getType().toString().replace("_", " ")));
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPotionSplash(PotionSplashEvent event) {
		for (LivingEntity entity : event.getAffectedEntities()) {
			if (!(entity instanceof Player player))
				continue;
			IVanishApi vanishHandler = CoreModules.VANISH.getApi();
			if (vanishHandler != null && vanishHandler.isVanished(player))
				event.setIntensity(player, 0);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onFoodChange(FoodLevelChangeEvent event) {
		if (!(event.getEntity()instanceof Player player)) return;
		IVanishApi vanishHandler = CoreModules.VANISH.getApi();
		if (vanishHandler != null && vanishHandler.isVanished(player))
			event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerPickupItem(EntityPickupItemEvent event) {
		if (!(event.getEntity()instanceof Player player)) return;
		IVanishApi vanishHandler = CoreModules.VANISH.getApi();
		if (vanishHandler != null && vanishHandler.isVanished(player))
			event.setCancelled(true);
	}



	enum PacketState {
		EDITED,
		IGNORE;
	}
}
