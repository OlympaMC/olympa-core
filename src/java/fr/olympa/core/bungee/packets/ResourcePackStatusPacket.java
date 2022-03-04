package fr.olympa.core.bungee.packets;

import fr.olympa.core.bungee.utils.SpigotPlayerPack;
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;

public class ResourcePackStatusPacket extends DefinedPacket {
	
	private ResourcePackStatus status;
	
	public ResourcePackStatusPacket() {}
	
	public ResourcePackStatusPacket(ResourcePackStatus status) {
		this.status = status;
	}
	
	public ResourcePackStatus getStatus() {
		return status;
	}
	
	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		SpigotPlayerPack.statusPacket(this, BungeePackets.getPlayer(handler));
	}
	
	@Override
	public void read(ByteBuf buf) {
		status = ResourcePackStatus.values()[readVarInt(buf)];
	}
	
	@Override
	public void write(ByteBuf buf) {
		buf.writeInt(status.ordinal());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}else if (obj instanceof ResourcePackStatusPacket) {
			return ((ResourcePackStatusPacket) obj).status == status;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return status.hashCode();
	}
	
	@Override
	public String toString() {
		return "ResourcePackStatus(status=" + status.name() + ")";
	}
	
	public enum ResourcePackStatus {
		SUCCESSFULLY_LOADED, DECLINED, FAILED_DOWNLOAD, ACCEPTED;
	}
	
}
