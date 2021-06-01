package fr.olympa.core.spigot.scoreboards;

import java.util.List;

import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.commun.permission.list.OlympaCorePermissionsSpigot;
import fr.olympa.api.spigot.command.ComplexCommand;
import fr.olympa.api.spigot.scoreboard.tab.Nametag;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.scoreboards.api.NametagAPI;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;

public class NameTagCommand extends ComplexCommand {

	private NametagAPI api;

	public NameTagCommand(OlympaCore plugin, NametagAPI api) {
		//		super(plugin, "nametag", "Gestion et test des nameTag", OlympaCorePermissions.NAMETAG_COMMAND, "nt");
		super(plugin, "nametag", "Gestion et test des nameTag.", OlympaCorePermissionsSpigot.SPIGOT_LAG_COMMAND, "nt");
		this.api = api;
	}

	@Cmd(args = { "prefix", "suffix", "INTEGER", "PLAYERS" }, min = 2)
	public void set(CommandContext cmd) {
		String prefix = cmd.getArgument(0);
		String suffix = cmd.getArgument(1);
		int sortPriority = cmd.getArgumentsLength() > 2 ? cmd.getArgument(2) : 0;
		Player player = cmd.getArgumentsLength() > 3 ? cmd.getArgument(3) : getPlayer();
		Nametag nameTag = new Nametag();
		nameTag.appendPrefix(prefix);
		nameTag.appendSuffix(suffix);
		api.manager.changeFakeNametag(player.getName(), nameTag, sortPriority, List.of(getPlayer()));
	}

	@Cmd()
	public void test(CommandContext cmd) {
		removePlayers(player);
	}

	@Cmd()
	public void test2(CommandContext cmd) {
		addPlayers(player);
	}

	public void addPlayers(Player receiver) {
		EntityPlayer[] playersBukkit = receiver.getWorld().getEntities().stream().filter(p -> p instanceof HumanEntity && p.getUniqueId() != receiver.getUniqueId()).map(p -> ((CraftEntity) p).getHandle()).toArray(EntityPlayer[]::new);
		PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, playersBukkit);
		((CraftPlayer) receiver).getHandle().playerConnection.sendPacket(packet);
	}

	public void removePlayers(Player receiver) {
		EntityPlayer[] playersBukkit = receiver.getWorld().getEntities().stream().filter(p -> p instanceof HumanEntity && p.getUniqueId() != receiver.getUniqueId()).map(p -> ((CraftEntity) p).getHandle()).toArray(EntityPlayer[]::new);
		PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, playersBukkit);
		((CraftPlayer) receiver).getHandle().playerConnection.sendPacket(packet);
	}
}
