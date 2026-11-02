package hms;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Domain model representing a Patient in the Hospital Management System.
 */
public class Patient {

    private String patientId;
    private String name;
    private int age;
    private String gender;
    private String department;
    private String visitType; // "OPD", "IPD", "EMERGENCY"
    private String requiredResource; // "General Bed", "ICU Bed", "Consultation"
    private String emergencyLevel; // "CRITICAL", "HIGH", "MEDIUM", "LOW", "NON-URGENT"
    private int priority; // 1 (Highest) to 5 (Lowest)
    private String status; // "WAITING FOR PROCESSING", "IN QUEUE", "ADMITTED", "DISCHARGED", etc.
    private String registrationDateTime;
    private String assignedBedId;
    private String assignedDoctorName;
    private String assignedHospitalId;
    private long arrivalSequence;

    public Patient(String patientId, String name, int age, String gender,
                   String department, String requiredResource,
                   String emergencyLevel, int priority) {
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.gender = gender != null ? gender : "Unspecified";
        this.department = department != null ? department : "General Medicine";
        this.visitType = "OPD";
        this.requiredResource = requiredResource != null ? requiredResource : "General Bed";
        this.emergencyLevel = emergencyLevel != null ? emergencyLevel : "MEDIUM";
        this.priority = priority > 0 ? priority : 3;
        this.status = "WAITING FOR PROCESSING";
        this.registrationDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.assignedBedId = null;
        this.assignedDoctorName = null;
        this.assignedHospitalId = "H001";
        this.arrivalSequence = System.currentTimeMillis();
    }

    public Patient(String patientId, String name, int age, String gender,
                   String visitType, int severity, String department) {
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.gender = gender != null ? gender : "Unspecified";
        this.visitType = visitType != null ? visitType : "OPD";
        this.priority = severity;
        this.emergencyLevel = mapSeverityToLevel(severity);
        this.department = department != null ? department : "General Medicine";
        this.requiredResource = severity <= 2 ? "ICU Bed" : "General Bed";
        this.status = "WAITING IN QUEUE";
        this.registrationDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.assignedBedId = null;
        this.assignedDoctorName = null;
        this.assignedHospitalId = "H001";
        this.arrivalSequence = System.currentTimeMillis();
    }

    private static String mapSeverityToLevel(int severity) {
        switch (severity) {
            case 1: return "CRITICAL";
            case 2: return "HIGH";
            case 3: return "MEDIUM";
            case 4: return "LOW";
            default: return "NON-URGENT";
        }
    }

    public String getPatientId() {
        return patientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    public String getRequiredResource() {
        return requiredResource;
    }

    public void setRequiredResource(String requiredResource) {
        this.requiredResource = requiredResource;
    }

    public String getEmergencyLevel() {
        return emergencyLevel;
    }

    public void setEmergencyLevel(String emergencyLevel) {
        this.emergencyLevel = emergencyLevel;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRegistrationDateTime() {
        return registrationDateTime;
    }

    public void setRegistrationDateTime(String registrationDateTime) {
        this.registrationDateTime = registrationDateTime;
    }

    public String getAssignedBedId() {
        return assignedBedId;
    }

    public void setAssignedBedId(String assignedBedId) {
        this.assignedBedId = assignedBedId;
    }

    public String getAssignedDoctorName() {
        return assignedDoctorName;
    }

    public void setAssignedDoctorName(String assignedDoctorName) {
        this.assignedDoctorName = assignedDoctorName;
    }

    public String getAssignedHospitalId() {
        return assignedHospitalId;
    }

    public void setAssignedHospitalId(String assignedHospitalId) {
        this.assignedHospitalId = assignedHospitalId;
    }

    public long getArrivalSequence() {
        return arrivalSequence;
    }

    public void setArrivalSequence(long arrivalSequence) {
        this.arrivalSequence = arrivalSequence;
    }

    public void displayPatient() {
        System.out.println("----------------------------------------");
        System.out.println("Patient ID       : " + patientId);
        System.out.println("Name             : " + name);
        System.out.println("Age              : " + age);
        System.out.println("Gender           : " + gender);
        System.out.println("Visit Type       : " + visitType);
        System.out.println("Department       : " + department);
        System.out.println("Required Resource: " + requiredResource);
        System.out.println("Emergency Level  : " + emergencyLevel);
        System.out.println("Priority         : " + priority);
        System.out.println("Status           : " + status);
        System.out.println("Registered At    : " + registrationDateTime);
        System.out.println("----------------------------------------");
    }
}
