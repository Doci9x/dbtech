package de.htwberlin.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwberlin.exceptions.CoolingSystemException;
import de.htwberlin.exceptions.DataException;

public class TrayFinder {

	private static final Logger L = LoggerFactory.getLogger(TrayFinder.class);
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

	public Tray findById(Integer trayId) {
		Tray tr = null;
		
		String sql = "select trayid, diameterincm, capacity, expirationdate from tray where trayid=?";
		try (PreparedStatement stmt = useConnection().prepareStatement(sql)) {
			stmt.setInt(1, trayId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					tr = new Tray(rs.getInt("trayid"),
								  rs.getInt("capacity"),
								  rs.getInt("diameterincm"),
								  rs.getDate("expirationdate").toLocalDate());
				} else {
					throw new CoolingSystemException("Dieses Tray mit der " + trayId + " existiert nicht.");
				}
			}
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return tr;
	}

	public List<Tray> findByDiameterInCM(Integer diameterInCM) {
		List<Tray> trays = new ArrayList<>();
		Tray tr = null;

		  L.info("findTrayMatchingDiameter: " + diameterInCM + " cm");
		  String sql =  String.join(" ",
				  "WITH unter AS (",
				  "  SELECT t.trayid, count(*) AS count",
				  "  FROM tray t",
				  "  JOIN place p ON t.trayid = p.trayid",
				  "  GROUP BY t.trayId",
				  ")",
		    	 "SELECT *",
		    	 "FROM tray t2",
		    	 "LEFT OUTER JOIN unter ON unter.trayid = t2.trayid",
		   		 "where t2.diameterincm = ?  AND ( unter.count < t2.capacity OR unter.count IS NULL)" );
		try (PreparedStatement stmt = useConnection().prepareStatement(sql)) {
			stmt.setInt(1, diameterInCM);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					//if (rs.getDate("expirationdate") == null) {
	                int trayId = rs.getInt("trayid");
	                int capacity = rs.getInt("capacity");
	                int diameter = rs.getInt("diameterincm");	                
	                LocalDate expirationDate = rs.getDate("expirationDate").toLocalDate();
	                

	                tr = new Tray(trayId, capacity, diameter, expirationDate);
						trays.add(tr);
					
				} 
			} L.info("trayList mit " + trays.size() + " Trays mit passendem Durchmesser gefunden");
			return trays;
		} catch (SQLException e) {
			L.error("",e);
			throw new DataException(e);
		}
	}
}
