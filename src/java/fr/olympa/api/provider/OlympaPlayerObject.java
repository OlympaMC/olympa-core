package fr.olympa.api.provider;

import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.objects.Gender;
import fr.olympa.api.objects.OlympaMoney;
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
	//Map<String, String> data = new HashMap<>();
	OlympaMoney storeMoney; // TODO
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

		// java.lang.ClassCastException: class java.lang.String cannot be cast to class java.lang.Long (java.lang.String and java.lang.Long are in module java.base of loader 'bootstrap')
		// fr.olympa.api.provider.OlympaPlayerObject.lambda$4(OlympaPlayerObject.java:75)
		if (false) {
			if (histNameJson != null && !histNameJson.isEmpty()) {
				TreeMap<Long, String> histName2 = new Gson().fromJson(histNameJson, TreeMap.class);
				// this line
				histName2.entrySet().stream().forEach(entry -> this.histName.put(entry.getKey(), entry.getValue()));
			}
			if (histIpJson != null && !histIpJson.isEmpty()) {
				TreeMap<Long, String> histIps = new Gson().fromJson(histIpJson, TreeMap.class);
				// this line
				histIps.entrySet().stream().forEach(entry -> this.histIp.put(entry.getKey(), entry.getValue()));
			}
		}
		/*if (dataJson != null && !dataJson.isEmpty()) {
			this.data = new Gson().fromJson(dataJson, HashMap.class);
		}*/
	}

	public void loadDatas(ResultSet resultSet) throws SQLException {
		// has to be override
	}

	public void saveDatas(PreparedStatement statement) throws SQLException {
		// has to be override
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
	}

	@Override
	public void addNewName(String name) {
		this.histName.put(Utils.getCurrentTimeInSeconds(), this.name);
		this.name = name;
		// to prevent bug if other player use the old name
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
	public OlympaMoney getStoreMoney() {
		return this.storeMoney;
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

	public String hashPassword(String password_toHash) {
		return Passwords.getSHA512(password_toHash, "DYhG9guiRVoUubWwvn2G0Fg3b0qyJfIxfs2aC9mi".getBytes());
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
	public boolean isPremium() {
		return premiumUuid != null;
	}

	public static class OlympaPlayerDeserializer implements JsonDeserializer<OlympaPlayer> {

		@Override
		public OlympaPlayerObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject object = json.getAsJsonObject();
			OlympaPlayerObject player = (OlympaPlayerObject) AccountProvider.playerProvider.create(context.deserialize(object.get("uuid"), UUID.class), object.get("name").getAsString(), null);
			if (object.has("afk")) player.afk = object.get("afk").getAsBoolean();
			if (object.has("email")) player.email = object.get("email").getAsString();
			if (object.has("firstConnection")) player.firstConnection = object.get("firstConnection").getAsLong();
			if (object.has("gender")) player.gender = context.deserialize(object.get("gender"), Gender.class);
			if (object.has("groups")) player.groups = context.deserialize(object.get("groups"), TreeMap.class); // TODO deserialize
			if (object.has("histIp")) player.histIp = context.deserialize(object.get("histIp"), TreeMap.class);
			if (object.has("histName")) player.histName = context.deserialize(object.get("histName"), TreeMap.class);
			if (object.has("id")) player.id = object.get("id").getAsLong();
			if (object.has("lastConnection")) player.lastConnection = object.get("lastConnection").getAsLong();
			if (object.has("password")) player.password = object.get("password").getAsString();
			if (object.has("premiumUuid")) player.premiumUuid = context.deserialize(object.get("premiumUuid"), UUID.class);
			if (object.has("storeMoney")) player.storeMoney = context.deserialize(object.get("storeMoney"), OlympaMoney.class);
			if (object.has("vanish")) player.vanish = object.get("vanish").getAsBoolean();
			if (object.has("verifMode")) player.verifMode = object.get("verifMode").getAsBoolean();
			return player;
		}

	}

}
