package hms.dao;

import hms.db.DBConnection;
import hms.ui.demo.DemoDataStore.ReferralRecord;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ReferralDAO handles inter-hospital transfer referral records in MySQL.
 */
public class ReferralDAO {

    public List<ReferralRecord> findAll() {
        List<ReferralRecord> list = new ArrayList<>();
        if (!DBConnection.isConnected()) return list;

        String sql = "SELECT referral_id, patient_id, patient_name, source_hospital, dest_hospital, bed_type, travel_minutes, timestamp " +
                     "FROM referrals ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String refId = rs.getString("referral_id");
                String patId = rs.getString("patient_id");
                String patName = rs.getString("patient_name");
                String src = rs.getString("source_hospital");
                String dest = rs.getString("dest_hospital");
                String bType = rs.getString("bed_type");
                int travelMins = rs.getInt("travel_minutes");
                String ts = rs.getString("timestamp");

                ReferralRecord ref = new ReferralRecord(refId, patId, patName, src, dest, bType, travelMins, ts);
                list.add(ref);
            }
        } catch (SQLException ex) {
            System.err.println("[ReferralDAO] findAll error: " + ex.getMessage());
        }
        return list;
    }

    public boolean save(ReferralRecord ref) {
        if (!DBConnection.isConnected() || ref == null) return false;

        String sql = "INSERT INTO referrals (referral_id, patient_id, patient_name, source_hospital, dest_hospital, bed_type, travel_minutes, timestamp) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE travel_minutes=VALUES(travel_minutes)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ref.getReferralId());
            stmt.setString(2, ref.getPatientId());
            stmt.setString(3, ref.getPatientName());
            stmt.setString(4, ref.getSourceHospital());
            stmt.setString(5, ref.getDestHospital());
            stmt.setString(6, ref.getBedType());
            stmt.setInt(7, ref.getTravelMinutes());
            stmt.setString(8, ref.getTimestamp());

            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[ReferralDAO] save error: " + ex.getMessage());
            return false;
        }
    }
}
