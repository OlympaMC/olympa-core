package fr.olympa.api.brigadier;

import java.util.UUID;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class UUIDArgumentType implements ArgumentType<UUID> {
	private UUIDArgumentType() {
	}

	public static UUIDArgumentType uuid() {
		return new UUIDArgumentType();
	}

	@Override
	public UUID parse(StringReader reader) throws CommandSyntaxException {
		return UUID.fromString(reader.readUnquotedString().replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
	}

}