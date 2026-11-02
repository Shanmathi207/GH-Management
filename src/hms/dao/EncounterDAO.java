package hms.dao;

import hms.Encounter;
import hms.db.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * EncounterDAO persists OPD and Emergency clinical consultations in MySQL.
 */
public class EncounterDAO {

    public List<Encounter> findAll() {
        List<Encounter> list = new ArrayList<>();
        if (!DBConnection.isConnected()) return list;

        String sql = "SELECT encounter_id, patient_id, doctor_id, doctor_name, department, hospital_id, " +
                     "symptoms, diagnosis, treatment_notes, notes, encounter_date, status " +
                     "FROM encounters ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String encId = rs.getString("encounter_id");
                String patId = rs.getString("patient_id");
                String docId = rs.getString("doctor_id");
                String docName = rs.getString("doctor_name");
                String dept = rs.getString("department");
                String hospId = rs.getString("hospital_id");
                String symptoms = rs.getString("symptoms");
                String diagnosis = rs.getString("diagnosis");
                String treatmentNotes = rs.getString("treatment_notes");
                String notes = rs.getString("notes");
                String date = rs.getString("encounter_date");
                String status = rs.getString("status");

                Encounter enc = new Encounter(encId, patId, docId, docName, dept, hospId,
                                              symptoms, diagnosis, treatmentNotes, notes, date, status);
                list.add(enc);
            }
        } catch (SQLException ex) {
            System.err.println("[EncounterDAO] findAll error: " + ex.getMessage());
        }
        return list;
    }

    public List<Encounter> findByPatientId(String patientId) {
        List<Encounter> list = new ArrayList<>();
        if (!DBConnection.isConnected() || patientId == null) return list;

        String sql = "SELECT encounter_id, patient_id, doctor_id, doctor_name, department, hospital_id, " +
                     "symptoms, diagnosis, treatment_notes, notes, encounter_date, status " +
                     "FROM encounters WHERE patient_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Encounter(
                        rs.getString("encounter_id"),
                        rs.getString("patient_id"),
                        rs.getString("doctor_id"),
                        rs.getString("doctor_name"),
                        rs.getString("department"),
                        rs.getString("hospital_id"),
                        rs.getString("symptoms"),
                        rs.getString("diagnosis"),
                        rs.getString("treatment_notes"),
                        rs.getString("notes"),
                        rs.getString("encounter_date"),
                        rs.getString("status")
                    ));
                }
            }
        } catch (SQLException ex) {
            System.err.println("[EncounterDAO] findByPatientId error: " + ex.getMessage());
        }
        return list;
    }

    public boolean save(Encounter enc) {
        if (!DBConnection.isConnected() || enc == null) return false;

        String sql = "INSERT INTO encounters (encounter_id, patient_id, doctor_id, doctor_name, department, hospital_id, " +
                     "symptoms, diagnosis, treatment_notes, notes, encounter_date, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE symptoms=VALUES(symptoms), diagnosis=VALUES(diagnosis), " +
                     "treatment_notes=VALUES(treatment_notes), notes=VALUES(notes), status=VALUES(status)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, enc.getEncounterId());
            stmt.setString(2, enc.getPatientId());
            stmt.setString(3, enc.getDoctorId());
            stmt.setString(4, enc.getDoctorName());
            stmt.setString(5, enc.getDepartment());
            stmt.setString(6, enc.getHospitalId());
            stmt.setString(7, enc.getSymptoms());
            stmt.setString(8, enc.getDiagnosis());
            stmt.setString(9, enc.getTreatmentNotes());
            stmt.setString(10, enc.getNotes());
            stmt.setString(11, enc.getEncounterDate());
            stmt.setString(12, enc.getStatus());

            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[EncounterDAO] save error: " + ex.getMessage());
            return false;
        }
    }
}
