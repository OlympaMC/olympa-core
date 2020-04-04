package fr.olympa.core.bungee.api.mojangapi.objects;

import java.util.UUID;

import com.google.gson.Gson;

import fr.olympa.api.utils.Utils;

public class UuidResponse {

	public static UuidResponse get(String json) {
		return new Gson().fromJson(json, UuidResponse.class);
	}

	String id;
	UUID uuid;
	String name;

	Boolean demo;
	Boolean legacy;

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public UUID getUuid() {
		if (uuid == null) {
			uuid = Utils.getUUID(name);
		}
		return uuid;
	}

	public boolean isDemo() {
		return demo != null && demo;
	}

	public boolean isLegacy() {
		return legacy != null && legacy;
	}
}
