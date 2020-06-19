package fr.olympa.core.spigot;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_15_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.Sets;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.holograms.Hologram;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.lines.AbstractLine;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.region.tracking.RegionManager;
import fr.olympa.api.region.tracking.TrackedRegion;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.TileEntity;

public class UtilsCommand extends ComplexCommand {

	public UtilsCommand(Plugin plugin) {
		super(plugin, "utils", "Commandes diverses", OlympaCorePermissions.UTILS_COMMAND);
	}

	@Cmd (player = true)
	public void chunkfix(CommandContext cmd) {
		net.minecraft.server.v1_15_R1.World world = ((CraftPlayer) sender).getHandle().world;
		Chunk oriChunk = getPlayer().getLocation().getChunk();
		for (int x = oriChunk.getX() - 16; x < oriChunk.getX() + 16; x++) {
			for (int z = oriChunk.getZ() - 16; z < oriChunk.getZ() + 16; z++) {
				net.minecraft.server.v1_15_R1.Chunk chunk = ((CraftChunk) getPlayer().getWorld().getChunkAt(x, z)).getHandle();
				for (Iterator<Entry<BlockPosition, TileEntity>> iterator = chunk.getTileEntities().entrySet().iterator(); iterator.hasNext();) {
					Entry<BlockPosition, TileEntity> tile = iterator.next();
					if (!tile.getValue().getTileType().isValidBlock(world.getType(tile.getKey()).getBlock())) {
						String str = "invalid tile entity: removing " + tile.getValue().getPosition().toString();
						System.out.println(str);
						sendSuccess(str);
						//world.removeTileEntity(tile.getValue().getPosition());
						//world.tileEntityList.remove(tile.getValue());
						world.tileEntityListTick.remove(tile.getValue());
						iterator.remove();
					}
				}
				//chunk.getTileEntities().clear();
			}
		}
	}

	@Cmd (player = true)
	public void regions(CommandContext cmd) {
		RegionManager regionManager = OlympaCore.getInstance().getRegionManager();
		Collection<TrackedRegion> trackedRegions = regionManager.getTrackedRegions().values();

		sendInfo("Régions trackées : " + trackedRegions.size());
		sendInfo("Total de points : " + trackedRegions.stream().mapToInt(x -> x.getRegion().getLocations().size()).sum());

		Set<TrackedRegion> playerRegions = regionManager.getCachedPlayerRegions(getPlayer());
		StringJoiner joiner = new StringJoiner(", ", "Vous êtes actuellement dans les régions : [", "]");
		if (playerRegions == null) playerRegions = Collections.EMPTY_SET;
		playerRegions.forEach(x -> joiner.add(x.getID()));
		sendInfo(joiner.toString());

		Set<TrackedRegion> applicable = trackedRegions.stream().filter(x -> x.getRegion().isIn(getPlayer())).collect(Collectors.toSet());
		sendInfo("Différences entre les régions en cache et les régions calculées : §l" + Sets.symmetricDifference(playerRegions, applicable));
	}

	@Cmd (min = 4, args = { "", "INTEGER", "INTEGER", "INTEGER" })
	public void testRegion(CommandContext cmd) {
		World world = Bukkit.getWorld((String) cmd.getArgument(0));
		int x = cmd.getArgument(1);
		int y = cmd.getArgument(2);
		int z = cmd.getArgument(3);
		for (TrackedRegion trackedRegion : OlympaCore.getInstance().getRegionManager().getTrackedRegions().values()) {
			if (trackedRegion.getRegion().isIn(world, x, y, z)) {
				sendInfo("Is in " + trackedRegion.getID());
			}else sendInfo("Not in " + trackedRegion.getID());
		}
	}

	@Cmd (player = true, min = 1, syntax = "<player name>")
	public void givePlayerHead(CommandContext cmd) {
		ItemUtils.skull(x -> player.getInventory().addItem(x), "item tête", cmd.getArgument(0));
	}

	@Cmd (player = true, min = 1, syntax = "<head value>")
	public void giveCustomHead(CommandContext cmd) {
		player.getInventory().addItem(ItemUtils.skullCustom("item custom tête", cmd.getArgument(0)));
	}

	@Cmd (player = true, min = 1, syntax = "<lignes>")
	public void createHologram(CommandContext cmd) {
		String[] linesStrings = cmd.getFrom(0).split("\\|");
		OlympaCore.getInstance().getHologramsManager().addHologram(new Hologram(player.getLocation().add(0, 1, 0), Arrays.stream(linesStrings).map(x -> new FixedLine<>(x)).toArray(AbstractLine[]::new)));
	}

}
