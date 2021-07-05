package fr.olympa.core.spigot.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.spigot.command.ComplexCommand;
import fr.olympa.api.spigot.editor.RegionEditor;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsSpigot;
import fr.olympa.core.common.provider.AccountProvider;
import fr.olympa.core.spigot.OlympaCore;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.TileEntity;
import net.minecraft.server.v1_16_R3.TileEntityTypes;

public class UtilsCommand extends ComplexCommand {

	public UtilsCommand(Plugin plugin) {
		super(plugin, "utils", "Commandes de développement diverses.", OlympaCorePermissionsSpigot.UTILS_COMMAND);

		super.addArgumentParser("FILE", (sender, arg) -> Arrays.stream(OlympaCore.getInstance().getDataFolder().listFiles()).map(File::getName).collect(Collectors.toList()),
				x -> new File(OlympaCore.getInstance().getDataFolder(), x), null);
	}

	@Cmd(player = true)
	public void chunkfix(CommandContext cmd) {
		net.minecraft.server.v1_16_R3.World world = ((CraftPlayer) sender).getHandle().world;
		Chunk oriChunk = getPlayer().getLocation().getChunk();
		for (int x = oriChunk.getX() - 16; x < oriChunk.getX() + 16; x++)
			for (int z = oriChunk.getZ() - 16; z < oriChunk.getZ() + 16; z++) {
				net.minecraft.server.v1_16_R3.Chunk chunk = ((CraftChunk) getPlayer().getWorld().getChunkAt(x, z)).getHandle();
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
		sendSuccess("Fin de l'opération.");
	}

	@Cmd(player = true)
	public void blockData(CommandContext cmd) {
		Block block = player.getTargetBlockExact(5);
		CraftBlock cblock = (CraftBlock) block;
		net.minecraft.server.v1_16_R3.Block nms = cblock.getNMS().getBlock();
		TileEntity tileEntity = cblock.getCraftWorld().getHandle().getTileEntity(cblock.getPosition());
		sendInfo("Position : %s", cblock.getPosition().toString());
		sendInfo("Bukkit Material : %s", block.getType().name());
		sendInfo("Tile entity : %b", nms.isTileEntity());
		if (tileEntity != null) {
			sendInfo("Tile entity type : %s", TileEntityTypes.a(tileEntity.getTileType()).getKey());
			sendInfo("Tile entity valid : %b", tileEntity.getTileType().isValidBlock(nms));
		}
	}

	@Cmd(player = true)
	public void fixTileEntities(CommandContext cmd) {
		net.minecraft.server.v1_16_R3.World world = ((CraftPlayer) sender).getHandle().world;
		sendInfo("Sélectionnez la région.");
		Player p = player;
		new RegionEditor(player, (region) -> {
			if (region == null)
				return;
			int tileEntities = 0;
			for (Iterator<Block> iterator = region.blockList(); iterator.hasNext();) {
				Block block = iterator.next();
				if (world.getTileEntity(((CraftBlock) block).getPosition()) != null)
					tileEntities++;
			}
			Prefix.DEFAULT_GOOD.sendMessage(p, "%d tile entities trouvées !", tileEntities);
		}).enterOrLeave();
	}

	@Cmd(min = 1)
	public void uuid(CommandContext cmd) {
		String arg1 = cmd.getArgument(0);
		String playerName = arg1.toLowerCase();
		UUID uuidCrack;
		UUID uuidPremium;
		UUID uuidActual;
		try {
			uuidCrack = UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			sendMessage(Prefix.DEFAULT_BAD, "Une erreur est survenue avec la conversion en uuid crack de %s.", arg1);
			e.printStackTrace();
			return;
		}
		try {
			OlympaPlayer op = AccountProvider.getter().get(arg1);
			if (op == null) {
				sendMessage(Prefix.DEFAULT_GOOD, "UUID Crack théorique (générer à partir du pseudo actuel %s) : %s\nCe joueur n'a pas de données en bdd.", playerName, uuidCrack);
				return;
			}
			uuidPremium = op.getPremiumUniqueId();
			uuidActual = op.getUniqueId();
		} catch (SQLException e) {
			sendMessage(Prefix.DEFAULT_BAD, "Une erreur est survenue avec la récupération des données du joueur %s.", arg1);
			e.printStackTrace();
			return;
		}
		sendMessage(Prefix.DEFAULT_GOOD, "UUID Crack théorique (générer à partir du pseudo actuel %s) : %s\nUUID serveur (en bdd) %s\nUUID Premium (en bdd) %s.", playerName, uuidCrack, uuidActual, uuidPremium);
	}

	@Cmd(player = true, min = 1, syntax = "<player name>")
	public void givePlayerHead(CommandContext cmd) {
		ItemUtils.skull(x -> player.getInventory().addItem(x), "item tête", cmd.getArgument(0));
	}

	@Cmd(player = true, min = 1, syntax = "<head value>")
	public void giveCustomHead(CommandContext cmd) {
		player.getInventory().addItem(ItemUtils.skullCustom("item custom tête", cmd.getArgument(0)));
	}

	@Cmd(min = 1, args = "FILE")
	public void deserializeBinary(CommandContext cmd) {
		File file = cmd.getArgument(0);
		ConfigurationSerializable serializable;
		try (BukkitObjectInputStream stream = new BukkitObjectInputStream(new FileInputStream(file))) {
			int bytes = stream.available();
			serializable = (ConfigurationSerializable) stream.readObject();
			sendSuccess("Object de type %s lu depuis %d bytes.", serializable.getClass().getName(), bytes);
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			sendError();
			return;
		}
		YamlConfiguration yaml = new YamlConfiguration();
		yaml.set("object", serializable);
		try {
			yaml.save(file);
			sendSuccess("Fichier YAML écrit.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Cmd(min = 1, args = "FILE")
	public void serializeBinary(CommandContext cmd) {
		File file = cmd.getArgument(0);
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		Object object = config.get("object");
		sendSuccess("Object de type %s lu.", object.getClass().getName());

		try (BukkitObjectOutputStream stream = new BukkitObjectOutputStream(new FileOutputStream(file))) {
			stream.writeObject(object);
			sendSuccess("Fichier écrit.");
		} catch (IOException e) {
			e.printStackTrace();
			sendError();
			return;
		}
	}

}
