package fr.olympa.core.spigot.vanish;

import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.vanish.IVanishApi;
import fr.olympa.core.spigot.OlympaCore;

@SuppressWarnings("deprecation")
public class VanishHandler implements IVanishApi {

	@Override
	public void disable(OlympaPlayer olympaPlayer, boolean showMessage) {
		Player player = olympaPlayer.getPlayer();
		OlympaCore plugin = OlympaCore.getInstance();
		player.removePotionEffect(PotionEffectType.INVISIBILITY);
		player.setCollidable(true);
		removeVanishMetadata(player);
		Bukkit.getOnlinePlayers().forEach(p -> p.showPlayer(plugin, player));
		if (showMessage)
			Prefix.DEFAULT_BAD.sendMessage(player, "Tu n'es plus en vanish.");
	}

	@Override
	public void enable(OlympaPlayer olympaPlayer, boolean showMessage) {
		Player player = olympaPlayer.getPlayer();
		OlympaCore plugin = OlympaCore.getInstance();
		//		new OlympaSpigotPermission(olympaPlayer.getGroup()).getPlayers(null, noPerm -> noPerm.forEach(noAdmin -> noAdmin.hidePlayer(plugin, player)));
		player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999999, 0, false, false), true);
		player.setCollidable(false);
		addVanishMetadata(player);
		OlympaAPIPermissions.VANISH_SEE.getOlympaPlayers(perm -> {
			plugin.getNameTagApi().callNametagUpdate(olympaPlayer, perm);
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
