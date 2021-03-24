package fr.olympa.core.spigot.groups;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.sort.Sorting;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.Utils;

public class StaffGui extends OlympaGUI {

	private static Sorting<OlympaPlayer> SORT_STAFF;
	{
		Map<ToLongFunction<OlympaPlayer>, Boolean> map = new HashMap<>();
		map.put(op -> (long) op.getGroup().getPower(), true);
		map.put(op -> op.getLastConnection(), true);
		SORT_STAFF = new Sorting<>(map);

	}

	public StaffGui() throws SQLException {
		super("Staff", 5);
		//		List<OlympaPlayer> staff = MySQL.getPlayersByGroupsIds(OlympaGroup.getStaffGroups()).stream().sorted((o1, o2) -> o2.getGroup().getPower() - o1.getGroup().getPower()).collect(Collectors.toList());
		List<OlympaPlayer> staff = MySQL.getPlayersByGroupsIds(OlympaGroup.getStaffGroups()).stream().sorted(SORT_STAFF).collect(Collectors.toList());
		int i = 0;
		for (OlympaPlayer s : staff) {
			int i2 = i;
			ItemUtils.skull(x -> inv.setItem(i2, x), s.getGroup().getPrefix(s.getGender()) + s.getName(), s.getName(),
					"", "&7" + s.getGroupsToHumainString(),
					"&8Dernière connexion " + Utils.timestampToDuration(s.getLastConnection()),
					"&7Premium " + (s.getPremiumUniqueId() != null ? "Oui" : "Non"));
			i++;
		}

	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		return true;
	}
}
