package fr.olympa.api.provider;

import java.util.UUID;

import fr.olympa.api.clans.ClanPlayerData;
import fr.olympa.api.player.OlympaPlayerInformations;

public class OlympaPlayerInformationsObject implements OlympaPlayerInformations {

	private final long id;
	private final String pseudo;
	private final UUID uuid;
	private ClanPlayerData clanData;

	public OlympaPlayerInformationsObject(long id, String pseudo, UUID uuid) {
		this.id = id;
		this.pseudo = pseudo;
		this.uuid = uuid;
	}

	public void setClanData(ClanPlayerData clanData) {
		this.clanData = clanData;
	}
	
	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getName() {
		return pseudo;
	}

	@Override
	public UUID getUUID() {
		return uuid;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof OlympaPlayerInformationsObject)
			return id == ((OlympaPlayerInformationsObject) obj).id;
		return false;
	}

	@Override
	public ClanPlayerData getClanData() {
		return clanData;
	}
	
}
