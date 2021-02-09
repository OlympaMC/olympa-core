package fr.olympa.api.provider;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.player.Gender;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.server.OlympaServer;
import fr.olympa.api.sql.SQLColumn;
import fr.olympa.api.utils.GsonCustomizedObjectTypeAdapter;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.login.Passwords;

@SuppressWarnings("unchecked")
public class OlympaPlayerObject implements OlympaPlayer, Cloneable {

	public static class OlympaPlayerDeserializer implements JsonDeserializer<OlympaPlayer> {

		@Override
		public OlympaPlayerObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject object = json.getAsJsonObject();
			OlympaPlayerObject player = (OlympaPlayerObject) AccountProvider.pluginPlayerProvider.create(context.deserialize(object.get("uuid"), UUID.class), object.get("name").getAsString(), object.get("ip").getAsString());
			if (object.has("email"))
				player.email = object.get("email").getAsString();
			if (object.has("firstConnection"))
				player.firstConnection = object.get("firstConnection").getAsLong();
			if (object.has("gender"))
				player.gender = context.deserialize(object.get("gender"), Gender.class);
			if (object.has("groups"))
				((Map<String, Long>) context.deserialize(object.get("groups"), Map.class)).forEach((name, time) -> player.groups.put(OlympaGroup.valueOf(name), time));
			if (object.has("histIp"))
				player.histIp = context.deserialize(object.get("histIp"), TreeMap.class);
			if (object.has("histName"))
				player.histName = context.deserialize(object.get("histName"), TreeMap.class);
			if (object.has("id"))
				player.id = object.get("id").getAsLong();
			if (object.has("lastConnection"))
				player.lastConnection = object.get("lastConnection").getAsLong();
			if (object.has("password"))
				player.password = object.get("password").getAsString();
			if (object.has("premiumUuid"))
				player.premiumUuid = context.deserialize(object.get("premiumUuid"), UUID.class);
			if (object.has("vanish"))
				player.vanish = object.get("vanish").getAsBoolean();
			//			if (object.has("discordOlympaId"))
			//				player.discordOlympaId = object.get("discordOlympaId").getAsInt();
			if (object.has("teamspeakId"))
				player.teamspeakId = object.get("teamspeakId").getAsInt();
			return player;
		}

	}

	private static final SQLColumn<OlympaPlayerObject> COLUMN_ID = new SQLColumn<OlympaPlayerObject>("id", "INT(20) unsigned NOT NULL AUTO_INCREMENT", Types.INTEGER).setPrimaryKey(OlympaPlayerObject::getId);
	private static final SQLColumn<OlympaPlayerObject> COLUMN_PSEUDO = new SQLColumn<OlympaPlayerObject>("pseudo", "VARCHAR(255) NOT NULL", Types.VARCHAR).setUpdatable();
	private static final SQLColumn<OlympaPlayerObject> COLUMN_UUID_SERVER = new SQLColumn<>("uuid_server", "VARCHAR(36) NOT NULL", Types.VARCHAR);
	private static final SQLColumn<OlympaPlayerObject> COLUMN_UUID_PREMIUM = new SQLColumn<OlympaPlayerObject>("uuid_premium", "VARCHAR(36) DEFAULT NULL", Types.VARCHAR).setUpdatable();
	private static final SQLColumn<OlympaPlayerObject> COLUMN_GROUPS = new SQLColumn<OlympaPlayerObject>("groups", "VARCHAR(45) DEFAULT '20'", Types.VARCHAR).setUpdatable().setNotDefault();
	private static final SQLColumn<OlympaPlayerObject> COLUMN_EMAIL = new SQLColumn<OlympaPlayerObject>("email", "VARCHAR(255) DEFAULT NULL", Types.VARCHAR).setUpdatable();
	private static final SQLColumn<OlympaPlayerObject> COLUMN_PASSWORD = new SQLColumn<OlympaPlayerObject>("password", "VARCHAR(512) DEFAULT NULL", Types.VARCHAR).setUpdatable().setNotDefault();
	private static final SQLColumn<OlympaPlayerObject> COLUMN_MONEY = new SQLColumn<>("money", "VARCHAR(20) DEFAULT '0'", Types.VARCHAR); // unused?
	private static final SQLColumn<OlympaPlayerObject> COLUMN_IP = new SQLColumn<OlympaPlayerObject>("ip", "VARCHAR(39) NOT NULL", Types.VARCHAR).setUpdatable();
	private static final SQLColumn<OlympaPlayerObject> COLUMN_CREATED = new SQLColumn<OlympaPlayerObject>("created", "DATE NOT NULL", Types.DATE).setUpdatable().setNotDefault();
	private static final SQLColumn<OlympaPlayerObject> COLUMN_LAST_CONNECTION = new SQLColumn<OlympaPlayerObject>("last_connection", "TIMESTAMP NULL DEFAULT current_timestamp()", Types.TIMESTAMP).setUpdatable().setNotDefault();
	private static final SQLColumn<OlympaPlayerObject> COLUMN_TS3_ID = new SQLColumn<OlympaPlayerObject>("ts3_id", "INT(11) DEFAULT NULL", Types.INTEGER).setUpdatable();
	//	private static final SQLColumn<OlympaPlayerObject> COLUMN_DISCORD_ID = new SQLColumn<OlympaPlayerObject>("discord_olympa_id", "INT(11) DEFAULT NULL", Types.INTEGER).setUpdatable();
	private static final SQLColumn<OlympaPlayerObject> COLUMN_NAME_HISTORY = new SQLColumn<OlympaPlayerObject>("name_history", "VARCHAR(10000) DEFAULT NULL", Types.VARCHAR).setUpdatable();
	private static final SQLColumn<OlympaPlayerObject> COLUMN_IP_HISTORY = new SQLColumn<OlympaPlayerObject>("ip_history", "VARCHAR(10000) DEFAULT NULL", Types.VARCHAR).setUpdatable();
	private static final SQLColumn<OlympaPlayerObject> COLUMN_GENDER = new SQLColumn<OlympaPlayerObject>("gender", "TINYINT(1) DEFAULT NULL", Types.TINYINT).setUpdatable();
	private static final SQLColumn<OlympaPlayerObject> COLUMN_VANISH = new SQLColumn<OlympaPlayerObject>("vanish", "TINYINT(1) DEFAULT 0", Types.TINYINT).setUpdatable();

	static final List<SQLColumn<OlympaPlayerObject>> COLUMNS = Arrays.asList(COLUMN_ID, COLUMN_PSEUDO, COLUMN_UUID_SERVER, COLUMN_UUID_PREMIUM, COLUMN_GROUPS, COLUMN_EMAIL, COLUMN_PASSWORD, COLUMN_MONEY, COLUMN_IP, COLUMN_CREATED,
			COLUMN_LAST_CONNECTION, COLUMN_TS3_ID/*, COLUMN_DISCORD_ID*/, COLUMN_NAME_HISTORY, COLUMN_IP_HISTORY, COLUMN_GENDER, COLUMN_VANISH);

	@Expose
	long id = -1;
	@Expose
	UUID uuid;
	@Expose
	UUID premiumUuid;
	@Expose
	Gender gender = Gender.UNSPECIFIED;
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
	Boolean connected;
	@Expose
	private int teamspeakId;
	//	@Expose
	//	private int discordOlympaId;

	private Object cachedPlayer = null;
	private OlympaPlayerInformations cachedInformations = null;

	public OlympaPlayerObject(UUID uuid, String name, String ip) {
		this.uuid = uuid;
		this.name = name;
		this.ip = ip;
		firstConnection = Utils.getCurrentTimeInSeconds();
		lastConnection = Utils.getCurrentTimeInSeconds();
	}

	public void updateGroups() {
		COLUMN_GROUPS.updateAsync(this, getGroupsToString(), null, null);
	}

	@Override
	public void addGroup(OlympaGroup group) {
		this.addGroup(group, 0l);
	}

	@Override
	public void addGroup(OlympaGroup group, long time) {
		groups.put(group, time);
		updateGroups();
	}

	@Override
	public void addNewIp(String ip) {
		histIp.put(Utils.getCurrentTimeInSeconds(), this.ip);
		COLUMN_IP_HISTORY.updateAsync(this, GsonCustomizedObjectTypeAdapter.GSON.toJson(histIp), null, null);
		setIp(ip);
	}

	@Override
	public void addNewName(String name) {
		histName.put(Utils.getCurrentTimeInSeconds(), this.name);
		COLUMN_NAME_HISTORY.updateAsync(this, GsonCustomizedObjectTypeAdapter.GSON.toJson(histName), null, null);
		setName(name);
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
		OlympaGroup olympaGroup = null;
		for (Iterator<OlympaGroup> iterator = groups.keySet().iterator(); iterator.hasNext();) {
			olympaGroup = iterator.next();
			if (!olympaGroup.isVisible())
				continue;
			if (OlympaServer.ALL.equals(olympaGroup.getServer()))
				break;
			if (Objects.equals(olympaGroup.getServer(), LinkSpigotBungee.Provider.link.getOlympaServer()))
				break;
		}
		return olympaGroup;
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
			if (entry.getValue() != 0)
				time = " (" + Utils.timestampToDuration(entry.getValue()) + ")";
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
		if (cachedInformations == null)
			cachedInformations = AccountProvider.getPlayerInformations(this);
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
		if (cachedPlayer == null)
			cachedPlayer = Bukkit.getPlayer(uuid);
		return (Player) cachedPlayer;
	}

	@Override
	public UUID getPremiumUniqueId() {
		return premiumUuid;
	}

	@Override
	public int getTeamspeakId() {
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
	public boolean isConnected() {
		return connected != null ? connected : false;
		//		return getPlayer() != null && getPlayer().isOnline();
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
		return this.password.equals(Passwords.getPBKDF2(password));
	}

	@Override
	public boolean isVanish() {
		return vanish;
	}

	@Override
	public void loadDatas(ResultSet resultSet) throws SQLException {
		// has to be override
	}

	@Override
	public void loadSavedDatas(long id, UUID premiumUuid, String groupsString, long firstConnection, long lastConnection, String password, String email, Gender gender, String histNameJson, String histIpJson/*, int discordOlympaId*/, int teamspeakId, boolean vanish) {
		this.id = id;
		this.premiumUuid = premiumUuid;
		for (String groupInfos : groupsString.split(";")) {
			String[] groupInfo = groupInfos.split(":");
			OlympaGroup olympaGroup = OlympaGroup.getById(Integer.parseInt(groupInfo[0]));
			Long until;
			if (groupInfo.length > 1)
				until = Long.parseLong(groupInfo[1]);
			else
				until = 0l;
			groups.put(olympaGroup, until);
		}
		this.firstConnection = firstConnection;
		this.lastConnection = lastConnection;
		this.password = password;
		this.email = email;
		this.gender = gender;
		//		this.discordOlympaId = discordOlympaId;
		this.teamspeakId = teamspeakId;
		this.vanish = vanish;

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
		updateGroups();
	}

	@Override
	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	@Override
	public void setEmail(String email) {
		this.email = email;
		COLUMN_EMAIL.updateAsync(this, email, null, null);
	}

	@Override
	public void setGender(Gender gender) {
		this.gender = gender;
		COLUMN_GENDER.updateAsync(this, gender.ordinal(), null, null);
	}

	@Override
	public void setGroup(OlympaGroup group) {
		setGroup(group, 0l);
	}

	@Override
	public void setGroup(OlympaGroup group, long time) {
		groups.clear();
		this.addGroup(group, time);
	}

	@Override
	public void setId(long id) {
		this.id = id; // TODO
	}

	@Override
	public void setIp(String ip) {
		this.ip = ip;
		COLUMN_IP.updateAsync(this, ip, null, null);
	}

	@Override
	public void setLastConnection(long lastConnection) {
		this.lastConnection = lastConnection;
		COLUMN_LAST_CONNECTION.updateAsync(this, new Timestamp(lastConnection * 1000), null, null);
	}

	@Override
	public void setName(String name) {
		this.name = name;
		COLUMN_PSEUDO.updateAsync(this, name, null, null);
	}

	@Override
	public void setPassword(String password) {
		this.password = Passwords.getPBKDF2(password);
		COLUMN_PASSWORD.updateAsync(this, this.password, null, null);
	}

	@Override
	public void setPremiumUniqueId(UUID premium_uuid) {
		premiumUuid = premium_uuid;
		COLUMN_UUID_PREMIUM.updateAsync(this, Utils.getUUIDString(premiumUuid), null, null);
	}

	@Override
	public void setTeamspeakId(int teamspeakId) {
		this.teamspeakId = teamspeakId;
		COLUMN_TS3_ID.updateAsync(this, teamspeakId, null, null);
	}

	@Override
	public void setVanish(boolean vanish) {
		this.vanish = vanish;
		COLUMN_VANISH.updateAsync(this, vanish, null, null);
	}

	//	@Override
	//	public int getDiscordOlympaId() {
	//		return discordOlympaId;
	//	}
	//
	//	@Override
	//	public void setDiscordOlympaId(int discordOlympaId) {
	//		this.discordOlympaId = discordOlympaId;
	//		COLUMN_DISCORD_ID.updateAsync(this, discordOlympaId, null, null);
	//	}
}
