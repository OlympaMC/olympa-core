package fr.olympa.api.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.StringJoiner;

import fr.olympa.api.LinkSpigotBungee;

public class OlympaStatement {

	public enum StatementType {
		INSERT("INTO"),
		SELECT,
		UPDATE,
		DELETE("FROM");

		String supp;

		private StatementType() {
		}

		private StatementType(String supp) {
			this.supp = supp;
		}

		public String get() {
			StringJoiner sj = new StringJoiner(" ");
			sj.add(name());
			if (supp != null)
				sj.add(supp);
			return sj.toString();
		}
	}

	private String statement;
	private boolean returnGeneratedKeys;

	public OlympaStatement(StatementType type, String tableName, String... keys) {
		if (type != StatementType.INSERT && type != StatementType.DELETE) {
			new Exception("Wrong StatementType." + type.name() + ". Must be StatementType." + StatementType.INSERT.name() + " or StatementType." + StatementType.DELETE.name() + ".").printStackTrace();
			return;
		}
		StringJoiner sj = new StringJoiner(" ");
		sj.add(type.get());
		sj.add(tableName);
		if (type == StatementType.INSERT) {
			StringJoiner sj2 = new StringJoiner(", ", "(", ")");
			for (String key : keys)
				sj2.add("`" + key + "`");
			sj.add(sj2.toString());
			sj.add("VALUES");
			sj2 = new StringJoiner(", ", "(", ")");
			for (@SuppressWarnings("unused")
			String key : keys)
				sj2.add("?");
			sj.add(sj2.toString());
		} else {
			sj.add("WHERE");
			StringJoiner sj2 = new StringJoiner(" AND ");
			Arrays.stream(keys).forEach(w -> sj2.add("`" + w + "` = ?"));
			sj.add(sj2.toString());
		}
		statement = sj.toString() + ";";
		returnGeneratedKeys = true;
	}

	public OlympaStatement(StatementType type, String tableName, String what, String[] keys) {
		this(type, tableName, new String[] { what }, keys);
	}

	public OlympaStatement(StatementType type, String tableName, String[] what, String... keys) {
		if (type != StatementType.SELECT && type != StatementType.UPDATE) {
			new Exception("Wrong StatementType." + type.name() + ". Must be StatementType." + StatementType.SELECT.name() + " or StatementType." + StatementType.UPDATE.name() + ".").printStackTrace();
			return;
		}
		StringJoiner sj = new StringJoiner(" ");
		sj.add(type.get());
		if (type == StatementType.SELECT) {
			if (keys == null || keys.length == 0)
				keys = new String[] { "*" };
			sj.add(String.join(", ", keys));
			sj.add("FROM");
		}
		sj.add(tableName);
		if (type == StatementType.UPDATE) {
			sj.add("SET");
			StringJoiner sj2 = new StringJoiner(", ");
			Arrays.stream(keys).forEach(key -> sj2.add("`" + key + "` = ?"));
			sj.add(sj2.toString());
		}
		if (what != null && what.length != 0) {
			sj.add("WHERE");
			StringJoiner sj2 = new StringJoiner(" AND ");
			Arrays.stream(what).forEach(w -> sj2.add("`" + w + "` = ?"));
			sj.add(sj2.toString());
		}
		statement = sj.toString() + ";";
	}

	public OlympaStatement(String statement) {
		this(statement, false);
	}

	public OlympaStatement(String statement, boolean returnGeneratedKeys) {
		this.statement = statement;
		this.returnGeneratedKeys = returnGeneratedKeys;
	}

	private PreparedStatement prepared;

	public PreparedStatement getStatement() throws SQLException {
		if (prepared == null || prepared.isClosed() || !prepared.getConnection().isValid(0))
			prepared = returnGeneratedKeys ? LinkSpigotBungee.Provider.link.getDatabase().prepareStatement(statement, Statement.RETURN_GENERATED_KEYS) : LinkSpigotBungee.Provider.link.getDatabase().prepareStatement(statement);
		return prepared;
	}

	public String getStatementCommand() {
		return statement;
	}

}
