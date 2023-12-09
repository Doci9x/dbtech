package de.htwberlin.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.htwberlin.exceptions.CoolingSystemException;
import de.htwberlin.exceptions.DataException;

public class SampleKindFinder {

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

	public SampleKind findById(Integer sampleKindId) {
		SampleKind sK = null;
		//sK.setConnection(useConnection());
		String sql = "select samplekindid, text, validnoofdays from samplekind where samplekindid=?";
		try (PreparedStatement stmt = useConnection().prepareStatement(sql)) {
			stmt.setInt(1, sampleKindId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					sK= new SampleKind(rs.getInt("sampleKindId"),
										rs.getString("text"),
										rs.getInt("validnoofdays"));
				} else {
					throw new CoolingSystemException("No SampleKind with SampleKindId: " + sampleKindId + " exists.");
				}
			}
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return sK;
	}

	public List<SampleKind> findByText(String text) {
		List<SampleKind> sKs = new ArrayList<SampleKind>();
		SampleKind sK = null;
		//sK.setConnection(useConnection());
		String sql = "select samplekindid, text, validnoofdays from samplekind where text=?";
		try (PreparedStatement stmt = useConnection().prepareStatement(sql)) {
			stmt.setString(1, text);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					sK= new SampleKind(rs.getInt("sampleKindId"),
							rs.getString("text"),
							rs.getInt("validnoofdays"));
					sKs.add(sK);
				}
			}
			return sKs;
		} catch (SQLException e) {
			throw new DataException(e);
		}

	}

}
