package de.htwberlin.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


import de.htwberlin.exceptions.CoolingSystemException;
import de.htwberlin.exceptions.DataException;

public class SampleFinder {

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
	
	public Sample findSampleById(Integer sampleId) {
		  //L.info("findSampleById: sampleId: " + sampleId);
		  String sql = "select sampleId, sampleKindId, expirationDate "
		  			 + "from Sample "
		  			 + "where sampleId=?";
		  Sample s = null;
		  try (PreparedStatement ps = useConnection().prepareStatement(sql)) {
			  ps.setInt(1, sampleId);
			  try (ResultSet rs = ps.executeQuery()) {
				  if (rs.next()) {
					  s = new Sample(rs.getInt("sampleId"), 
							  		 rs.getInt("sampleKindId"), 
							  		 rs.getDate("expirationDate").toLocalDate());
				  } else {
					  throw new CoolingSystemException("Die Probe mit der " + sampleId + "existiert nicht.");
				  }
			  }
		  } catch (SQLException e) {
			  throw new DataException(e);
		  } return s;
	  }
}
