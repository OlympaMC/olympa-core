package fr.olympa.core.spigot.vanish;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.olympa.api.common.module.OlympaModule.ModuleApi;
import fr.olympa.api.common.permission.list.OlympaAPIPermissionsSpigot;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.spigot.scoreboard.tab.INametagApi;
import fr.olympa.api.spigot.scoreboard.tab.INametagApi.NametagHandler;
import fr.olympa.api.spigot.vanish.IVanishApi;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;

public class VanishHandler implements IVanishApi, ModuleApi<OlympaCore> {

	NametagHandler handler;
	Map<String, TriConsumer<Player, OlympaPlayer, Boolean>> consumerRegister = new HashMap<>();

	/**
	 * @param name
	 * @param biConsumer key = olympaPlayer value = willBecomeVanished
	 * @return false if other handler has already register with same name. Parameters override this old entry
	 */
	@Override
	public boolean registerHandler(String name, TriConsumer<Player, OlympaPlayer, Boolean> biConsumer) {
		return consumerRegister.put(name, biConsumer) == null;
	}

	@Override
	public boolean unRegisterHandler(String name) {
		return consumerRegister.remove(name) != null;
	}

	@Override
	public boolean enable(OlympaCore plugin) {
		if (handler != null)
			disable(plugin);
		INametagApi nameTagApi = plugin.getNameTagApi();
		if (nameTagApi != null) {
			handler = (nametag, op, to) -> {
				if (isVanished((Player) op.getPlayer()) && OlympaAPIPermissionsSpigot.VANISH_SEE.hasPermission(to))
					nametag.appendSuffix("??d[??5VANISH??d]??r");
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
		Player player = (Player) olympaPlayer.getPlayer();
		OlympaCore plugin = OlympaCore.getInstance();
		player.removePotionEffect(PotionEffectType.NIGHT_VISION);
		player.setCollidable(true);
		removeVanishMetadata(player);
		INametagApi api = plugin.getNameTagApi();
		api.callNametagUpdate(olympaPlayer);
		Bukkit.getOnlinePlayers().forEach(p -> p.showPlayer(plugin, player));
		if (showMessage)
			Prefix.DEFAULT_BAD.sendMessage(player, "Tu n'es plus en vanish.");
		olympaPlayer.setVanish(false);
		consumerRegister.values().forEach(consumer -> consumer.accept(player, olympaPlayer, false));
	}

	@Override
	public void enable(OlympaPlayer olympaPlayer, boolean showMessage) {
		Player player = (Player) olympaPlayer.getPlayer();
		OlympaCore plugin = OlympaCore.getInstance();
		player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
		player.setCollidable(false);
		addVanishMetadata(player);
		INametagApi api = plugin.getNameTagApi();
		Consumer<? super Set<OlympaPlayer>> hide = playerPerm -> {
			api.callNametagUpdate(olympaPlayer, playerPerm);
			playerPerm.forEach(staff -> ((Player) staff.getPlayer()).showPlayer(plugin, player));
		};
		Consumer<? super Set<OlympaPlayer>> show = noPerm -> noPerm.forEach(noStaff -> {
			((Player) noStaff.getPlayer()).hidePlayer(plugin, player);
			api.callNametagUpdate(olympaPlayer, noPerm);
		});
		if (OlympaAPIPermissionsSpigot.VANISH_SEE_ADMIN.hasPermission(olympaPlayer))
			OlympaAPIPermissionsSpigot.VANISH_SEE_ADMIN.getOlympaPlayers(hide, show);
		else
			OlympaAPIPermissionsSpigot.VANISH_COMMAND.getOlympaPlayers(hide, show);
		if (showMessage)
			Prefix.DEFAULT_GOOD.sendMessage(player, "Tu es d??sormais en vanish.");
		olympaPlayer.setVanish(true);
		consumerRegister.values().forEach(consumer -> consumer.accept(player, olympaPlayer, true));
	}

	//	@Override
	//	public void enable(OlympaPlayer olympaPlayer, boolean showMessage) {
	//		Player player = (Player) olympaPlayer.getPlayer();
	//		OlympaCore plugin = OlympaCore.getInstance();
	//		player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
	//		player.setCollidable(false);
	//		addVanishMetadata(player);
	//		INametagApi api = plugin.getNameTagApi();
	//		//		new OlympaSpigotPermission(olympaPlayer.getGroup()).getPlayers(null, noPerm -> noPerm.forEach(noAdmin -> noAdmin.hidePlayer(plugin, player)));
	//		//		OlympaAPIPermissions.VANISH_SEE.getOlympaPlayers(playerPerm -> {
	//		new OlympaSpigotPermission(olympaPlayer.getGroup()).getOlympaPlayers(playerPerm -> {
	//			api.callNametagUpdate(olympaPlayer, playerPerm);
	//			((Player) olympaPlayer.getPlayer()).showPlayer(plugin, player);
	//		}, noPerm -> noPerm.forEach(noStaff -> ((Player) noStaff.getPlayer()).hidePlayer(plugin, player)));
	//		if (showMessage)
	//			Prefix.DEFAULT_GOOD.sendMessage(player, "Tu es d??sormais en vanish.");
	//	}

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
