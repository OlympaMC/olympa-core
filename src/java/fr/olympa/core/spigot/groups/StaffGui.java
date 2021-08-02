package fr.olympa.core.spigot.groups;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.sort.Sorting;
import fr.olympa.api.spigot.gui.OlympaGUI;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.common.provider.AccountProvider;

public class StaffGui extends OlympaGUI {

	private static final Sorting<OlympaPlayer> SORT_STAFF;

	static {
		LinkedHashMap<ToLongFunction<OlympaPlayer>, Boolean> map = new LinkedHashMap<>();
		map.put(op -> (long) op.getGroup().getPower(), false);
		map.put(op -> op.getLastConnection(), false);
		SORT_STAFF = new Sorting<>(map);

	}

	public StaffGui() throws SQLException {
		super("Staff", 5);
		//		List<OlympaPlayer> staff = MySQL.getPlayersByGroupsIds(OlympaGroup.getStaffGroups()).stream().sorted((o1, o2) -> o2.getGroup().getPower() - o1.getGroup().getPower()).collect(Collectors.toList());
		List<OlympaPlayer> staff = AccountProvider.getter().getSQL().getPlayersByGroupsIds(OlympaGroup.getStaffGroups()).stream().sorted(SORT_STAFF).collect(Collectors.toList());
		int i = 0;
		for (OlympaPlayer s : staff) {
			int i2 = i;
			ItemUtils.skull(x -> inv.setItem(i2, x), s.getGroup().getPrefix(s.getGender()) + s.getName(), s.getName(),
					"", "§7" + s.getGroupsToHumainString(),
					s.isConnected() ? "§8Connecté" + s.getTuneChar() : "§8Dernière connexion " + Utils.timestampToDuration(s.getLastConnection()),
					"§7Premium " + (s.getPremiumUniqueId() != null ? "Oui" : "Non"));
			i++;
		}

	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		return true;
	}
}
