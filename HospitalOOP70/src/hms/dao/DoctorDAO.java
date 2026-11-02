package hms.dao;

import hms.Doctor;
import hms.db.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DoctorDAO manages attending physician records and on-duty statuses in MySQL.
 */
public class DoctorDAO {

    public List<Doctor> findAll() {
        List<Doctor> list = new ArrayList<>();
        if (!DBConnection.isConnected()) return list;

        String sql = "SELECT d.doctor_id, d.name, d.department, d.hospital_id, h.hospital_name, d.on_duty " +
                     "FROM doctors d LEFT JOIN hospitals h ON d.hospital_id = h.hospital_id " +
                     "ORDER BY d.doctor_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Doctor doc = new Doctor(
                    rs.getString("doctor_id"),
                    rs.getString("name"),
                    rs.getString("department"),
                    rs.getString("hospital_id"),
                    rs.getString("hospital_name") != null ? rs.getString("hospital_name") : "Government Hospital",
                    rs.getBoolean("on_duty")
                );
                list.add(doc);
            }
        } catch (SQLException ex) {
            System.err.println("[DoctorDAO] findAll error: " + ex.getMessage());
        }
        return list;
    }

    public boolean updateDutyStatus(String doctorId, boolean onDuty) {
        if (!DBConnection.isConnected()) return false;

        String sql = "UPDATE doctors SET on_duty = ? WHERE doctor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, onDuty);
            stmt.setString(2, doctorId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[DoctorDAO] updateDutyStatus error: " + ex.getMessage());
            return false;
        }
    }

    public boolean save(Doctor doctor) {
        if (!DBConnection.isConnected()) return false;

        String sql = "INSERT INTO doctors (doctor_id, name, department, hospital_id, on_duty) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE name=VALUES(name), department=VALUES(department), " +
                     "hospital_id=VALUES(hospital_id), on_duty=VALUES(on_duty)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, doctor.getDoctorId());
            stmt.setString(2, doctor.getName());
            stmt.setString(3, doctor.getDepartment());
            stmt.setString(4, doctor.getHospitalId());
            stmt.setBoolean(5, doctor.isOnDuty());

            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[DoctorDAO] save error: " + ex.getMessage());
            return false;
        }
    }
}
