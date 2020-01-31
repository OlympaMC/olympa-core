package fr.olympa.api.provider;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.gson.Gson;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.objects.Gender;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.Passwords;
import fr.olympa.api.utils.Utils;

public class OlympaPlayerObject implements OlympaPlayer, Cloneable {

	long id;
	UUID uuid;
	UUID premiumUuid;
	Gender gender = Gender.MALE;
	String password;

	String email;

	String name;
	TreeMap<OlympaGroup, Long> groups = new TreeMap<>(Comparator.comparing(OlympaGroup::getPower).reversed());
	String ip;
	long firstConnection;
	long lastConnection;
	TreeMap<Long, String> histName = new TreeMap<>(Comparator.comparing(Long::longValue).reversed());
	TreeMap<Long, String> histIp = new TreeMap<>(Comparator.comparing(Long::longValue).reversed());
	Map<String, String> data = new HashMap<>();

	double money;

	boolean vanish;

	boolean verifMode;

	boolean afk;

	public OlympaPlayerObject(UUID uuid, String name, String ip) {
		this.uuid = uuid;
		this.name = name;
		this.ip = ip;
		this.groups.put(OlympaGroup.PLAYER, 0l);
		this.firstConnection = Utils.getCurrentTimeInSeconds();
		this.lastConnection = Utils.getCurrentTimeInSeconds();
	}

	@Override
	public void loadSavedDatas(long id, UUID premiumUuid, String groupsString, long firstConnection, long lastConnection, String password, String email, Gender gender, String histNameJson, String histIpJson) {
		this.id = id;
		this.premiumUuid = premiumUuid;
		for (String groupInfos : groupsString.split(";")) {
			String[] groupInfo = groupInfos.split(":");
			OlympaGroup olympaGroup = OlympaGroup.getById(Integer.parseInt(groupInfo[0]));
			Long until;
			if (groupInfo.length > 1) {
				until = Long.parseLong(groupInfo[1]);
			} else {
				until = 0l;
			}
			this.groups.put(olympaGroup, until);
		}
		this.firstConnection = firstConnection;
		this.lastConnection = lastConnection;
		this.password = password;
		this.email = email;
		this.gender = gender;
		if (histNameJson != null && !histNameJson.isEmpty()) {
			Map<Long, String> histName2 = new Gson().fromJson(histNameJson, Map.class);
			this.histName.putAll(histName2);
		}
		if (histIpJson != null && !histIpJson.isEmpty()) {
			Map<Long, String> histIps = new Gson().fromJson(histIpJson, Map.class);
			this.histIp.putAll(histIps);
		}
	}

	@Override
	public void addGroup(OlympaGroup group) {
		this.addGroup(group, 0l);
	}

	@Override
	public void addGroup(OlympaGroup group, long time) {
		this.groups.put(group, time);
		if (this.groups.size() > 1 && this.groups.containsKey(OlympaGroup.PLAYER)) {
			this.removeGroup(OlympaGroup.PLAYER);
		}
	}

	@Override
	public void addNewIp(String ip) {
		this.histIp.put(Utils.getCurrentTimeInSeconds(), this.ip);
		this.ip = ip;
		//MySQL.savePlayer(this); TODO je sais pas si c'est nécessaire, il y a ça dans #addNewName
	}

	@Override
	public void addMoney(double money) {
		this.money += money;
	}

	@Override
	public void addNewName(String name) {
		this.histName.put(Utils.getCurrentTimeInSeconds(), this.name);
		this.name = name;
		MySQL.savePlayer(this);
	}

