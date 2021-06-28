package fr.olympa.core.bungee.packets;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;

import com.google.common.hash.Hashing;

import fr.olympa.api.spigot.utils.ProtocolAPI;
import fr.olympa.core.bungee.utils.SpigotPlayerPack;
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants.Direction;

/**
 * Created by Phoenix616 on 24.03.2015.
 * Updated by SkytAsul since 2021.
 */
public class ResourcePackSendPacket extends DefinedPacket {

	private String url;
	private String hash;
	private boolean required = false;
	private String prompt = null;

	public ResourcePackSendPacket() {}

	public ResourcePackSendPacket(String url, String hash) {
		this.url = url;
		if (hash != null)
			this.hash = hash.toLowerCase(Locale.ROOT);
		else
			this.hash = Hashing.sha1().hashString(url, StandardCharsets.UTF_8).toString().toLowerCase(Locale.ROOT);
	}

	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		SpigotPlayerPack.sendPacket(this);
	}
	
	@Override
	public void read(ByteBuf buf, Direction direction, int protocolVersion) {
		url = readString(buf);
		try {
			hash = readString(buf, 40);
		} catch (IndexOutOfBoundsException ignored) {} // No hash
		if (protocolVersion >= ProtocolAPI.V1_17.getProtocolNumber()) {
			required = buf.readBoolean();
			if (buf.readBoolean()) prompt = readString(buf);
		}
	}
	
	@Override
	public void write(ByteBuf buf, Direction direction, int protocolVersion) {
		writeString(url, buf);
		writeString(hash, buf);
		if (protocolVersion >= ProtocolAPI.V1_17.getProtocolNumber()) {
			buf.writeBoolean(required);
			if (prompt != null) {
				buf.writeBoolean(true);
				writeString(prompt, buf);
			}else {
				buf.writeBoolean(false);
			}
		}
	}

	public String getUrl() {
		return url;
	}

	public String getHash() {
		return hash;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setHash(String hash) {
		if (hash != null)
			this.hash = hash.substring(0, 39).toLowerCase(Locale.ROOT);
		else
			this.hash = Hashing.sha1().hashString(url, StandardCharsets.UTF_8).toString().substring(0, 39).toLowerCase(Locale.ROOT);
	}

	public boolean isRequired() {
		return required;
	}
	
	public void setRequired(boolean required) {
		this.required = required;
	}
	
	public String getPrompt() {
		return prompt;
	}
	
	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}
	
	@Override
	public String toString() {
		return "ResourcePackSend(url=" + url + ", hash=" + hash + ", required=" + required + ", prompt=" + prompt + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		else if (obj instanceof ResourcePackSendPacket) {
			ResourcePackSendPacket other = (ResourcePackSendPacket) obj;
			if (required != other.required) return false;
			if (!Objects.equals(url, other.url)) return false;
			if (!Objects.equals(hash, other.hash)) return false;
			if (!Objects.equals(prompt, other.prompt)) return false;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = 1;
		result = result * 59 + (url == null ? 0 : url.hashCode());
		result = result * 59 + (hash == null ? 0 : hash.hashCode());
		result = result * 59 + (required ? 1 : 0);
		result = result * 59 + (prompt == null ? 0 : prompt.hashCode());
		return result;
	}
}