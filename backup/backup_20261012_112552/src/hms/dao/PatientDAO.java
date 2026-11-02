package hms.dao;

import hms.Patient;
import hms.db.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PatientDAO manages patient census records, triage severity, and admissions in MySQL.
 */
public class PatientDAO {

    public List<Patient> findAll() {
        List<Patient> list = new ArrayList<>();
        if (!DBConnection.isConnected()) return list;

        String sql = "SELECT patient_id, name, age, gender, department, visit_type, " +
                     "required_resource, emergency_level, priority, status, assigned_bed_id, " +
                     "assigned_doctor_name, assigned_hospital_id, arrival_sequence, registration_date_time " +
                     "FROM patients ORDER BY arrival_sequence ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Patient p = mapRowToPatient(rs);
                list.add(p);
            }
        } catch (SQLException ex) {
            System.err.println("[PatientDAO] findAll error: " + ex.getMessage());
        }
        return list;
    }

    public Patient findById(String patientId) {
        if (!DBConnection.isConnected()) return null;

        String sql = "SELECT patient_id, name, age, gender, department, visit_type, " +
                     "required_resource, emergency_level, priority, status, assigned_bed_id, " +
                     "assigned_doctor_name, assigned_hospital_id, arrival_sequence, registration_date_time " +
                     "FROM patients WHERE patient_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToPatient(rs);
                }
            }
        } catch (SQLException ex) {
            System.err.println("[PatientDAO] findById error: " + ex.getMessage());
        }
        return null;
    }

    public boolean save(Patient patient) {
        if (!DBConnection.isConnected()) return false;

        String sql = "INSERT INTO patients (patient_id, name, age, gender, department, visit_type, " +
                     "required_resource, emergency_level, priority, status, assigned_bed_id, " +
                     "assigned_doctor_name, assigned_hospital_id, arrival_sequence, registration_date_time) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE name=VALUES(name), age=VALUES(age), gender=VALUES(gender), " +
                     "department=VALUES(department), visit_type=VALUES(visit_type), required_resource=VALUES(required_resource), " +
                     "emergency_level=VALUES(emergency_level), priority=VALUES(priority), status=VALUES(status), " +
                     "assigned_bed_id=VALUES(assigned_bed_id), assigned_doctor_name=VALUES(assigned_doctor_name), " +
                     "assigned_hospital_id=VALUES(assigned_hospital_id), arrival_sequence=VALUES(arrival_sequence), " +
                     "registration_date_time=VALUES(registration_date_time)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patient.getPatientId());
            stmt.setString(2, patient.getName());
            stmt.setInt(3, patient.getAge());
            stmt.setString(4, patient.getGender());
            stmt.setString(5, patient.getDepartment());
            stmt.setString(6, patient.getVisitType());
            stmt.setString(7, patient.getRequiredResource());
            stmt.setString(8, patient.getEmergencyLevel());
            stmt.setInt(9, patient.getPriority());
            stmt.setString(10, patient.getStatus());
            stmt.setString(11, patient.getAssignedBedId());
            stmt.setString(12, patient.getAssignedDoctorName());
            stmt.setString(13, patient.getAssignedHospitalId());
            stmt.setLong(14, patient.getArrivalSequence());
            stmt.setString(15, patient.getRegistrationDateTime());

            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[PatientDAO] save error: " + ex.getMessage());
            return false;
        }
    }

    public boolean update(Patient patient) {
        return save(patient);
    }

    public boolean delete(String patientId) {
        if (!DBConnection.isConnected()) return false;

        String sql = "DELETE FROM patients WHERE patient_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patientId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[PatientDAO] delete error: " + ex.getMessage());
            return false;
        }
    }

    private Patient mapRowToPatient(ResultSet rs) throws SQLException {
        Patient p = new Patient(
            rs.getString("patient_id"),
            rs.getString("name"),
            rs.getInt("age"),
            rs.getString("gender"),
            rs.getString("department"),
            rs.getString("required_resource"),
            rs.getString("emergency_level"),
            rs.getInt("priority")
        );
        p.setVisitType(rs.getString("visit_type"));
        p.setStatus(rs.getString("status"));
        p.setAssignedBedId(rs.getString("assigned_bed_id"));
        p.setAssignedDoctorName(rs.getString("assigned_doctor_name"));
        p.setAssignedHospitalId(rs.getString("assigned_hospital_id"));
        p.setArrivalSequence(rs.getLong("arrival_sequence"));
        p.setRegistrationDateTime(rs.getString("registration_date_time"));
        return p;
    }
}
