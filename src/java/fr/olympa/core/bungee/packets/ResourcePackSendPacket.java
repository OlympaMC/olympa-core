package fr.olympa.core.bungee.packets;

import java.util.Objects;

import fr.olympa.api.common.server.ResourcePack;
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

	private ResourcePack pack;
	private boolean required = false;
	private String prompt = null;

	public ResourcePackSendPacket() {
		this(new ResourcePack());
	}

	public ResourcePackSendPacket(ResourcePack pack) {
		this.pack = pack;
	}

	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		SpigotPlayerPack.sendPacket(this, BungeePackets.getPlayer(handler));
	}
	
	@Override
	public void read(ByteBuf buf, Direction direction, int protocolVersion) {
		pack.setUrl(readString(buf));
		try {
			pack.setHash(readString(buf, 40));
		} catch (IndexOutOfBoundsException ignored) {} // No hash
		if (protocolVersion >= ProtocolAPI.V1_17.getProtocolNumber()) {
			required = buf.readBoolean();
			if (buf.readBoolean()) prompt = readString(buf);
		}
	}
	
	@Override
	public void write(ByteBuf buf, Direction direction, int protocolVersion) {
		writeString(pack.getUrl(), buf);
		writeString(pack.getHash(), buf);
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
	
	public ResourcePack getResourcePack() {
		return pack;
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
		return "ResourcePackSend(pack=" + pack.toString() + ", required=" + required + ", prompt=" + prompt + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		else if (obj instanceof ResourcePackSendPacket) {
			ResourcePackSendPacket other = (ResourcePackSendPacket) obj;
			if (required != other.required) return false;
			if (!Objects.equals(pack, other.pack)) return false;
			if (!Objects.equals(prompt, other.prompt)) return false;
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = 1;
		result = result * 59 + pack.hashCode();
		result = result * 59 + (required ? 1 : 0);
		result = result * 59 + (prompt == null ? 0 : prompt.hashCode());
		return result;
	}
}