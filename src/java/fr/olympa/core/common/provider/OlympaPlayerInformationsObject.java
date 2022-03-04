package fr.olympa.core.common.provider;

import java.util.UUID;

import fr.olympa.api.common.provider.OlympaPlayerInformationsAPI;

public class OlympaPlayerInformationsObject extends OlympaPlayerInformationsAPI {

	public OlympaPlayerInformationsObject(long id, String pseudo, UUID uuid) {
		super(id, pseudo, uuid);
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
