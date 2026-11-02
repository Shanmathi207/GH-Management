package hms.dao;

import hms.db.DBConnection;
import hms.ui.demo.DemoDataStore.HistoryRecord;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * HistoryDAO persists clinical, operational, and administrative audit history in MySQL.
 */
public class HistoryDAO {

    public List<HistoryRecord> findAll() {
        List<HistoryRecord> list = new ArrayList<>();
        if (!DBConnection.isConnected()) return list;

        String sql = "SELECT history_id, patient_id, activity_type, description, staff_user, timestamp " +
                     "FROM patient_history ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String hId = rs.getString("history_id");
                String pId = rs.getString("patient_id");
                String actType = rs.getString("activity_type");
                String desc = rs.getString("description");
                String staff = rs.getString("staff_user");
                String ts = rs.getString("timestamp");

                HistoryRecord hr = new HistoryRecord(hId, pId, actType, desc, staff, ts);
                list.add(hr);
            }
        } catch (SQLException ex) {
            System.err.println("[HistoryDAO] findAll error: " + ex.getMessage());
        }
        return list;
    }

    public boolean save(HistoryRecord hr) {
        if (!DBConnection.isConnected() || hr == null) return false;

        String sql = "INSERT INTO patient_history (history_id, patient_id, activity_type, description, staff_user, timestamp) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE description=VALUES(description)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hr.getHistoryId());
            stmt.setString(2, hr.getPatientId());
            stmt.setString(3, hr.getActivityType());
            stmt.setString(4, hr.getDescription());
            stmt.setString(5, hr.getStaffUser());
            stmt.setString(6, hr.getTimestamp());

            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[HistoryDAO] save error: " + ex.getMessage());
            return false;
        }
    }
}
