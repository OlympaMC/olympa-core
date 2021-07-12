package fr.olympa.api.common.provider;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.google.gson.annotations.Expose;

import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.permission.OlympaPermission;
import fr.olympa.api.common.player.Gender;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.login.Passwords;
import fr.olympa.core.common.utils.GsonCustomizedObjectTypeAdapter;

@SuppressWarnings("unchecked")
public class OlympaPlayerObject extends OlympaPlayerCore {

	@Expose
	public UUID premiumUuid;
	@Expose
	public String password;
	@Expose
	public String email;
	@Expose
	public Map<String, OlympaServer> customPermissions = new HashMap<>();
	@Expose
	public Map<Long, String> histIp = new TreeMap<>(Comparator.comparing(Long::longValue).reversed());

	public OlympaPlayerObject(UUID uuid, String name, String ip) {
		super(uuid, name, ip);
		firstConnection = Utils.getCurrentTimeInSeconds();
		lastConnection = Utils.getCurrentTimeInSeconds();
	}

	public void updateGroups() {
		COLUMN_GROUPS.updateAsync(this, getGroupsToString(), null, null);
	}

	@Override
	public void removeGroup(OlympaGroup group) {
		super.removeGroup(group);
		updateGroups();
	}

	@Override
	public void addGroup(OlympaGroup group, long time) {
		super.addGroup(group, time);
		updateGroups();
	}

	@Override
	public void addNewIp(String ip) {
		histIp.put(Utils.getCurrentTimeInSeconds(), getIp());
		COLUMN_IP_HISTORY.updateAsync(this, GsonCustomizedObjectTypeAdapter.GSON.toJson(histIp), null, null);
		setIp(ip);
	}

	@Override
	public void addNewName(String name) {
		histName.put(Utils.getCurrentTimeInSeconds(), getName());
		COLUMN_NAME_HISTORY.updateAsync(this, GsonCustomizedObjectTypeAdapter.GSON.toJson(histName), null, null);
		setName(name);
	}

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public boolean hasCustomPermission(String per, OlympaServer serv) {
		return customPermissions.entrySet().stream().anyMatch(e -> e.getKey().equals(per) && (e.getValue() == null || e.getValue().equals(serv)));
	}

	@Override
	public Map<String, OlympaServer> getCustomPermissions() {
		return Collections.unmodifiableMap(customPermissions);
	}

	public void addCustomPermission(OlympaPermission perm, OlympaServer serv) {
		if (serv == null)
			serv = OlympaServer.ALL;
		customPermissions.put(perm.getName(), serv);
		COLUMN_CUSTOM_PERMISSIONS.updateAsync(this, GsonCustomizedObjectTypeAdapter.GSON.toJson(customPermissions), null, null);
	}

	public void removeCustomPermission(OlympaPermission perm, OlympaServer serv) {
		if (serv != null)
			customPermissions.remove(perm.getName(), serv);
		else
			customPermissions.remove(perm.getName());
		COLUMN_CUSTOM_PERMISSIONS.updateAsync(this, GsonCustomizedObjectTypeAdapter.GSON.toJson(customPermissions), null, null);
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public UUID getPremiumUniqueId() {
		return premiumUuid;
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
	public void loadDatas(ResultSet resultSet) throws SQLException {
		// has to be overriden
	}

	@Override
	public void loadSavedDatas(long id, UUID premiumUuid, String groupsString, long firstConnection, long lastConnection, String password, String email, Gender gender, String histNameJson, String histIpJson, int teamspeakId, boolean vanish) {
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
	public void setEmail(String email) {
		this.email = email;
		COLUMN_EMAIL.updateAsync(this, email, null, null);
	}

	@Override
	public void setGender(Gender gender) {
		super.setGender(gender);
		COLUMN_GENDER.updateAsync(this, gender.ordinal(), null, null);
	}

	@Override
	public void setIp(String ip) {
		super.setIp(ip);
		COLUMN_IP.updateAsync(this, ip, null, null);
	}

	@Override
	public void setLastConnection(long lastConnection) {
		super.setLastConnection(lastConnection);
		COLUMN_LAST_CONNECTION.updateAsync(this, new Timestamp(lastConnection * 1000), null, null);
	}

	@Override
	public void setName(String name) {
		super.setName(name);
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
		super.setTeamspeakId(teamspeakId);
		COLUMN_TS3_ID.updateAsync(this, teamspeakId, null, null);
	}

	@Override
	public void setVanish(boolean vanish) {
		super.setVanish(vanish);
		COLUMN_VANISH.updateAsync(this, vanish, null, null);
	}

	@Override
	public Map<Long, String> getHistIp() {
		return histIp;
	}
}
