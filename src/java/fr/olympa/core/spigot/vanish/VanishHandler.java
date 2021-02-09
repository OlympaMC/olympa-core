package fr.olympa.core.spigot.vanish;

import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.olympa.api.module.OlympaModule.ModuleApi;
import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.scoreboard.tab.INametagApi;
import fr.olympa.api.scoreboard.tab.INametagApi.NametagHandler;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.vanish.IVanishApi;
import fr.olympa.core.spigot.OlympaCore;

@SuppressWarnings("deprecation")
public class VanishHandler implements IVanishApi, ModuleApi<OlympaCore> {

	NametagHandler handler;

	@Override
	public boolean enable(OlympaCore plugin) {
		if (handler != null)
			disable(plugin);
		INametagApi nameTagApi = plugin.getNameTagApi();
		if (nameTagApi != null) {
			handler = (nametag, op, to) -> {
				if (isVanished(op.getPlayer()) && OlympaAPIPermissions.VANISH_SEE.hasPermission(to))
					nametag.appendSuffix("§d[§5VANISH§d]");
			};
			nameTagApi.addNametagHandler(EventPriority.HIGHEST, handler);
			return true;
		} else
			plugin.sendMessage("&4VanishHandler &7> &cCan't add nameTagHandler because nameTagApi is disable.");
		return false;
	}

	@Override
	public boolean disable(OlympaCore plugin) {
		INametagApi nameTagApi = plugin.getNameTagApi();
		if (isEnabled()) {
			nameTagApi.removeNametagHandler(handler);
			handler = null;
			return true;
		}
		return false;
	}

	@Override
	public boolean isEnabled() {
		return handler != null;
	}

	@Override
	public boolean setToPlugin(OlympaCore plugin) {
		plugin.setVanishApi(this);
		return true;
	}

	@Override
	public void disable(OlympaPlayer olympaPlayer, boolean showMessage) {
		Player player = olympaPlayer.getPlayer();
		OlympaCore plugin = OlympaCore.getInstance();
		player.removePotionEffect(PotionEffectType.INVISIBILITY);
		player.setCollidable(true);
		removeVanishMetadata(player);
		INametagApi api = plugin.getNameTagApi();
		api.callNametagUpdate(olympaPlayer);
		Bukkit.getOnlinePlayers().forEach(p -> p.showPlayer(plugin, player));
		if (showMessage)
			Prefix.DEFAULT_BAD.sendMessage(player, "Tu n'es plus en vanish.");
	}

	@Override
	public void enable(OlympaPlayer olympaPlayer, boolean showMessage) {
		Player player = olympaPlayer.getPlayer();
		OlympaCore plugin = OlympaCore.getInstance();
		//		new OlympaSpigotPermission(olympaPlayer.getGroup()).getPlayers(null, noPerm -> noPerm.forEach(noAdmin -> noAdmin.hidePlayer(plugin, player)));
		player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false), true);
		player.setCollidable(false);
		addVanishMetadata(player);
		INametagApi api = plugin.getNameTagApi();
		OlympaAPIPermissions.VANISH_SEE.getOlympaPlayers(playerPerm -> {
			//			NametagAPI api = SpigotModule.NAME_TAG.getApi();
			api.callNametagUpdate(olympaPlayer, playerPerm);
			olympaPlayer.getPlayer().showPlayer(plugin, player);
		}, noPerm -> noPerm.forEach(noStaff -> noStaff.getPlayer().hidePlayer(plugin, player)));
		if (showMessage)
			Prefix.DEFAULT_GOOD.sendMessage(player, "Tu es désormais en vanish.");
	}

	@Override
	public void addVanishMetadata(Player player) {
		player.setMetadata("vanished", new FixedMetadataValue(OlympaCore.getInstance(), true));
	}

	@Override
	public Stream<? extends Player> getVanished() {
		return Bukkit.getOnlinePlayers().stream().filter(players -> isVanished(players));
	}

	@Override
	public boolean isVanished(Player player) {
		for (MetadataValue meta : player.getMetadata("vanished"))
			if (meta.asBoolean())
				return true;
		return false;
	}

	@Override
	public void removeVanishMetadata(Player player) {
		player.removeMetadata("vanished", OlympaCore.getInstance());
	}

}
