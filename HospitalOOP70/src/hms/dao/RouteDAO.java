package hms.dao;

import hms.db.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * RouteDAO persists inter-hospital transport route edges and fictional travel minutes in MySQL.
 */
public class RouteDAO {

    public static class RouteEdge {
        private final String sourceHospitalId;
        private final String destHospitalId;
        private final int travelMinutes;
        private final double distanceKm;

        public RouteEdge(String sourceHospitalId, String destHospitalId, int travelMinutes, double distanceKm) {
            this.sourceHospitalId = sourceHospitalId;
            this.destHospitalId = destHospitalId;
            this.travelMinutes = travelMinutes;
            this.distanceKm = distanceKm;
        }

        public String getSourceHospitalId() { return sourceHospitalId; }
        public String getDestHospitalId() { return destHospitalId; }
        public int getTravelMinutes() { return travelMinutes; }
        public double getDistanceKm() { return distanceKm; }
    }

    public List<RouteEdge> findAll() {
        List<RouteEdge> list = new ArrayList<>();
        if (!DBConnection.isConnected()) return list;

        String sql = "SELECT source_hospital_id, dest_hospital_id, travel_minutes, distance_km " +
                     "FROM hospital_routes ORDER BY source_hospital_id, dest_hospital_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new RouteEdge(
                    rs.getString("source_hospital_id"),
                    rs.getString("dest_hospital_id"),
                    rs.getInt("travel_minutes"),
                    rs.getDouble("distance_km")
                ));
            }
        } catch (SQLException ex) {
            System.err.println("[RouteDAO] findAll error: " + ex.getMessage());
        }
        return list;
    }

    public boolean save(String sourceHosp, String destHosp, int travelMinutes, double distanceKm) {
        if (!DBConnection.isConnected()) return false;

        String sql = "INSERT INTO hospital_routes (source_hospital_id, dest_hospital_id, travel_minutes, distance_km) " +
                     "VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE travel_minutes=VALUES(travel_minutes), distance_km=VALUES(distance_km)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sourceHosp);
            stmt.setString(2, destHosp);
            stmt.setInt(3, travelMinutes);
            stmt.setDouble(4, distanceKm);

            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[RouteDAO] save error: " + ex.getMessage());
            return false;
        }
    }
}
