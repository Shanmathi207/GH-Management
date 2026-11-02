package hms.dao;

import hms.Hospital;
import hms.db.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * HospitalDAO manages hospital facility persistence in MySQL.
 */
public class HospitalDAO {

    public List<Hospital> findAll() {
        List<Hospital> list = new ArrayList<>();
        if (!DBConnection.isConnected()) return list;

        String sql = "SELECT hospital_id, hospital_name, location, general_bed_capacity, icu_bed_capacity FROM hospitals ORDER BY hospital_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Hospital h = new Hospital(
                    rs.getString("hospital_id"),
                    rs.getString("hospital_name"),
                    rs.getString("location"),
                    rs.getInt("general_bed_capacity"),
                    rs.getInt("icu_bed_capacity")
                );
                list.add(h);
            }
        } catch (SQLException ex) {
            System.err.println("[HospitalDAO] findAll error: " + ex.getMessage());
        }
        return list;
    }

    public boolean save(Hospital hospital) {
        if (!DBConnection.isConnected()) return false;

        String sql = "INSERT INTO hospitals (hospital_id, hospital_name, location, general_bed_capacity, icu_bed_capacity) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE hospital_name=VALUES(hospital_name), location=VALUES(location), " +
                     "general_bed_capacity=VALUES(general_bed_capacity), icu_bed_capacity=VALUES(icu_bed_capacity)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hospital.getHospitalId());
            stmt.setString(2, hospital.getHospitalName());
            stmt.setString(3, hospital.getLocation());
            stmt.setInt(4, hospital.getGeneralBedCapacity());
            stmt.setInt(5, hospital.getIcuBedCapacity());

            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[HospitalDAO] save error: " + ex.getMessage());
            return false;
        }
    }
}
