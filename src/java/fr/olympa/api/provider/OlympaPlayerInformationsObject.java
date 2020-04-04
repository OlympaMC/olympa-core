package fr.olympa.api.provider;

import java.util.UUID;

import fr.olympa.api.objects.OlympaPlayerInformations;

public class OlympaPlayerInformationsObject implements OlympaPlayerInformations {

	private final long id;
	private final String pseudo;
	private final UUID uuid;

	public OlympaPlayerInformationsObject(long id, String pseudo, UUID uuid) {
		this.id = id;
		this.pseudo = pseudo;
		this.uuid = uuid;
	}

	@Override
	public long getID() {
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
		if (obj instanceof OlympaPlayerInformationsObject) {
			return id == ((OlympaPlayerInformationsObject) obj).id;
		}
		return false;
	}
	
}
