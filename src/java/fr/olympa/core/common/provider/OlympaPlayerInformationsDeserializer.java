package fr.olympa.core.common.provider;

import java.lang.reflect.Type;
import java.util.UUID;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import fr.olympa.api.common.player.OlympaPlayerInformations;

public class OlympaPlayerInformationsDeserializer implements JsonDeserializer<OlympaPlayerInformations> {

	@Override
	public OlympaPlayerInformations deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		return new OlympaPlayerInformationsObject(object.get("id").getAsLong(), object.get("pseudo").getAsString(), context.deserialize(object.get("uuid"), UUID.class));
	}

}
