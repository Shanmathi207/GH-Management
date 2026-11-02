package hms.dao;

import hms.Patient;
import hms.db.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PatientDAO manages patient census records, demographics, triage severity, and admissions in MySQL.
 */
public class PatientDAO {

    public List<Patient> findAll() {
        List<Patient> list = new ArrayList<>();
        if (!DBConnection.isConnected()) return list;

        String sql = "SELECT patient_id, name, age, gender, contact_number, address, department, visit_type, " +
                     "required_resource, emergency_level, priority, status, assigned_bed_id, " +
                     "assigned_doctor_name, assigned_hospital_id, arrival_sequence, registration_date_time " +
                     "FROM patients ORDER BY arrival_sequence ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToPatient(rs));
            }
        } catch (SQLException ex) {
            System.err.println("[PatientDAO] findAll error: " + ex.getMessage());
        }
        return list;
    }

    public Patient findById(String patientId) {
        if (!DBConnection.isConnected() || patientId == null) return null;

        String sql = "SELECT patient_id, name, age, gender, contact_number, address, department, visit_type, " +
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

    public List<Patient> search(String query) {
        List<Patient> list = new ArrayList<>();
        if (!DBConnection.isConnected()) return list;
        if (query == null || query.trim().isEmpty()) return findAll();

        String q = "%" + query.trim().toLowerCase() + "%";
        String sql = "SELECT patient_id, name, age, gender, contact_number, address, department, visit_type, " +
                     "required_resource, emergency_level, priority, status, assigned_bed_id, " +
                     "assigned_doctor_name, assigned_hospital_id, arrival_sequence, registration_date_time " +
                     "FROM patients WHERE LOWER(patient_id) LIKE ? OR LOWER(name) LIKE ? OR LOWER(contact_number) LIKE ? " +
                     "ORDER BY arrival_sequence DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, q);
            stmt.setString(2, q);
            stmt.setString(3, q);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToPatient(rs));
                }
            }
        } catch (SQLException ex) {
            System.err.println("[PatientDAO] search error: " + ex.getMessage());
        }
        return list;
    }

    public boolean save(Patient patient) {
        if (!DBConnection.isConnected() || patient == null) return false;

        String sql = "INSERT INTO patients (patient_id, name, age, gender, contact_number, address, department, visit_type, " +
                     "required_resource, emergency_level, priority, status, assigned_bed_id, " +
                     "assigned_doctor_name, assigned_hospital_id, arrival_sequence, registration_date_time) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE name=VALUES(name), age=VALUES(age), gender=VALUES(gender), " +
                     "contact_number=VALUES(contact_number), address=VALUES(address), " +
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
            stmt.setString(5, patient.getContactNumber());
            stmt.setString(6, patient.getAddress());
            stmt.setString(7, patient.getDepartment());
            stmt.setString(8, patient.getVisitType());
            stmt.setString(9, patient.getRequiredResource());
            stmt.setString(10, patient.getEmergencyLevel());
            stmt.setInt(11, patient.getPriority());
            stmt.setString(12, patient.getStatus());
            stmt.setString(13, patient.getAssignedBedId());
            stmt.setString(14, patient.getAssignedDoctorName());
            stmt.setString(15, patient.getAssignedHospitalId());
            stmt.setLong(16, patient.getArrivalSequence());
            stmt.setString(17, patient.getRegistrationDateTime());

            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[PatientDAO] save error: " + ex.getMessage());
            return false;
        }
    }

    public boolean update(Patient patient) {
        return save(patient);
    }

    public boolean updateDemographics(String patientId, String name, int age, String gender, String contact, String address) {
        if (!DBConnection.isConnected() || patientId == null) return false;

        String sql = "UPDATE patients SET name = ?, age = ?, gender = ?, contact_number = ?, address = ? WHERE patient_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setInt(2, age);
            stmt.setString(3, gender);
            stmt.setString(4, contact);
            stmt.setString(5, address);
            stmt.setString(6, patientId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[PatientDAO] updateDemographics error: " + ex.getMessage());
            return false;
        }
    }

    public boolean delete(String patientId) {
        if (!DBConnection.isConnected() || patientId == null) return false;

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
        try {
            p.setContactNumber(rs.getString("contact_number"));
            p.setAddress(rs.getString("address"));
        } catch (SQLException ignored) {}

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
