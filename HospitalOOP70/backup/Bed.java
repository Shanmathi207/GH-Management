package hms;

/**
 * Abstract domain model representing a hospital bed.
 * Subclasses include GeneralBed and ICUBed.
 */
public abstract class Bed {

    private String bedId;
    private String department;
    private String hospitalId;
    private boolean occupied;
    private boolean cleaning;
    private String patientId;

    public Bed(String bedId, String department) {
        this.bedId = bedId;
        this.department = department;
        this.hospitalId = "H001";
        this.occupied = false;
        this.cleaning = false;
        this.patientId = null;
    }

    public Bed(String bedId, String department, String hospitalId) {
        this.bedId = bedId;
        this.department = department;
        this.hospitalId = hospitalId;
        this.occupied = false;
        this.cleaning = false;
        this.patientId = null;
    }

    public String getBedId() {
        return bedId;
    }

    public String getDepartment() {
        return department;
    }

    public String getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(String hospitalId) {
        this.hospitalId = hospitalId;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public boolean isCleaning() {
        return cleaning;
    }

    public void setCleaning(boolean cleaning) {
        this.cleaning = cleaning;
        if (cleaning) {
            this.occupied = false;
        }
    }

    public String getPatientId() {
        return patientId;
    }

    public void admitPatient(String patientId) {
        if (!occupied && !cleaning) {
            this.occupied = true;
            this.cleaning = false;
            this.patientId = patientId;
        }
    }

    public void dischargePatient() {
        this.occupied = false;
        this.patientId = null;
        this.cleaning = true; // Bed enters CLEANING state after discharge
    }

    public void finishCleaning() {
        this.cleaning = false;
        this.occupied = false;
    }

    public String getStatus() {
        if (occupied) return "OCCUPIED";
        if (cleaning) return "CLEANING";
        return "AVAILABLE";
    }

    public abstract boolean supportsRequirement(String requirement);

    public abstract String getBedType();

    public void displayBed() {
        System.out.println(
            "Bed ID: " + bedId +
            " | Hospital: " + hospitalId +
            " | Type: " + getBedType() +
            " | Department: " + department +
            " | Status: " + getStatus() +
            (patientId != null ? (" | Patient: " + patientId) : "")
        );
    }
}
