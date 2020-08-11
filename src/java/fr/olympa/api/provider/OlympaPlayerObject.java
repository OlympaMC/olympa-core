package fr.olympa.api.provider;

import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.player.Gender;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.GsonCustomizedObjectTypeAdapter;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.login.Passwords;

@SuppressWarnings("unchecked") 
public class OlympaPlayerObject implements OlympaPlayer, Cloneable {

	public static class OlympaPlayerDeserializer implements JsonDeserializer<OlympaPlayer> {

		@Override
		public OlympaPlayerObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject object = json.getAsJsonObject();
			OlympaPlayerObject player = (OlympaPlayerObject) AccountProvider.playerProvider.create(context.deserialize(object.get("uuid"), UUID.class), object.get("name").getAsString(), object.get("ip").getAsString());
			if (object.has("afk")) {
				player.afk = object.get("afk").getAsBoolean();
			}
			if (object.has("email")) {
				player.email = object.get("email").getAsString();
			}
			if (object.has("firstConnection")) {
				player.firstConnection = object.get("firstConnection").getAsLong();
			}
			if (object.has("gender")) {
				player.gender = context.deserialize(object.get("gender"), Gender.class);
			}
			if (object.has("groups")) {
				((Map<String, Long>) context.deserialize(object.get("groups"), Map.class)).forEach((name, time) -> player.groups.put(OlympaGroup.valueOf(name), time));
			}
			if (object.has("histIp")) {
				player.histIp = context.deserialize(object.get("histIp"), TreeMap.class);
			}
			if (object.has("histName")) {
				player.histName = context.deserialize(object.get("histName"), TreeMap.class);
			}
			if (object.has("id")) {
				player.id = object.get("id").getAsLong();
			}
			if (object.has("lastConnection")) {
				player.lastConnection = object.get("lastConnection").getAsLong();
			}
			if (object.has("password")) {
				player.password = object.get("password").getAsString();
			}
			if (object.has("premiumUuid")) {
				player.premiumUuid = context.deserialize(object.get("premiumUuid"), UUID.class);
			}
			if (object.has("vanish")) {
				player.vanish = object.get("vanish").getAsBoolean();
			}
			if (object.has("verifMode")) {
				player.verifMode = object.get("verifMode").getAsBoolean();
			}
			if (object.has("teamspeakId")) {
				player.teamspeakId = object.get("teamspeakId").getAsLong();
			}
			return player;
		}

	}

	@Expose
	long id;
	@Expose
	UUID uuid;
	@Expose
	UUID premiumUuid;
	@Expose
	Gender gender = Gender.MALE;
	@Expose
	String password;
	@Expose
	String email;
	@Expose
	String name;
	@Expose
	TreeMap<OlympaGroup, Long> groups = new TreeMap<>(Comparator.comparing(OlympaGroup::getPower).reversed());
	@Expose
	String ip;
	@Expose
	long firstConnection;
	@Expose
	long lastConnection;
	@Expose
	TreeMap<Long, String> histName = new TreeMap<>(Comparator.comparing(Long::longValue).reversed());
	@Expose
	TreeMap<Long, String> histIp = new TreeMap<>(Comparator.comparing(Long::longValue).reversed());
	@Expose
	boolean vanish;
	@Expose
	boolean verifMode;
	@Expose
	boolean afk;
	@Expose
	Boolean connected;
	@Expose
	private long teamspeakId;
	private Object cachedPlayer = null;
	private OlympaPlayerInformations cachedInformations = null;

	public OlympaPlayerObject(UUID uuid, String name, String ip) {
		this.uuid = uuid;
		this.name = name;
		this.ip = ip;
		firstConnection = Utils.getCurrentTimeInSeconds();
		lastConnection = Utils.getCurrentTimeInSeconds();
	}

	@Override
	public void addGroup(OlympaGroup group) {
		this.addGroup(group, 0l);
	}

	@Override
	public void addGroup(OlympaGroup group, long time) {
		groups.put(group, time);
		if (groups.size() > 1 && groups.containsKey(OlympaGroup.PLAYER)) {
			removeGroup(OlympaGroup.PLAYER);
		}
	}

	@Override
	public void addNewIp(String ip) {
		histIp.put(Utils.getCurrentTimeInSeconds(), this.ip);
		this.ip = ip;
	}

	@Override
	public void addNewName(String name) {
		histName.put(Utils.getCurrentTimeInSeconds(), this.name);
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
		return email;
	}

	@Override
	public long getFirstConnection() {
		return firstConnection;
	}

	@Override
	public Gender getGender() {
		return gender;
	}

	@Override
	public OlympaGroup getGroup() {
		return groups.isEmpty() ? OlympaGroup.PLAYER : groups.firstKey();
	}

	@Override
	public String getGroupName() {
		return getGroup().getName(gender);
	}

	@Override
	public String getGroupNameColored() {
		return getGroup().getColor() + getGroupName();
	}

	@Override
	public String getGroupPrefix() {
		return getGroup().getPrefix(gender);
	}

	@Override
	public TreeMap<OlympaGroup, Long> getGroups() {
		return groups;
	}

	@Override
	public String getGroupsToHumainString() {
		return groups.entrySet().stream().map(entry -> {
			String time = new String();
			if (entry.getValue() != 0) {
				time = " (" + Utils.timestampToDateAndHour(entry.getValue()) + ")";
			}
			return entry.getKey().getName(gender) + time;
		}).collect(Collectors.joining(", "));
	}

	@Override
	public String getGroupsToString() {
		return groups.entrySet().stream().map(entry -> entry.getKey().getId() + (entry.getValue() != 0 ? ":" + entry.getValue() : "")).collect(Collectors.joining(";"));
	}

	@Override
	public TreeMap<Long, String> getHistHame() {
		return histName;
	}

	@Override
	public TreeMap<Long, String> getHistIp() {
		return histIp;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public OlympaPlayerInformations getInformation() {
		if (cachedInformations == null) {
			cachedInformations = AccountProvider.getPlayerInformations(this);
		}
		return cachedInformations;
	}

	@Override
	public String getIp() {
		return ip;
	}

	@Override
	public long getLastConnection() {
		return lastConnection;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public Player getPlayer() {
		if (cachedPlayer == null) {
			cachedPlayer = Bukkit.getPlayer(uuid);
		}
		return (Player) cachedPlayer;
	}

	@Override
	public UUID getPremiumUniqueId() {
		return premiumUuid;
	}

	@Override
	public long getTeamspeakId() {
		return teamspeakId;
	}

	@Override
	public String getTuneChar() {
		return gender.getTurne();
	}

	@Override
	public UUID getUniqueId() {
		return uuid;
	}

	@Override
	public boolean isAfk() {
		return afk;
	}

	@Override
	public boolean isConnected() {
		return connected != null ? connected : false;
	}

	@Override
	public boolean isGenderFemale() {
		return gender == Gender.FEMALE;
	}

	@Override
	public boolean isPremium() {
		return premiumUuid != null;
	}

	@Override
	public boolean isSamePassword(String password) {
		return this.password.equals(Passwords.getSHA512(password));
	}

	@Override
	public boolean isVanish() {
		return vanish;
	}

	@Override
	public boolean isVerifMode() {
		return verifMode;
	}

	@Override
	public void loadDatas(ResultSet resultSet) throws SQLException {
		// has to be override
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
			groups.put(olympaGroup, until);
		}
		this.firstConnection = firstConnection;
		this.lastConnection = lastConnection;
		this.password = password;
		this.email = email;
		this.gender = gender;

		if (histNameJson != null && !histNameJson.isEmpty()) {
			Map<Long, String> histName2 = GsonCustomizedObjectTypeAdapter.GSON.fromJson(histNameJson, Map.class);
			histName2.entrySet().stream().forEach(entry -> histName.put(entry.getKey(), entry.getValue()));
		}
		if (histIpJson != null && !histIpJson.isEmpty()) {
			Map<Long, String> histIps = GsonCustomizedObjectTypeAdapter.GSON.fromJson(histIpJson, Map.class);
			histIps.entrySet().stream().forEach(entry -> histIp.put(entry.getKey(), entry.getValue()));
		}
	}

	@Override
	public void removeGroup(OlympaGroup group) {
		groups.remove(group);
	}

	@Override
	public void saveDatas(PreparedStatement statement) throws SQLException {
		// has to be override
	}

	@Override
	public void setAfk(boolean afk) {
		this.afk = afk;
	}

	@Override
	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	@Override
	public void setEmail(String email) {
		this.email = email;
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
		groups.clear();
		this.addGroup(group, time);
	}

	@Override
	public void setId(long id) {
		this.id = id;
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
		this.password = Passwords.getSHA512(password);
	}

	@Override
	public void setPremiumUniqueId(UUID premium_uuid) {
		premiumUuid = premium_uuid;
	}

	@Override
	public void setTeamspeakId(long teamspeakId) {
		this.teamspeakId = teamspeakId;
	}

	@Override
	public void setVanish(boolean vanish) {
		this.vanish = vanish;
	}

	@Override
	public void setVerifMode(boolean verifMode) {
		this.verifMode = verifMode;
	}

}
