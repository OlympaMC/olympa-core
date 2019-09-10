package fr.tristiisch.olympa.api.bossbar;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Witherbar extends BukkitRunnable {

	private static String title;
	private static String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
	private static HashMap<UUID, Object> withers = new HashMap<>();
	private static Class<?> craftworldclass, entitywitherclass, packetentinitylivingout, craftplayerclass;
	private static Class<?> packetclass, packetconnection, packetplayoutdestroy, packetplayoutmeta, packetplayteleport;
	private static Constructor<?> witherconstructor, entitylivingconstructor, packetplayteleportconstructor, packetplaymetaconstructor;
	private static Method craftworldgethandle, craftplayergethandle, packetconnectsend, getentitywitherid, withersetlocationmethod;

	static {
		try {
			craftworldclass = getObcClass("CraftWorld");
			entitywitherclass = getNmsClass("EntityWither");
			packetentinitylivingout = getNmsClass("PacketPlayOutSpawnEntityLiving");
			craftplayerclass = getObcClass("entity.CraftPlayer");
			packetclass = getNmsClass("Packet");
			packetconnection = getNmsClass("PlayerConnection");
			packetplayoutdestroy = getNmsClass("PacketPlayOutEntityDestroy");
			packetplayoutmeta = getNmsClass("PacketPlayOutEntityMetadata");
			packetplayteleport = getNmsClass("PacketPlayOutEntityTeleport");
			witherconstructor = entitywitherclass.getConstructor(getNmsClass("World"));
			entitylivingconstructor = packetentinitylivingout.getConstructor(getNmsClass("EntityLiving"));
			packetplaymetaconstructor = packetplayoutmeta.getConstructor(int.class, getNmsClass("DataWatcher"), boolean.class);
			packetplayteleportconstructor = packetplayteleport.getConstructor(getNmsClass("Entity"));
			craftworldgethandle = craftworldclass.getMethod("getHandle");
			craftplayergethandle = craftplayerclass.getMethod("getHandle");
			packetconnectsend = packetconnection.getMethod("sendPacket", packetclass);
			getentitywitherid = entitywitherclass.getMethod("getId");
			withersetlocationmethod = entitywitherclass.getMethod("setLocation", double.class, double.class, double.class, float.class, float.class);
		} catch (final Exception error) {
			error.printStackTrace();
		}
	}

	public static void addPlayer(final Player player) {
		try {
			final Object craftworld = craftworldclass.cast(player.getWorld());
			final Object worldserver = craftworldgethandle.invoke(craftworld);
			final Object wither = witherconstructor.newInstance(worldserver);
			final Location location = getWitherLocation(player.getLocation());
			wither.getClass().getMethod("setCustomName", String.class).invoke(wither, title);
			wither.getClass().getMethod("setInvisible", boolean.class).invoke(wither, true);
			wither.getClass().getMethod("setLocation", double.class, double.class, double.class, float.class, float.class).invoke(wither, location.getX(), location.getY(), location.getZ(), 0, 0);
			final Object packet = entitylivingconstructor.newInstance(wither);
			final Object craftplayer = craftplayerclass.cast(player);
			final Object entityplayer = craftplayergethandle.invoke(craftplayer);
			final Object playerconnection = entityplayer.getClass().getField("playerConnection").get(entityplayer);
			packetconnectsend.invoke(packetconnection.cast(playerconnection), packet);
			withers.put(player.getUniqueId(), wither);
		} catch (final Exception error) {
			error.printStackTrace();
		}
	}

	public static Class<?> getNmsClass(final String classname) {
		final String fullname = "net.minecraft.server." + version + classname;
		Class<?> realclass = null;
		try {
			realclass = Class.forName(fullname);
		} catch (final Exception error) {
			error.printStackTrace();
		}
		return realclass;
	}

	public static Class<?> getObcClass(final String classname) {
		final String fullname = "org.bukkit.craftbukkit." + version + classname;
		Class<?> realclass = null;
		try {
			realclass = Class.forName(fullname);
		} catch (final Exception error) {
			error.printStackTrace();
		}
		return realclass;
	}

	public static Location getWitherLocation(final Location location) {
		return location.add(location.getDirection().normalize().multiply(20).add(new Vector(0, 5, 0)));
	}

	public static boolean hasPlayer(final Player player) {
		return withers.containsKey(player.getUniqueId());
	}

	public static void removePlayer(final Player player) {
		try {
			if (withers.containsKey(player.getUniqueId())) {
				final Object packet = packetplayoutdestroy.getConstructor(getNmsClass("EntityLiving")).newInstance(withers.get(player.getUniqueId()));
				withers.remove(player.getUniqueId());
				packetconnection = getNmsClass("PlayerConnection");
				packetclass = getNmsClass("Packet");
				craftplayerclass = getObcClass("entity.CraftPlayer");
				final Object craftplayer = craftplayerclass.cast(player);
				final Object entityplayer = craftplayergethandle.invoke(craftplayer);
				final Object playerconnection = entityplayer.getClass().getField("playerConnection").get(entityplayer);
				packetconnectsend.invoke(packetconnection.cast(playerconnection), packet);
			}
		} catch (final Exception error) {
			error.printStackTrace();
		}
	}

	public static void setProgress(float progress) {
		if (progress <= 0) {
			progress = (float) 0.001;
		}
		try {
			for (final Entry<UUID, Object> entry : withers.entrySet()) {
				final Object wither = entry.getValue();
				entitywitherclass = getNmsClass("EntityWither");
				wither.getClass().getMethod("setHealth", float.class).invoke(wither, progress * (float) entitywitherclass.getMethod("getMaxHealth").invoke(wither));
				final Object packet = packetplayoutmeta.getConstructor(int.class, getNmsClass("DataWatcher"), boolean.class)
						.newInstance(entitywitherclass.getMethod("getId").invoke(wither), entitywitherclass.getMethod("getDataWatcher").invoke(wither), true);
				craftplayerclass = getObcClass("entity.CraftPlayer");
				assert Bukkit.getPlayer(entry.getKey()) != null;
				final Object craftplayer = craftplayerclass.cast(Bukkit.getPlayer(entry.getKey()));
				final Object entityplayer = craftplayergethandle.invoke(craftplayer);
				packetclass = getNmsClass("Packet");
				packetconnection = getNmsClass("PlayerConnection");
				final Object playerconnection = entityplayer.getClass().getField("playerConnection").get(entityplayer);
				packetconnectsend.invoke(packetconnection.cast(playerconnection), packet);
			}
		} catch (final Exception error) {
			error.printStackTrace();
		}
	}

	public static void setTitle(final String title) {
		try {
			Witherbar.title = title;
			for (final Entry<UUID, Object> entry : withers.entrySet()) {
				final Object wither = entry.getValue();
				entitywitherclass = getNmsClass("EntityWither");
				wither.getClass().getMethod("setCustomName", String.class).invoke(wither, title);
				final Object packet = packetplaymetaconstructor.newInstance(getentitywitherid.invoke(wither), entitywitherclass.getMethod("getDataWatcher").invoke(wither), true);
				craftplayerclass = getObcClass("entity.CraftPlayer");
				assert Bukkit.getPlayer(entry.getKey()) != null;
				final Object craftplayer = craftplayerclass.cast(Bukkit.getPlayer(entry.getKey()));
				final Object entityplayer = craftplayergethandle.invoke(craftplayer);
				packetclass = getNmsClass("Packet");
				packetconnection = getNmsClass("PlayerConnection");
				final Object playerconnection = entityplayer.getClass().getField("playerConnection").get(entityplayer);
				packetconnectsend.invoke(packetconnection.cast(playerconnection), packet);
			}
		} catch (final Exception error) {
			error.printStackTrace();
		}
	}

	public Witherbar(final Plugin plugin, final String title) {
		Witherbar.title = title;
		final Plugin fixed = plugin;
		// this.runTaskTimer(fixed, 0, 0);
	}

	@Override
	public void run() {
		for (final Entry<UUID, Object> entry : withers.entrySet()) {
			if (Bukkit.getPlayer(entry.getKey()) != null) {
				try {
					final Object wither = entry.getValue();
					assert Bukkit.getPlayer(entry.getKey()) != null;
					final Location location = getWitherLocation(Bukkit.getPlayer(entry.getKey()).getEyeLocation());
					entitywitherclass = getNmsClass("EntityWither");
					withersetlocationmethod.invoke(wither, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
					final Object packet = packetplayteleportconstructor.newInstance(wither);
					craftplayerclass = getObcClass("entity.CraftPlayer");
					assert Bukkit.getPlayer(entry.getKey()) != null;
					final Object craftplayer = craftplayerclass.cast(Bukkit.getPlayer(entry.getKey()));
					final Object entityplayer = craftplayergethandle.invoke(craftplayer);
					packetclass = getNmsClass("Packet");
					packetconnection = getNmsClass("PlayerConnection");
					final Object playerconnection = entityplayer.getClass().getField("playerConnection").get(entityplayer);
					packetconnectsend.invoke(packetconnection.cast(playerconnection), packet);
				} catch (final Exception error) {
					error.printStackTrace();
				}
			} else {
				this.cancel();
			}
		}
	}

}