package fr.olympa.core.bungee.vpn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;

public class OlympaVpn {
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
	List<String> users = new ArrayList<>();

	public OlympaVpn(long id, String query, Boolean proxy, Boolean mobile, Boolean hosting, String usersString, String country, String city, String org, String as) {
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
	}

	public void addUser(String username) {
		if (users == null)
			users = new ArrayList<>();
		users.add(username);
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

	public boolean hasUser(String username) {
		return users.contains(username);
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

	public boolean isOk() {
		return status != null && status.equals("success");
	}

}