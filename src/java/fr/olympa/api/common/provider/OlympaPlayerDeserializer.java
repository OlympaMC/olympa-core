package fr.olympa.api.common.provider;

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

public class OlympaPlayerDeserializer implements JsonDeserializer<OlympaPlayer> {

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
		if (object.has("customPermissions"))
			player.customPermissions = context.deserialize(object.get("customPermissions"), TreeMap.class);
		return player;
	}

}
