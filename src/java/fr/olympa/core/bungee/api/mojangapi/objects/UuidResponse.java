package fr.olympa.core.bungee.api.mojangapi.objects;

import java.util.UUID;

import com.google.gson.Gson;

public class UuidResponse {
	
	public static UuidResponse get(String json) {
		return new Gson().fromJson(json, UuidResponse.class);
	}
	
	final UUID id;
	final String name;
	final Boolean demo;
	final Boolean legacy;

	public UuidResponse(UUID id, String name, boolean demo, boolean legacy) {
		this.id = id;
		this.name = name;
		this.demo = demo;
		this.legacy = legacy;
	}

	public UUID getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}
	
	public boolean isDemo() {
		return this.demo != null && this.demo;
	}

	public boolean isLegacy() {
		return this.legacy != null && this.legacy;
	}
}
