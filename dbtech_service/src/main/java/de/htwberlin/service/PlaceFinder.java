

package de.htwberlin.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.htwberlin.exceptions.DataException;

public class PlaceFinder {

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

	public Place findByTrayId(Integer trayId) {
		Place pl = null;
		pl.setConnection(useConnection());
		String sql = "select trayid, placeno, sampleid from place where trayid=?";
		try (PreparedStatement stmt = useConnection().prepareStatement(sql)) {
			stmt.setInt(1, trayId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					pl.setTrayId(trayId);
					pl.setPlaceNo(rs.getInt("placeno"));
					pl.setSampleId(rs.getInt("sampleid"));				}
			}
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return pl;
	}

}
