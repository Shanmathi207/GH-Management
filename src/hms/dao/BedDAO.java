package hms.dao;

import hms.Bed;
import hms.GeneralBed;
import hms.ICUBed;
import hms.db.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BedDAO manages inpatient bed lifecycle, occupancy, and sanitization flags in MySQL.
 * Enforces transactional concurrency control to prevent double bed allocation.
 * Preserves the Bed, GeneralBed, and ICUBed inheritance structure.
 */
public class BedDAO {

    public List<Bed> findAll() {
        List<Bed> list = new ArrayList<>();
        if (!DBConnection.isConnected()) return list;

        String sql = "SELECT bed_id, department, hospital_id, bed_type, occupied, cleaning, patient_id, status FROM beds ORDER BY bed_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String bedId = rs.getString("bed_id");
                String dept = rs.getString("department");
                String hospId = rs.getString("hospital_id");
                String type = rs.getString("bed_type");
                boolean occ = rs.getBoolean("occupied");
                boolean clean = rs.getBoolean("cleaning");
                String patientId = rs.getString("patient_id");
                String status = rs.getString("status");

                Bed bed;
                if (type != null && type.toUpperCase().contains("ICU")) {
                    bed = new ICUBed(bedId, dept, hospId);
                } else {
                    bed = new GeneralBed(bedId, dept, hospId);
                }

                if (status != null && !status.trim().isEmpty()) {
                    bed.setStatus(status);
                    bed.setPatientId(patientId);
                } else if (occ) {
                    bed.admitPatient(patientId);
                } else if (clean) {
                    bed.setCleaning(true);
                }

                list.add(bed);
            }
        } catch (SQLException ex) {
            System.err.println("[BedDAO] findAll error: " + ex.getMessage());
        }
        return list;
    }

    public boolean updateBedStatus(String bedId, boolean occupied, boolean cleaning, String patientId) {
        String status = occupied ? "OCCUPIED" : (cleaning ? "CLEANING" : "AVAILABLE");
        return updateBedStatus(bedId, occupied, cleaning, patientId, status);
    }

    public boolean updateBedStatus(String bedId, boolean occupied, boolean cleaning, String patientId, String status) {
        if (!DBConnection.isConnected() || bedId == null) return false;

        String sql = "UPDATE beds SET occupied = ?, cleaning = ?, patient_id = ?, status = ? WHERE bed_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, occupied);
            stmt.setBoolean(2, cleaning);
            stmt.setString(3, patientId);
            stmt.setString(4, status != null ? status : (occupied ? "OCCUPIED" : (cleaning ? "CLEANING" : "AVAILABLE")));
            stmt.setString(5, bedId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[BedDAO] updateBedStatus error: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Atomically allocates a bed to a patient, preventing concurrent double-booking.
     */
    public boolean allocateBedAtomic(String bedId, String patientId) {
        if (!DBConnection.isConnected() || bedId == null || patientId == null) return false;

        String sql = "UPDATE beds SET occupied = TRUE, cleaning = FALSE, patient_id = ?, status = 'OCCUPIED' " +
                     "WHERE bed_id = ? AND occupied = FALSE AND cleaning = FALSE AND (status = 'AVAILABLE' OR status IS NULL)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patientId);
            stmt.setString(2, bedId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[BedDAO] allocateBedAtomic error: " + ex.getMessage());
            return false;
        }
    }

    public boolean save(Bed bed) {
        if (!DBConnection.isConnected() || bed == null) return false;

        String sql = "INSERT INTO beds (bed_id, department, hospital_id, bed_type, occupied, cleaning, patient_id, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE department=VALUES(department), hospital_id=VALUES(hospital_id), " +
                     "bed_type=VALUES(bed_type), occupied=VALUES(occupied), cleaning=VALUES(cleaning), " +
                     "patient_id=VALUES(patient_id), status=VALUES(status)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, bed.getBedId());
            stmt.setString(2, bed.getDepartment());
            stmt.setString(3, bed.getHospitalId());
            stmt.setString(4, bed.getBedType());
            stmt.setBoolean(5, bed.isOccupied());
            stmt.setBoolean(6, bed.isCleaning());
            stmt.setString(7, bed.getPatientId());
            stmt.setString(8, bed.getStatus());

            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[BedDAO] save error: " + ex.getMessage());
            return false;
        }
    }
}
