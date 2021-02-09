package fr.olympa.core.spigot.groups;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.OlympaItemBuild;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.sort.Sorting;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.Utils;

public class StaffGui extends OlympaGUI {

	public StaffGui() throws SQLException {
		super("Staff", 5);
		//		List<OlympaPlayer> staff = MySQL.getPlayersByGroupsIds(OlympaGroup.getStaffGroups()).stream().sorted((o1, o2) -> o2.getGroup().getPower() - o1.getGroup().getPower()).collect(Collectors.toList());
		List<OlympaPlayer> staff = MySQL.getPlayersByGroupsIds(OlympaGroup.getStaffGroups()).stream().sorted(new Sorting<OlympaPlayer>(op -> op.getGroup().getPower())).collect(Collectors.toList());
		int i = 0;
		for (OlympaPlayer s : staff)
			inv.setItem(i++, new OlympaItemBuild(s.getGroup().getPrefix(s.getGender()) + s.getName()).skullowner(s.getName()).addLore(
					"", "&7" + s.getGroupsToHumainString(),
					"&8Dernière connexion " + Utils.timestampToDuration(s.getLastConnection()),
					"&7Premium " + (s.getPremiumUniqueId() != null ? "Oui" : "Non")).build());
	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		return true;
	}
}
