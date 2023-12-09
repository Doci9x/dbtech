package de.htwberlin.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.htwberlin.exceptions.DataException;

public class SampleKind {

	private Integer sampleKindId;
	private String text;
	private Integer validNoOfDays;
	
	public SampleKind() {
		
	}
	
	public SampleKind(Integer sampleKindId, String text, Integer validNoOfDays) {
		this.sampleKindId = sampleKindId;
		this.text = text;
		this.validNoOfDays = validNoOfDays;
	}

	private final String updateStatementString = "update samplekind set text = ?, validnoofdays =? where samplekindid = ?";
	private final String insertStatementString = "insert into samplekind values (?,?,?)";
	private final String deleteStatementString = "delete from samplekind where samplekindid = ?";

	private Connection connection = null;

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	@SuppressWarnings("unused")
	private Connection useConnection() {
		if (connection == null) {
			throw new DataException("Connection not set");
		}
		return connection;
	}

	public Integer getSampleKindId() {
		return sampleKindId;
	}

	public void setSampleKindId(Integer sampleKindId) {
		this.sampleKindId = sampleKindId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Integer getValidNoOfDays() {
		return validNoOfDays;
	}

	public void setValidNoOfDays(Integer validNoOfDays) {
		validNoOfDays = validNoOfDays;
	}

	public void insert(String text, Integer validNoOfDays) {
		// SQL-Code zum Speichern dieses Trays in der Datenbank
		Integer newSampleKindId = FindNextDatabaseId();
		try (PreparedStatement insertStatement = useConnection().prepareStatement(insertStatementString)) {
			insertStatement.setInt(1, newSampleKindId);
			insertStatement.setString(2, text);
			insertStatement.setInt(3, validNoOfDays);
			insertStatement.executeUpdate();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	public void update(Integer sampleKindId, String text, Integer validOfNoDays) {
		// SQL-Code zum Aendern dieses Angebots in der Datenbank
		try (PreparedStatement updateStatement = connection.prepareStatement(updateStatementString)) {
			updateStatement.setString(1, text);
			updateStatement.setInt(2, validNoOfDays);
			updateStatement.setInt(3, sampleKindId);
			updateStatement.executeQuery();
		} catch (SQLException e) {
			throw new DataException(e);
		} 
	}

	public void delete(Integer sampleKindId) {
		// SQL-Code zum Loeschen dieses Angebots in der Datenbank
		try (PreparedStatement deleteStatement = useConnection().prepareStatement(deleteStatementString)){
			deleteStatement.setInt(1, sampleKindId);
			deleteStatement.executeQuery();
		}  catch (SQLException e) {
			throw new DataException(e);
		}
	}

	private Integer FindNextDatabaseId() {
		Integer newSampleKindId = 1;
		String sql = "SELECT max(samplekindid) as maxId from samplekind";
		try (Statement stmt = useConnection().createStatement()) {
			try (ResultSet rs = stmt.executeQuery(sql)) {
				rs.next();
				Integer maxId = rs.getInt("maxId");
				if (maxId != null)
					newSampleKindId = maxId + 1;
			}
			return newSampleKindId;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
}
