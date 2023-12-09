package de.htwberlin.service;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwberlin.exceptions.CoolingSystemException;
import de.htwberlin.exceptions.DataException;

public class CoolingService implements ICoolingService {
  private static final Logger L = LoggerFactory.getLogger(CoolingService.class);
  private Connection connection;

  @Override
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
  
  	private Sample sample;
	private SampleFinder sampleFinder;

	private Tray tray;
	private TrayFinder trayFinder;

	private SampleKind sampleKind;
	private SampleKindFinder sampleKindFinder;

	private Place place;
	private PlaceFinder placeFinder;

  @Override
  public void transferSample(Integer sampleId, Integer diameterInCM) {
    L.info("transferSample: sampleId: " + sampleId + ", diameterInCM: " + diameterInCM);
    sampleFinder = new SampleFinder();
	sampleFinder.setConnection(useConnection());

	sample = sampleFinder.findSampleById(sampleId);

	if (sample == null) {
		throw new CoolingSystemException("SampleId: " + sampleId + " not exist.");
	}
	
	trayFinder = new TrayFinder();
	trayFinder.setConnection(connection);
	List<Tray> trayList = trayFinder.findByDiameterInCM(diameterInCM);

	if (trayList.size() == 0) {
		L.info("transferSample: Kein Tablett mit passenden Durchmesser vorhanden");
		throw new CoolingSystemException("Kein freies Tablett mit passenden Durchmesser vorhanden");
	}

	L.info("transferSample: trayList: " + trayList);
	
	Sample sample = sampleFinder.findSampleById(sampleId);
	// List Trays in der Benutzung
	List<Tray> usedTrays = trayList.parallelStream().filter(t -> t.getExpirationDate() != null)
			.collect(Collectors.toList());
	if (usedTrays.size() != 0) { // if 1.
		// sotiert nach ExpirationDate
		List<Tray> sortedUsedTrayList = usedTrays.stream().sorted(Comparator.comparing(Tray::getExpirationDate))
				.toList();
		L.info("transferSample: sortedUsedTrayList: " + sortedUsedTrayList);

		// Freie Pl�tze in der usedTray suchen und f�r den ersten freien Platz neu Place
		// anlegen
		for (Tray tray : sortedUsedTrayList) {
			int trayid = tray.getTrayid();
			if (sample.getExpirationdate().isBefore(tray.getExpirationDate())) {
				L.info("transferSample: Expiration Date Of Sample: " + sample.getExpirationdate()
						+ " is before expiration Date  Of Tray: " + tray.getExpirationDate());
				for (int i = 0; i < tray.getCapacity(); i++) {
					if (placeIsFree(tray.getTrayid(), i + 1)) {
						L.info("transferSample: place Is Free: trayId: " + trayid + ", placeNo: " + (i + 1));
						place.setTrayId(tray.getTrayid());
						place.setPlaceNo(i + 1);
						place.setSampleId(sampleId);
						return;
					}
				}
			}
		}
		if (trayList.size() == usedTrays.size()) { // if kein neues tray
			L.info("transferSample: Alle passenden Tabletts voll. Kein freies Tablett richtiger Gr��e vorhanden");
			throw new CoolingSystemException(
					"Alle passenden Tabletts voll. Kein freies Tablett richtiger Gr��e vorhanden");
		} else { // if neues tray vorhanden
			L.info("transferSample: Ablaufdatum Probe zu gross. Freies Tablett richtiger Groesse vorhanden");

			// List Trays neu
			List<Tray> newTrays = trayList.parallelStream().filter(t -> t.getExpirationDate() == null)
					.collect(Collectors.toList());
			L.info("transferSample: newTrays: " + newTrays);

			tray = trayFinder.findById(trayList.get(0).getTrayid());
			tray.update(tray.getTrayid(), tray.getDiameterInCM(), tray.getCapacity(),
					java.sql.Date.valueOf(sample.getExpirationdate().plusDays(30)).toLocalDate());

			place.setTrayId(newTrays.get(0).getTrayid());
			place.setPlaceNo(1);
			place.setSampleId(sampleId);
			place.insert(place.getTrayId(), place.getPlaceNo(), place.getSampleId());
		}

	} else { // if 1. usedtray.size == 0
		L.info("transferSample: Keine passenden Tabletts bereits in der Benutzung voll. Nur noch neues freies Tablett richtiger Groesse vorhanden");
		tray = trayFinder.findById(trayList.get(0).getTrayid());
		tray.update(tray.getTrayid(), tray.getDiameterInCM(), tray.getCapacity(),
				java.sql.Date.valueOf(sample.getExpirationdate().plusDays(30)).toLocalDate());
		place.setTrayId(trayList.get(0).getTrayid());
		place.setPlaceNo(1);
		place.setSampleId(sampleId);
		place.insert(place.getTrayId(), place.getPlaceNo(), place.getSampleId());
		return;
	}
	
  }
  
  public boolean placeIsFree(int trayId, int placeNo) {
		L.info("PlaceIsFree: trayId: " + trayId + ", placeNo: " + placeNo);
		String sql = "SELECT placeNo FROM place WHERE trayId = ? AND placeNo = ?";
		try (PreparedStatement ps = useConnection().prepareStatement(sql)) {
			ps.setInt(1, trayId);
			ps.setInt(2, placeNo);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					L.info("PlaceIsFree: ende ,Place is Free");
					return true;
				} else {
					L.info("PlaceIsFree: ende ,Place is NOT Free");
					return false;
				}
			}
		} catch (SQLException e) {
			L.error("", e);
			throw new DataException(e);
		}
	}

}
