package fr.olympa.core.bungee.packets;

import java.beans.ConstructorProperties;
import java.util.Locale;

/*
 * ResourcepacksPlugins - bungee
 * Copyright (C) 2018 Max Lee aka Phoenix616 (mail@moep.tv)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.google.common.base.Charsets;
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
	
	public ResourcePackSendPacket() {};
	
	@ConstructorProperties ({ "url", "hash" })
	public ResourcePackSendPacket(String url, String hash) {
		this.url = url;
		if (hash != null) {
			this.hash = hash.toLowerCase(Locale.ROOT);
		}else {
			this.hash = Hashing.sha1().hashString(url, Charsets.UTF_8).toString().toLowerCase(Locale.ROOT);
		}
	}
	
	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		SpigotPlayerPack.sendPacket(this);
	}
	
	public void read(ByteBuf buf) {
		this.url = readString(buf);
		try {
			this.hash = readString(buf);
		}catch (IndexOutOfBoundsException ignored) {} // No hash
	}
	
	public void write(ByteBuf buf) {
		writeString(this.url, buf);
		writeString(this.hash, buf);
	}
	
	public String getUrl() {
		return this.url;
	}
	
	public String getHash() {
		return this.hash;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setHash(String hash) {
		if (hash != null) {
			this.hash = hash.substring(0, 39).toLowerCase(Locale.ROOT);
		}else {
			this.hash = Hashing.sha1().hashString(this.getUrl(), Charsets.UTF_8).toString().substring(0, 39).toLowerCase(Locale.ROOT);
		}
	}
	
	public String toString() {
		return "ResourcePackSend(url=" + this.getUrl() + ", hash=" + this.getHash() + ")";
	}
	
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}else if (obj instanceof ResourcePackSendPacket) {
			ResourcePackSendPacket other = (ResourcePackSendPacket) obj;
			String this$url = this.getUrl();
			String other$url = other.getUrl();
			if (this$url == null && other$url == null) {
				return true;
			}
			if (this$url == null || other$url == null) {
				return false;
			}
			if (!this$url.equals(other$url)) {
				return false;
			}
			String this$hash = this.getHash();
			String other$hash = other.getHash();
			
			if (this$hash == null && other$hash == null) {
				return true;
			}
			if (this$hash == null || other$hash == null) {
				return false;
			}
			return this$hash.equals(other$hash);
		}
		return false;
	}
	
	public int hashCode() {
		int result = 1;
		String $url = this.getUrl();
		result = result * 59 + ($url == null ? 0 : $url.hashCode());
		String $hash = this.getHash();
		result = result * 59 + ($hash == null ? 0 : $hash.hashCode());
		return result;
	}
}