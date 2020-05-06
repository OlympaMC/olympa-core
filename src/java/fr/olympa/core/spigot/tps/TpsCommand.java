package fr.olympa.core.spigot.tps;

import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.ProtocolAPI;
import fr.olympa.api.utils.TPS;
import fr.olympa.api.utils.TPSUtils;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.protocolsupport.ProtocolSupportHook;

public class TpsCommand extends OlympaCommand {

	public TpsCommand(Plugin plugin) {
		super(plugin, "tps");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		double[] tps = TPS.getDoubleTPS();
		float average = TPS.getAverage(tps);
		// float[] tpsFloat = TPS.round(tps);
		MachineInfo machine = new MachineInfo();
		OlympaCore core = OlympaCore.getInstance();
		StringJoiner sb = new StringJoiner("\n");
		sb.add("&3Serveur " + core.getServerName());
		sb.add("&3Ouvert depuis &b" + core.getUptime() + "&3.");
		sb.add("&3Status: " + core.getStatus().getNameColored() + "&3.");
		sb.add("&3TPS: &b1m " + TPSUtils.getTpsColor(tps[0]) + "&b 5m " + TPSUtils.getTpsColor(tps[1]) + "&b 15m " + TPSUtils.getTpsColor(tps[2]) + "&3.");
		sb.add("&3Moyenne: &b" + TPSUtils.getTpsColor(average) + "&3.");
		sb.add("&3RAM: &b" + machine.getMemUsage() + "&3 (" + machine.getMemUse() + ").");
		sb.add("&3CPU: &b" + machine.getCPUUsage() + "&3 (" + machine.getCores() + " cores).");
		sb.add("&3Threads: &b" + machine.getThreads() + "&3.");
		sb.add("&3Version du serveur: &b" + Bukkit.getBukkitVersion() + "&3.");
		ProtocolSupportHook protocolSupport = (ProtocolSupportHook) core.getProtocolSupport();
		if (protocolSupport != null) {
			sb.add("&3Versions supportés: &b" + protocolSupport.getVersionSupported() + "&3.");
		} else {
			String versionsString = "erreur";
			try {
				List<String> versions = ProtocolAPI.getVersionSupported();
				if (!versions.isEmpty()) {
					versionsString = String.join(", ", versions);
				}
			} catch (Exception ex) {
				versionsString = "erreur : " + ex.getMessage();
			}
			sb.add("&3Versions supportés: &b" + versionsString + "&3.");
		}
		for (World world : OlympaCore.getInstance().getServer().getWorlds()) {
			Chunk[] chunks = world.getLoadedChunks();
			List<Entity> entities = world.getEntities();
			List<LivingEntity> livingEntities = world.getLivingEntities();
			Collection<Chunk> forceChunks = world.getForceLoadedChunks();
			String fc = "";
			if (!forceChunks.isEmpty()) {
				fc = " (" + forceChunks.size() + " forcés)";
			}
			sb.add("&3Monde &b" + world.getName() + "&3: &b" + chunks.length + "&3 chunks &b" + livingEntities.size() + "/" + entities.size() + fc + "&3 entités" + "&3.");
		}

		sendMessage(Prefix.DEFAULT, sb.toString());
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
