package hms.dao;

import hms.db.DBConnection;
import java.sql.*;

/**
 * AdmissionDAO tracks active and historical inpatient admissions and discharges.
 */
public class AdmissionDAO {

    public boolean recordAdmission(String patientId, String bedId, String hospitalId) {
        if (!DBConnection.isConnected()) return false;

        String sql = "INSERT INTO admissions (patient_id, bed_id, hospital_id, admitted_at, status) " +
                     "VALUES (?, ?, ?, NOW(), 'ACTIVE')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patientId);
            stmt.setString(2, bedId);
            stmt.setString(3, hospitalId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[AdmissionDAO] recordAdmission error: " + ex.getMessage());
            return false;
        }
    }

    public boolean recordDischarge(String patientId, String notes) {
        if (!DBConnection.isConnected()) return false;

        String sql = "UPDATE admissions SET status = 'DISCHARGED', discharged_at = NOW(), discharge_notes = ? " +
                     "WHERE patient_id = ? AND status = 'ACTIVE'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, notes != null ? notes : "");
            stmt.setString(2, patientId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[AdmissionDAO] recordDischarge error: " + ex.getMessage());
            return false;
        }
    }

    public int getDischargeCount() {
        if (!DBConnection.isConnected()) return 0;

        String sql = "SELECT COUNT(*) FROM admissions WHERE status = 'DISCHARGED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            System.err.println("[AdmissionDAO] getDischargeCount error: " + ex.getMessage());
        }
        return 0;
    }
}
