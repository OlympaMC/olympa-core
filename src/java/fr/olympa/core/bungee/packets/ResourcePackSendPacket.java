package fr.olympa.core.bungee.packets;

import java.beans.ConstructorProperties;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import com.google.common.hash.Hashing;

import fr.olympa.core.bungee.utils.SpigotPlayerPack;
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;

/**
 * Created by Phoenix616 on 24.03.2015.
 */
public class ResourcePackSendPacket extends DefinedPacket {

	private String url;
	private String hash;

	public ResourcePackSendPacket() {}

	@ConstructorProperties({ "url", "hash" })
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
	public void read(ByteBuf buf) {
		url = readString(buf);
		try {
			hash = readString(buf);
		} catch (IndexOutOfBoundsException ignored) {} // No hash
	}

	@Override
	public void write(ByteBuf buf) {
		writeString(url, buf);
		writeString(hash, buf);
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
			this.hash = Hashing.sha1().hashString(getUrl(), StandardCharsets.UTF_8).toString().substring(0, 39).toLowerCase(Locale.ROOT);
	}

	@Override
	public String toString() {
		return "ResourcePackSend(url=" + getUrl() + ", hash=" + getHash() + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		else if (obj instanceof ResourcePackSendPacket) {
			ResourcePackSendPacket other = (ResourcePackSendPacket) obj;
			String this$url = getUrl();
			String other$url = other.getUrl();
			if (this$url == null && other$url == null)
				return true;
			if (this$url == null || other$url == null)
				return false;
			if (!this$url.equals(other$url))
				return false;
			String this$hash = getHash();
			String other$hash = other.getHash();

			if (this$hash == null && other$hash == null)
				return true;
			if (this$hash == null || other$hash == null)
				return false;
			return this$hash.equals(other$hash);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = 1;
		String $url = getUrl();
		result = result * 59 + ($url == null ? 0 : $url.hashCode());
		String $hash = getHash();
		result = result * 59 + ($hash == null ? 0 : $hash.hashCode());
		return result;
	}
}