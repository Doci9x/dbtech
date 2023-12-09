package de.htwberlin.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

import de.htwberlin.exceptions.DataException;

public class Place {

	private Integer trayId;
	private Integer placeNo;
	private Integer sampleId;
	
	private final String updateStatementString = "update place set placeno=?, sampleid = ? where trayid = ?";
	private final String insertStatementString = "insert into place values (?,?,?)";

	private Connection connection = null;
	
	public Place() {
	}

	public Place(Integer trayId, Integer placeNo, Integer sampleId) {
		this.trayId = trayId;
		this.placeNo = placeNo;
		this.sampleId = sampleId;
	}

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

	public Integer getTrayId() {
		return trayId;
	}

	public void setTrayId(Integer trayId) {
		this.trayId = trayId;
	}

	public Integer getPlaceNo() {
		return placeNo;
	}

	public void setPlaceNo(Integer placeNo) {
		this.placeNo = placeNo;
	}

	public Integer getSampleId() {
		return sampleId;
	}

	public void setSampleId(Integer sampleId) {
		this.sampleId = sampleId;
	};
	
	public void insert(Integer trayId, Integer placeNo, Integer sampleId) {
		// SQL-Code zum Speichern dieses Trays in der Datenbank
		try (PreparedStatement insertStatement = useConnection().prepareStatement(insertStatementString)) {
			insertStatement.setInt(1, trayId);
			insertStatement.setInt(2, placeNo);
			insertStatement.setInt(3, sampleId);
			insertStatement.executeUpdate();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	public void update(Integer trayId, Integer placeNo, Integer sampleId) {
		// SQL-Code zum Aendern dieses Angebots in der Datenbank
		try (PreparedStatement updateStatement = connection.prepareStatement(updateStatementString)) {
			updateStatement.setInt(1, placeNo);
			updateStatement.setInt(2, sampleId);
			updateStatement.setInt(3, trayId);			
			updateStatement.executeQuery();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	public void delete() {}

}
