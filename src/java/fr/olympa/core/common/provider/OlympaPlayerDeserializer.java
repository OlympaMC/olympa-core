package fr.olympa.core.common.provider;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.player.Gender;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.common.provider.OlympaPlayerObject;
import fr.olympa.api.common.server.OlympaServer;

public class OlympaPlayerDeserializer implements JsonDeserializer<OlympaPlayer> {

	@SuppressWarnings("unchecked")
	@Override
	public OlympaPlayerObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		OlympaPlayerObject player = (OlympaPlayerObject) AccountProviderAPI.getter().getOlympaPlayerProvider().create(context.deserialize(object.get("uuid"), UUID.class), object.get("name").getAsString(), object.get("ip").getAsString());
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
			((Map<Long, String>) context.deserialize(object.get("histName"), Map.class)).forEach(player.histName::put);
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
		if (object.has("teamspeakId"))
			player.teamspeakId = object.get("teamspeakId").getAsInt();
		if (object.has("customPermissions"))
			((Map<String, String>) context.deserialize(object.get("customPermissions"), Map.class)).forEach((permission, server) -> player.customPermissions.put(permission, OlympaServer.valueOf(server)));
		return player;
	}

}
