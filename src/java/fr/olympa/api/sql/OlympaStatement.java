package fr.olympa.api.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import fr.olympa.core.spigot.OlympaCore;

public class OlympaStatement {

	private final String statement;
	private boolean returnGeneratedKeys;

	public OlympaStatement(String statement) {
		this.statement = statement;
	}

	public OlympaStatement(String statement, boolean returnGeneratedKeys) {
		this.statement = statement;
		this.returnGeneratedKeys = returnGeneratedKeys;
	}

	private PreparedStatement prepared;
	public PreparedStatement getStatement() throws SQLException {
		if (prepared == null || prepared.isClosed()) prepared = returnGeneratedKeys ? OlympaCore.getInstance().getDatabase().prepareStatement(statement, Statement.RETURN_GENERATED_KEYS) : OlympaCore.getInstance().getDatabase().prepareStatement(statement);
		return prepared;
	}

	public String getStatementCommand() {
		return statement;
	}

}
