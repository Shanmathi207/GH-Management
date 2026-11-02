package hms.dao;

import hms.AdmissionRequest;
import hms.Patient;
import hms.db.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AdmissionRequestDAO handles persistence of inpatient bed admission requests.
 */
public class AdmissionRequestDAO {

    public List<AdmissionRequest> findAll(Map<String, Patient> patientMap) {
        List<AdmissionRequest> list = new ArrayList<>();
        if (!DBConnection.isConnected()) return list;

        String sql = "SELECT request_id, patient_id, bed_requirement, hospital_id, arrival_sequence, status " +
                     "FROM admission_requests ORDER BY arrival_sequence ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String reqId = rs.getString("request_id");
                String patId = rs.getString("patient_id");
                String bedReq = rs.getString("bed_requirement");
                String hospId = rs.getString("hospital_id");
                long seq = rs.getLong("arrival_sequence");
                String status = rs.getString("status");

                Patient p = (patientMap != null) ? patientMap.get(patId) : null;
                if (p == null) {
                    p = new Patient(patId, "Patient " + patId, 40, "Unspecified", "IPD", 2, "General Medicine");
                }

                AdmissionRequest req = new AdmissionRequest(reqId, p, seq, bedReq, hospId);
                req.setStatus(status);
                list.add(req);
            }
        } catch (SQLException ex) {
            System.err.println("[AdmissionRequestDAO] findAll error: " + ex.getMessage());
        }
        return list;
    }

    public boolean save(AdmissionRequest req) {
        if (!DBConnection.isConnected() || req == null) return false;

        String sql = "INSERT INTO admission_requests (request_id, patient_id, bed_requirement, hospital_id, arrival_sequence, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE bed_requirement=VALUES(bed_requirement), hospital_id=VALUES(hospital_id), " +
                     "arrival_sequence=VALUES(arrival_sequence), status=VALUES(status)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, req.getRequestId());
            stmt.setString(2, req.getPatient() != null ? req.getPatient().getPatientId() : "");
            stmt.setString(3, req.getBedRequirement());
            stmt.setString(4, req.getHospitalId());
            stmt.setLong(5, req.getArrivalSequence());
            stmt.setString(6, req.getStatus());

            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[AdmissionRequestDAO] save error: " + ex.getMessage());
            return false;
        }
    }

    public boolean updateStatus(String requestId, String status) {
        if (!DBConnection.isConnected()) return false;

        String sql = "UPDATE admission_requests SET status = ? WHERE request_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setString(2, requestId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[AdmissionRequestDAO] updateStatus error: " + ex.getMessage());
            return false;
        }
    }
}
