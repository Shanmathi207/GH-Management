package hms;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Domain model representing an OPD or Emergency medical consultation encounter.
 */
public class Encounter {

    private String encounterId;
    private String patientId;
    private String doctorId;
    private String doctorName;
    private String department;
    private String hospitalId;
    private String notes;
    private String encounterDate;
    private String status; // "IN_PROGRESS", "COMPLETED", "CANCELLED"

    public Encounter(String encounterId, String patientId, String doctorId, String doctorName,
                     String department, String hospitalId, String notes) {
        this.encounterId = encounterId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.department = department;
        this.hospitalId = hospitalId;
        this.notes = notes;
        this.encounterDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.status = "COMPLETED";
    }

    public String getEncounterId() {
        return encounterId;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getDepartment() {
        return department;
    }

    public String getHospitalId() {
        return hospitalId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getEncounterDate() {
        return encounterDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void displayEncounter() {
        System.out.println(
            "Encounter: " + encounterId +
            " | Patient: " + patientId +
            " | Doctor: " + doctorName + " (" + department + ")" +
            " | Hospital: " + hospitalId +
            " | Date: " + encounterDate +
            " | Status: " + status
        );
    }
}
