package fr.olympa.core.spigot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.craftbukkit.v1_15_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import com.google.common.collect.Sets;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.editor.RegionEditor;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.region.tracking.RegionManager;
import fr.olympa.api.region.tracking.TrackedRegion;
import fr.olympa.api.utils.Prefix;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.TileEntity;
import net.minecraft.server.v1_15_R1.TileEntityTypes;

public class UtilsCommand extends ComplexCommand {

	public UtilsCommand(Plugin plugin) {
		super(plugin, "utils", "Commandes diverses", OlympaCorePermissions.UTILS_COMMAND);
		
		super.addArgumentParser("FILE", sender -> Arrays.stream(OlympaCore.getInstance().getDataFolder().listFiles()).map(File::getName).collect(Collectors.toList()), x -> new File(OlympaCore.getInstance().getDataFolder(), x));
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
						world.getTileEntity(tile.getKey()); // permet de créer la tile entity si elle existe
					}

				}
				//chunk.getTileEntities().clear();
			}
		}
		sendSuccess("Fin de l'opération.");
	}

	@Cmd (player = true)
	public void blockData(CommandContext cmd) {
		Block block = player.getTargetBlockExact(5);
		CraftBlock cblock = (CraftBlock) block;
		net.minecraft.server.v1_15_R1.Block nms = cblock.getNMS().getBlock();
		TileEntity tileEntity = cblock.getCraftWorld().getHandle().getTileEntity(cblock.getPosition());
		sendInfo("Position : %s", cblock.getPosition().toString());
		sendInfo("Bukkit Material : %s", block.getType().name());
		sendInfo("Tile entity : %b", nms.isTileEntity());
		if (tileEntity != null) {
			sendInfo("Tile entity type : %s", TileEntityTypes.a(tileEntity.getTileType()).getKey());
			sendInfo("Tile entity valid : %b", tileEntity.getTileType().isValidBlock(nms));
		}
	}
	
	@Cmd (player = true)
	public void fixTileEntities(CommandContext cmd) {
		net.minecraft.server.v1_15_R1.World world = ((CraftPlayer) sender).getHandle().world;
		sendInfo("Sélectionnez la région.");
		Player p = player;
		new RegionEditor(player, (region) -> {
			if (region == null) return;
			int tileEntities = 0;
			for (Iterator<Block> iterator = region.blockList(); iterator.hasNext();) {
				Block block = iterator.next();
				if (world.getTileEntity(((CraftBlock) block).getPosition()) != null) tileEntities++;
			}
			Prefix.DEFAULT_GOOD.sendMessage(p, "%d tile entities trouvées !", tileEntities);
		}).enterOrLeave();
	}

	@Cmd (player = true)
	public void regions(CommandContext cmd) {
		RegionManager regionManager = OlympaCore.getInstance().getRegionManager();
		Collection<TrackedRegion> trackedRegions = regionManager.getTrackedRegions().values();

		sendInfo("Régions trackées : %d", trackedRegions.size());
		sendInfo("Total de points : %d", trackedRegions.stream().mapToInt(x -> x.getRegion().getLocations().size()).sum());

		Set<TrackedRegion> playerRegions = regionManager.getCachedPlayerRegions(getPlayer());
		if (playerRegions == null) playerRegions = Collections.EMPTY_SET;
		sendInfo("Vous êtes actuellement dans les régions : §l%s", playerRegions.stream().map(x -> x.getID()).collect(Collectors.joining(", ", "[", "]")));

		Set<TrackedRegion> applicable = trackedRegions.stream().filter(x -> x.getRegion().isIn(getPlayer())).collect(Collectors.toSet());
		sendInfo("Différences entre les régions en cache et les régions calculées : §l%s", Sets.symmetricDifference(playerRegions, applicable).stream().map(x -> x.getID()).collect(Collectors.joining(", ", "[", "]")));
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
	
	@Cmd (min = 1, args = "FILE")
	public void deserializeBinary(CommandContext cmd) {
		File file = cmd.getArgument(0);
		ConfigurationSerializable serializable;
		try (BukkitObjectInputStream stream = new BukkitObjectInputStream(new FileInputStream(file))) {
			int bytes = stream.available();
			serializable = (ConfigurationSerializable) stream.readObject();
			sendSuccess("Object de type %s lu depuis %d bytes.", serializable.getClass().getName(), bytes);
		}catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			sendError();
			return;
		}
		YamlConfiguration yaml = new YamlConfiguration();
		yaml.set("object", serializable);
		try {
			yaml.save(file);
			sendSuccess("Fichier YAML écrit.");
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Cmd (min = 1, args = "FILE")
	public void serializeBinary(CommandContext cmd) {
		File file = cmd.getArgument(0);
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		Object object = config.get("object");
		sendSuccess("Object de type %s lu.", object.getClass().getName());
		
		try (BukkitObjectOutputStream stream = new BukkitObjectOutputStream(new FileOutputStream(file))) {
			stream.writeObject(object);
			sendSuccess("Fichier écrit.");
		}catch (IOException e) {
			e.printStackTrace();
			sendError();
			return;
		}
	}

}
