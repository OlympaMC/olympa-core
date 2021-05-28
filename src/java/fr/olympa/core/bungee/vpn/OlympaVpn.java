package fr.olympa.core.bungee.vpn;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.utils.Utils;

public class OlympaVpn {

	// 3 month
	private static long outdateTime = LinkSpigotBungee.upTime - 8035200;

	public static OlympaVpn fromJson(String json) {
		return new Gson().fromJson(json, OlympaVpn.class);
	}

	long id;
	String query;
	String status;
	String country;
	String city;
	String org;
	String as;
	Boolean mobile;
	Boolean proxy;
	Boolean hosting;
	boolean upWithDB = false;
	List<String> users;
	List<String> whitelistUsers;
	long time;
	long lastUpdate;

	public OlympaVpn(long id, String query, Boolean proxy, Boolean mobile, Boolean hosting, String usersString, String country, String city, String org, String as, String whitelist, Timestamp timestamp, Date date) {
		this.id = id;
		this.query = query;
		if (mobile)
			this.mobile = mobile;
		if (proxy)
			this.proxy = proxy;
		if (hosting)
			this.hosting = hosting;
		this.country = country;
		this.city = city;
		this.org = org;
		this.as = as;
		if (usersString != null && !usersString.isEmpty())
			users = Arrays.stream(usersString.split(";")).collect(Collectors.toList());
		if (whitelist != null && !whitelist.isEmpty())
			whitelistUsers = Arrays.stream(whitelist.split(";")).collect(Collectors.toList());
		lastUpdate = timestamp.getTime() / 1000L;
		if (date != null)
			time = date.getTime() / 1000L;
		else
			setTime();
	}

	public void addUser(String username) {
		if (username == null)
			return;
		if (users == null)
			users = new ArrayList<>();
		else if (users.contains(username))
			return;
		removeUpWithDb();
		users.add(username);
	}

	public void addUserWhitelist(String username) {
		if (whitelistUsers == null)
			whitelistUsers = new ArrayList<>();
		else if (whitelistUsers.contains(username))
			return;
		removeUpWithDb();
		whitelistUsers.add(username);
	}

	public String getAs() {
		return as;
	}

	public String getCity() {
		return city;
	}

	public String getCountry() {
		return country;
	}

	public long getId() {
		return id;
	}

	public String getIp() {
		return query;
	}

	public String getOrg() {
		return org;
	}

	public String getStatus() {
		return status;
	}

	public List<String> getUsers() {
		return users;
	}

	public List<String> getWhitelistUsers() {
		return whitelistUsers;
	}

	public long getTime() {
		return time;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public boolean hasUser(String username) {
		return users != null && users.contains(username);
	}

	public boolean hasWhitelistUser(String username) {
		return whitelistUsers != null && whitelistUsers.contains(username);
	}

	public boolean hasWhitelistUsers() {
		return whitelistUsers != null && users != null && whitelistUsers.stream().anyMatch(u -> users.contains(u));
	}

	public Boolean isHosting() {
		return hosting != null && hosting;
	}

	public Boolean isMobile() {
		return mobile != null && mobile;
	}

	public Boolean isProxy() {
		return proxy != null && proxy;
	}

	public boolean isOk(String baseIp) {
		return query.equals(baseIp) && status != null && status.equals("success");
	}

	public void removeUpWithDb() {
		upWithDB = false;
	}

	public void setUpWithDB(boolean upWithDB) {
		this.upWithDB = upWithDB;
	}

	public boolean isUpWithDB() {
		return upWithDB;
	}

	public void setTime() {
		time = Utils.getCurrentTimeInSeconds();
	}

	public void setDefaultTimesIfNeeded() {
		if (time != 0)
			return;
		setTime();
		lastUpdate = time;
	}

	public boolean isOutDate() {
		return time == 0 || time < outdateTime;
	}

	public void update(OlympaVpn createVpnInfo) {
		query = createVpnInfo.getIp();
		if (mobile)
			mobile = createVpnInfo.isMobile();
		if (proxy)
			proxy = createVpnInfo.isProxy();
		if (hosting)
			hosting = createVpnInfo.isHosting();
		country = createVpnInfo.getCountry();
		city = createVpnInfo.getCity();
		org = createVpnInfo.getOrg();
		as = createVpnInfo.getAs();
	}
}