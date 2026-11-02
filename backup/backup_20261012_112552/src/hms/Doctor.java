package hms;

/**
 * Domain model representing a Medical Doctor.
 */
public class Doctor {

    private String doctorId;
    private String name;
    private String department;
    private String hospitalId;
    private String hospitalName;
    private boolean onDuty;

    public Doctor(String doctorId, String name, String department, boolean onDuty) {
        this.doctorId = doctorId;
        this.name = name;
        this.department = department;
        this.hospitalId = "H001";
        this.hospitalName = "Government General Hospital";
        this.onDuty = onDuty;
    }

    public Doctor(String doctorId, String name, String department, String hospitalId, String hospitalName, boolean onDuty) {
        this.doctorId = doctorId;
        this.name = name;
        this.department = department;
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
        this.onDuty = onDuty;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public String getHospitalId() {
        return hospitalId;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public boolean isOnDuty() {
        return onDuty;
    }

    public void setDutyStatus(boolean status) {
        this.onDuty = status;
    }

    public void setOnDuty(boolean onDuty) {
        this.onDuty = onDuty;
    }

    public void displayDoctor() {
        System.out.println(
            "Doctor ID: " + doctorId +
            " | Name: " + name +
            " | Department: " + department +
            " | Hospital: " + hospitalName +
            " | Status: " + (onDuty ? "ON DUTY" : "OFF DUTY")
        );
    }
}
