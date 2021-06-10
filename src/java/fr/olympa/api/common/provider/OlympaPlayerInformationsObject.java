package fr.olympa.api.common.provider;

import java.util.UUID;

import com.google.gson.annotations.Expose;

import fr.olympa.api.common.player.OlympaPlayerInformations;

public class OlympaPlayerInformationsObject implements OlympaPlayerInformations {

	@Expose
	private final long id;
	@Expose
	private final String pseudo;
	@Expose
	private final UUID uuid;

	public OlympaPlayerInformationsObject(long id, String pseudo, UUID uuid) {
		this.id = id;
		this.pseudo = pseudo;
		this.uuid = uuid;
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

}
