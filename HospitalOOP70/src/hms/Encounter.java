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
    private String symptoms;
    private String diagnosis;
    private String treatmentNotes;
    private String notes;
    private String encounterDate;
    private String status; // "IN_PROGRESS", "COMPLETED", "CANCELLED"

    public Encounter(String encounterId, String patientId, String doctorId, String doctorName,
                     String department, String hospitalId, String notes) {
        this(encounterId, patientId, doctorId, doctorName, department, hospitalId, 
             "General Consultation", "Clinical Evaluation", notes, notes,
             LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), "COMPLETED");
    }

    public Encounter(String encounterId, String patientId, String doctorId, String doctorName,
                     String department, String hospitalId, String notes, String encounterDate, String status) {
        this(encounterId, patientId, doctorId, doctorName, department, hospitalId, 
             "General Consultation", "Clinical Evaluation", notes, notes,
             encounterDate, status);
    }

    public Encounter(String encounterId, String patientId, String doctorId, String doctorName,
                     String department, String hospitalId, String symptoms, String diagnosis,
                     String treatmentNotes, String notes, String encounterDate, String status) {
        this.encounterId = encounterId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.department = department;
        this.hospitalId = hospitalId;
        this.symptoms = symptoms != null ? symptoms : "Reported symptoms";
        this.diagnosis = diagnosis != null ? diagnosis : "Preliminary diagnosis";
        this.treatmentNotes = treatmentNotes != null ? treatmentNotes : notes;
        this.notes = notes != null ? notes : "";
        this.encounterDate = (encounterDate != null) ? encounterDate : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.status = (status != null) ? status : "COMPLETED";
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

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatmentNotes() {
        return treatmentNotes;
    }

    public void setTreatmentNotes(String treatmentNotes) {
        this.treatmentNotes = treatmentNotes;
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
            " | Diagnosis: " + diagnosis +
            " | Hospital: " + hospitalId +
            " | Date: " + encounterDate +
            " | Status: " + status
        );
    }
}