	@Override
	public OlympaPlayer clone() {
		try {
			return (OlympaPlayer) super.clone();
		} catch (final CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getEmail() {
		return this.email;
	}

	@Override
	public long getFirstConnection() {
		return this.firstConnection;
	}

	@Override
	public Gender getGender() {
		return this.gender;
	}

	@Override
	public OlympaGroup getGroup() {
		return this.groups.firstKey();
	}

	@Override
	public TreeMap<OlympaGroup, Long> getGroups() {
		return this.groups;
	}

	@Override
	public String getGroupsToHumainString() {
		return this.groups.entrySet().stream().map(entry -> {
			String time = new String();
			if (entry.getValue() != 0) {
				time = " (" + Utils.timestampToDateAndHour(entry.getValue()) + ")";
			}
			return entry.getKey().getName() + time;
		}).collect(Collectors.joining(", "));
	}

	@Override
	public String getGroupsToString() {
		return this.groups.entrySet().stream().map(entry -> entry.getKey().getId() + (entry.getValue() != 0 ? ":" + entry.getValue() : "")).collect(Collectors.joining(";"));
	}

	@Override
	public TreeMap<Long, String> getHistHame() {
		return this.histName;
	}

	@Override
	public TreeMap<Long, String> getHistIp() {
		return this.histIp;
	}

	@Override
	public long getId() {
		return this.id;
	}

	@Override
	public String getIp() {
		return this.ip;
	}

	@Override
	public long getLastConnection() {
		return this.lastConnection;
	}

	@Override
	public double getMoney() {
		return this.money;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public Player getPlayer() {
		return Bukkit.getPlayer(this.uuid);
	}

	@Override
	public UUID getPremiumUniqueId() {
		return this.premiumUuid;
	}

	@Override
	public UUID getUniqueId() {
		return this.uuid;
	}

	@Override
	public void giveMoney(double money) {
		this.money += money;
	}

	public String hashPassword(String password_toHash) {
		return Passwords.getSHA512(password_toHash, "DYhG9guiRVoUubWwvn2G0Fg3b0qyJfIxfs2aC9mi".getBytes());
	}

	@Override
	public boolean hasMoney(double money) {
		return this.money >= money;
	}

	@Override
	public boolean hasPermission(OlympaPermission permission) {
		return this.groups.keySet().stream().filter(group -> group.getPower() >= permission.getGroup().getPower()).findFirst().isPresent();
	}

	@Override
	public boolean isAfk() {
		return this.afk;
	}

	@Override
	public boolean isSamePassword(String password) {
		password = this.hashPassword(password);
		return this.password.equals(password);
	}

	@Override
	public boolean isVanish() {
		return this.vanish;
	}

	@Override
	public boolean isVerifMode() {
		return this.verifMode;
	}

	public void putData(String key, String value) {
		this.data.put(key, value);
	}

	public void removeData(String key) {
		this.data.remove(key);
	}

	private void removeGroup(OlympaGroup group) {
		this.groups.remove(group);
	}

	@Override
	public void setId(long id) {
		this.id = id;
	}

	@Override
	public void setAfk(boolean afk) {
		this.afk = afk;
	}

	@Override
	public void setGender(Gender gender) {
		this.gender = gender;
	}

	@Override
	public void setGroup(OlympaGroup group) {
		this.setGroup(group, 0l);
	}

	@Override
	public void setGroup(OlympaGroup group, long time) {
		this.groups.clear();
		this.addGroup(group, time);
	}

	@Override
	public void setIp(String ip) {
		this.ip = ip;
	}

	@Override
	public void setLastConnection(long lastConnection) {
		this.lastConnection = lastConnection;
	}

	@Override
	public void setMoney(double money) {
		this.money = money;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setPassword(String password) {
		this.password = this.hashPassword(password);
	}

	@Override
	public void setPremiumUniqueId(UUID premium_uuid) {
		this.premiumUuid = premium_uuid;
	}

	@Override
	public void setVanish(boolean vanish) {
		this.vanish = vanish;
	}

	@Override
	public void setVerifMode(boolean verifMode) {
		this.verifMode = verifMode;
	}

	@Override
	public boolean withdrawMoney(double money) {
		if (this.money >= money) {
			this.money -= money;
			return true;
		} else {
			return false;
		}
	}

}
