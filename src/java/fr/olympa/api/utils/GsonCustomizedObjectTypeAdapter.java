package fr.olympa.api.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.common.provider.OlympaPlayerInformationsObject.OlympaPlayerInformationsDeserializer;
import fr.olympa.api.common.provider.OlympaPlayerObject.OlympaPlayerDeserializer;

public class GsonCustomizedObjectTypeAdapter extends TypeAdapter<Object> {

	private static final GsonCustomizedObjectTypeAdapter adapter = new GsonCustomizedObjectTypeAdapter();
	public static final GsonBuilder GSON_BUILDER = new GsonBuilder()
			.registerTypeHierarchyAdapter(OlympaPlayer.class, new OlympaPlayerDeserializer())
			.registerTypeHierarchyAdapter(OlympaPlayerInformations.class, new OlympaPlayerInformationsDeserializer())
			.registerTypeHierarchyAdapter(Map.class, adapter)
			.registerTypeHierarchyAdapter(List.class, adapter)
			.excludeFieldsWithoutExposeAnnotation();
	public static final Gson GSON = GSON_BUILDER.create();

	private final TypeAdapter<Object> delegate = new Gson().getAdapter(Object.class);

	@Override
	public void write(JsonWriter out, Object value) throws IOException {
		delegate.write(out, value);
	}

	@Override
	public Object read(JsonReader in) throws IOException {
		JsonToken token = in.peek();
		switch (token) {
		case BEGIN_ARRAY:
			List<Object> list = new ArrayList<>();
			in.beginArray();
			while (in.hasNext())
				list.add(read(in));
			in.endArray();
			return list;

		case BEGIN_OBJECT:
			Map<Object, Object> map = new TreeMap<>();
			in.beginObject();
			while (in.hasNext()) {
				Object name = in.nextName();
				try {
					name = Long.parseLong((String) name);
				} catch (NumberFormatException e) {}
				map.put(name, read(in));
			}
			in.endObject();
			return map;

		case STRING:
			/*Object obj = in.nextString();
			try {
				obj = Long.parseLong((String) obj);
			}catch (NumberFormatException e) {}*/
			return in.nextString();

		case NUMBER:
			//return in.nextDouble();
			String n = in.nextString();
			if (n.indexOf('.') != -1)
				return Double.parseDouble(n);
			return Long.parseLong(n);

		case BOOLEAN:
			return in.nextBoolean();

		case NULL:
			in.nextNull();
			return null;

		default:
			throw new IllegalStateException();
		}
	}

}
