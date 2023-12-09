package de.htwberlin.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import de.htwberlin.exceptions.DataException;

public class Tray {
	private Integer trayId;
	private Integer diameterInCm;
	private Integer capacity;
	private LocalDate expirationDate;

	private final String updateStatementString = "update sample set diameterInCM = ?, capacity = ?, expirationdate = ? where trayid = ?";
	private final String insertStatementString = "insert into tray values (?,?,?,?)";
	private final String deleteStatementString = "delete from tray where trayid = ?";

	private Connection connection = null;
	
	public Tray(Integer trayId, Integer diameterInCm, Integer capacity, LocalDate expirationDate) {
		super();
		this.trayId = trayId;
		this.diameterInCm = diameterInCm;
		this.capacity = capacity;
		this.expirationDate = expirationDate;
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

	public Integer getTrayid() {
		return trayId;
	}

	public void setTrayid(Integer trayid) {
		this.trayId = trayid;
	}

	public Integer getDiameterInCM() {
		return diameterInCm;
	}

	public void setDiameterInCM(Integer diameterInCM) {
		this.diameterInCm = diameterInCM;
	}

	public Integer getCapacity() {
		return capacity;
	}

	public void setCapacity(Integer capacity) {
		this.capacity = capacity;
	}

	public LocalDate getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(LocalDate expirationDate) {
		this.expirationDate = expirationDate;
	}

	public void insert(Integer diameterInCM, Integer capacity, LocalDate expirationdate) {
		// SQL-Code zum Speichern dieses Trays in der Datenbank
		Integer newTrayId = FindNextDatabaseId();
		try (PreparedStatement insertStatement = useConnection().prepareStatement(insertStatementString)) {
			insertStatement.setInt(1, newTrayId);
			insertStatement.setInt(2, diameterInCM);
			insertStatement.setInt(3, capacity);
			insertStatement.setDate(4, java.sql.Date.valueOf(expirationdate));
			insertStatement.executeUpdate();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	public void update(Integer trayId, Integer diameterInCM, Integer capacity, LocalDate expirationdate) {
		// SQL-Code zum Aendern dieses Angebots in der Datenbank
		try (PreparedStatement updateStatement = connection.prepareStatement(updateStatementString)) {
			updateStatement.setInt(1, diameterInCM);
			updateStatement.setInt(2, capacity);
			updateStatement.setDate(3, java.sql.Date.valueOf(expirationdate));
			updateStatement.setInt(4, trayId);
			updateStatement.executeQuery();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	public void delete(Integer trayId) {
		// SQL-Code zum Loeschen dieses Angebots in der Datenbank
		try (PreparedStatement deleteStatement = useConnection().prepareStatement(deleteStatementString)) {
			deleteStatement.setInt(1, trayId);
			deleteStatement.executeQuery();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	private Integer FindNextDatabaseId() {
		Integer newTrayId = 1;
		String sql = "SELECT max(trayId) as maxId from tray";
		try (Statement stmt = useConnection().createStatement()) {
			try (ResultSet rs = stmt.executeQuery(sql)) {
				rs.next();
				Integer maxId = rs.getInt("maxId");
				if (maxId != null)
					newTrayId = maxId + 1;
			}
			return newTrayId;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

}
