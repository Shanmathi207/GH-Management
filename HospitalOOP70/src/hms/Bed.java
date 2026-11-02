package hms;

/**
 * Abstract domain model representing a hospital bed.
 * Subclasses include GeneralBed and ICUBed.
 * 
 * Supported Bed States:
 * - AVAILABLE: Clean, vacant, ready for immediate admission
 * - RESERVED: Held for pending admission or inbound transfer
 * - OCCUPIED: Active inpatient admission
 * - CLEANING: Post-discharge turnaround and infection control sanitization
 * - MAINTENANCE: Under technical repair or inspection
 */
public abstract class Bed {

    private String bedId;
    private String department;
    private String hospitalId;
    private boolean occupied;
    private boolean cleaning;
    private String patientId;
    private String status = "AVAILABLE";

    public Bed(String bedId, String department) {
        this(bedId, department, "H001");
    }

    public Bed(String bedId, String department, String hospitalId) {
        this.bedId = bedId;
        this.department = department;
        this.hospitalId = hospitalId;
        this.occupied = false;
        this.cleaning = false;
        this.patientId = null;
        this.status = "AVAILABLE";
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
            this.status = "CLEANING";
        } else if ("CLEANING".equalsIgnoreCase(this.status)) {
            this.status = "AVAILABLE";
        }
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public boolean isAvailable() {
        return !occupied && !cleaning && 
               !"RESERVED".equalsIgnoreCase(status) && 
               !"MAINTENANCE".equalsIgnoreCase(status);
    }

    public void admitPatient(String patientId) {
        if (!occupied && !cleaning && !"MAINTENANCE".equalsIgnoreCase(status)) {
            this.occupied = true;
            this.cleaning = false;
            this.patientId = patientId;
            this.status = "OCCUPIED";
        }
    }

    public void reserveBed(String patientId) {
        if (isAvailable()) {
            this.status = "RESERVED";
            this.patientId = patientId;
        }
    }

    public void dischargePatient() {
        this.occupied = false;
        this.patientId = null;
        this.cleaning = true; // Bed enters CLEANING state after discharge
        this.status = "CLEANING";
    }

    public void finishCleaning() {
        this.cleaning = false;
        this.occupied = false;
        this.status = "AVAILABLE";
    }

    public void setMaintenance(boolean underMaintenance) {
        if (underMaintenance) {
            this.occupied = false;
            this.cleaning = false;
            this.status = "MAINTENANCE";
        } else if ("MAINTENANCE".equalsIgnoreCase(this.status)) {
            this.status = "AVAILABLE";
        }
    }

    public String getStatus() {
        if (occupied) return "OCCUPIED";
        if (cleaning) return "CLEANING";
        if (status != null && !status.trim().isEmpty()) return status.toUpperCase();
        return "AVAILABLE";
    }

    public void setStatus(String newStatus) {
        if (newStatus == null) return;
        this.status = newStatus.toUpperCase();
        if ("OCCUPIED".equalsIgnoreCase(this.status)) {
            this.occupied = true;
            this.cleaning = false;
        } else if ("CLEANING".equalsIgnoreCase(this.status)) {
            this.occupied = false;
            this.cleaning = true;
        } else {
            this.occupied = false;
            this.cleaning = false;
        }
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
