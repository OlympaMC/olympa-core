package fr.tristiisch.olympa.api.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.tristiisch.olympa.api.item.OlympaItemBuild;
import fr.tristiisch.olympa.api.task.TaskManager;
import fr.tristiisch.olympa.api.utils.SpigotUtils;

public class OlympaGuiBuild {

	public static class GuiData {

		private String data;
		private String id;
		private UUID playerUuid;

		public GuiData(String id, UUID playerUuid) {
			this.id = id;
			this.playerUuid = playerUuid;
			this.data = new String();
		}

		public GuiData(String id, UUID playerUuid, String data) {
			this.id = id;
			this.playerUuid = playerUuid;
			this.data = data;
		}

		public String getData() {
			return this.data;
		}

		public String getId() {
			return this.id;
		}

		public UUID getPlayerUniqueId() {
			return this.playerUuid;
		}

		public void setData(String data) {
			this.data = data;

		}
	}

	public static OlympaItemBuild cancelItemBuild = new OlympaItemBuild(Material.REDSTONE_BLOCK, "&4✖ &lImpossible");
	private static int columns = 9;
	private static List<GuiData> players = new ArrayList<>();

	public static void cancelInDev(InventoryClickEvent event) {
		cancelItem(event, "En développement");
	}

	public static void cancelItem(InventoryClickEvent event, String msg) {
		event.setCancelled(true);
		Player player = (Player) event.getWhoClicked();
		ItemStack item = event.getCurrentItem();
		Inventory clickedInventory = event.getClickedInventory();

		event.getClickedInventory().setItem(event.getSlot(), cancelItemBuild.lore("", "&c" + msg, "").build());
		player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);

		TaskManager.runTaskLater(player.getUniqueId() + String.valueOf(event.getSlot()), () -> {
			if (clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
				clickedInventory.setItem(event.getSlot(), item);
			}
		}, 30);
	}

	public static GuiData getGuiData(Player player) {
		return getGuiData(player.getUniqueId());
	}

	public static GuiData getGuiData(UUID uuid) {
		return players.stream().filter(data -> uuid == data.getPlayerUniqueId()).findFirst().orElse(null);
	}

	public static void removeGui(GuiData guiData) {
		players.remove(guiData);
	}

	public static void removeGui(Player player) {
		removeGui(getGuiData(player));

	}

	public static void setGuiData(GuiData guiData) {
		GuiData guiDataOld = getGuiData(guiData.getPlayerUniqueId());
		if (guiDataOld != null) {
			removeGui(guiDataOld);
		}
		players.add(guiData);
	}

	private String data;

	private String id;

	private Inventory inventory;

	public OlympaGuiBuild(String name, String id, int size) {
		if (size % columns != 0) {
			size = size + columns - size % columns;
		}
		this.inventory = Bukkit.createInventory(null, size, SpigotUtils.color(name));
		this.id = id;
		this.data = "";
	}

	public OlympaGuiBuild(String name, String id, int size, String data) {
		if (size % columns != 0) {
			size = size + columns - size % columns;
		}
		this.inventory = Bukkit.createInventory(null, size, SpigotUtils.color(name));
		this.id = id;
		this.data = data;
	}

	public OlympaGuiBuild(String name, String id, InventoryType type) {
		this.inventory = Bukkit.createInventory(null, type, SpigotUtils.color(name));
		this.id = id;
		this.data = "";
	}

	public OlympaGuiBuild(String name, String id, long column) {
		int size = (int) (columns * column);
		this.inventory = Bukkit.createInventory(null, size, SpigotUtils.color(name));
		this.id = id;
		this.data = "";
	}

	public void addItem(ItemStack item) {
		int index = IntStream.range(0, this.inventory.getContents().length).filter(i -> this.inventory.getContents()[i] == null).findFirst().orElse(-1);
		this.inventory.setItem(index, item);
	}

	public int getColumn() {
		return columns;
	}

	public int getColumn(int i) {
		return this.getSlot(1, i);
	}

	public String getData() {
		return this.data;
	}

	public int getFirstSlot() {
		return 0;
	}

	public Inventory getInventory() {
		return this.inventory;
	}

	public ItemStack getItem(int index) {
		return this.inventory.getItem(index);
	}

	public int getLastSlot() {
		return this.getSize() - 1;
	}

	public int getMiddleColumn() {
		return (columns - 1) * 2;
	}

	public int getMiddleLigne(int i) {
		return this.getColumn(1) / 2 * i;
	}

	public int getMiddleSlot() {
		return this.getSize() / 2;
	}

	public int getMiddleSlotPlusColumn(int i) {
		return this.getMiddleSlot() + i * 9;
	}

	public String getName() {
		return this.inventory.getName();
	}

	public int getSize() {
		return this.inventory.getSize();
	}

	// TODO à verif
	public int getSlot(int ligne, int column) {
		return columns * (ligne - 1) + 1 + column;
	}

	public void openInventory(Player player) {
		player.openInventory(this.inventory);
		if (this.id != null) {
			setGuiData(new GuiData(this.id, player.getUniqueId(), this.data));
		}
	}

	public void openInventory(Player... players) {
		for (Player player : players) {
			this.openInventory(player);
		}
	}

	public void removeItem(int index) {
		this.inventory.setItem(index, null);
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setItem(int index, ItemStack item) {
		this.inventory.setItem(index, item);
	}

	public void setItem(ItemStack[] items) {
		this.inventory.setContents(items);
	}
}
