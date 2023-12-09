package de.htwberlin.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import de.htwberlin.exceptions.DataException;

public class Sample {

	private Integer sampleId;
	private LocalDate expirationDate;
	private Integer sampleKindId;
	
	public Sample() {
	}

	public Sample(Integer sampleId, Integer sampleKindId, LocalDate expirationDate) {
		super();
		this.sampleId = sampleId;
		this.sampleKindId = sampleKindId;
		this.expirationDate = expirationDate;
	}

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

	public Integer getSampleId() {
		return sampleId;
	}

	public void setSampleId(Integer sampleId) {
		this.sampleId = sampleId;
	}

	public LocalDate getExpirationdate() {
		return expirationDate;
	}

	public void setExpirationdate(LocalDate expirationdate) {
		this.expirationDate = expirationdate;
	}

	public Integer getSampleKindId() {
		return sampleKindId;
	}

	public void setSampleKindId(Integer sampleKindId) {
		this.sampleKindId = sampleKindId;
	}

	public void insert(Integer sampleKindId, LocalDate expirationdate) {
		// SQL-Code zum Speichern dieses Trays in der Datenbank
		Integer newSampleId = FindNextDatabaseId();
		String insertStatementString = "insert into sample values (?,?,?)";

		try (PreparedStatement insertStatement = useConnection().prepareStatement(insertStatementString)) {
			insertStatement.setInt(1, newSampleId);
			insertStatement.setInt(2, sampleKindId);
			insertStatement.setDate(3, java.sql.Date.valueOf(expirationdate));
			insertStatement.executeUpdate();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	public void update(Integer sampleId, Integer sampleKindId, LocalDate expirationdate) {
		// SQL-Code zum Aendern dieses Angebots in der Datenbank
		String updateStatementString = "update sample set samplekindid = ?, expirationdate = ? where sampleid = ?";

		try (PreparedStatement updateStatement = connection.prepareStatement(updateStatementString)) {
			updateStatement.setInt(1, sampleKindId);
			updateStatement.setDate(2, java.sql.Date.valueOf(expirationdate));
			updateStatement.setInt(3, sampleId);
			updateStatement.executeQuery();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	public void delete(Integer sampleId) {
		// SQL-Code zum Loeschen dieses Angebots in der Datenbank
		String deleteStatementString = "delete from sample where sampleid = ?";
		try (PreparedStatement deleteStatement = useConnection().prepareStatement(deleteStatementString)) {
			deleteStatement.setInt(1, sampleId);
			deleteStatement.executeQuery();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	private Integer FindNextDatabaseId() {
		Integer newSampleId = 1;
		String sql = "SELECT max(sampleid) as maxId from sample";
		try (Statement stmt = useConnection().createStatement()) {
			try (ResultSet rs = stmt.executeQuery(sql)) {
				rs.next();
				Integer maxId = rs.getInt("maxId");
				if (maxId != null)
					newSampleId = maxId + 1;
			}
			return newSampleId;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

}
