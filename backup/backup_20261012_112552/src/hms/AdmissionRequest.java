package hms;

/**
 * Domain model representing a formal admission request for a patient.
 */
public class AdmissionRequest {

    private String requestId;
    private Patient patient;
    private long arrivalSequence;
    private String bedRequirement; // "General Bed" or "ICU Bed"
    private String hospitalId;
    private String status; // "PENDING", "ALLOCATED", "REJECTED", "TRANSFERRED"

    public AdmissionRequest(Patient patient, long arrivalSequence) {
        this("REQ-" + System.currentTimeMillis() % 100000, patient, arrivalSequence, 
             patient.getRequiredResource() != null ? patient.getRequiredResource() : "General Bed", 
             patient.getAssignedHospitalId() != null ? patient.getAssignedHospitalId() : "H001");
    }

    public AdmissionRequest(String requestId, Patient patient, long arrivalSequence, String bedRequirement, String hospitalId) {
        this.requestId = requestId;
        this.patient = patient;
        this.arrivalSequence = arrivalSequence;
        this.bedRequirement = bedRequirement;
        this.hospitalId = hospitalId;
        this.status = "PENDING";
    }

    public String getRequestId() {
        return requestId;
    }

    public Patient getPatient() {
        return patient;
    }

    public long getArrivalSequence() {
        return arrivalSequence;
    }

    public String getBedRequirement() {
        return bedRequirement;
    }

    public void setBedRequirement(String bedRequirement) {
        this.bedRequirement = bedRequirement;
    }

    public String getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(String hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void displayRequest() {
        System.out.println(
            "Req ID: " + requestId +
            " | Patient: " + (patient != null ? patient.getPatientId() + " (" + patient.getName() + ")" : "N/A") +
            " | Priority: " + (patient != null ? patient.getPriority() : "-") +
            " | Bed Type: " + bedRequirement +
            " | Hospital: " + hospitalId +
            " | Arrival Seq: " + arrivalSequence +
            " | Status: " + status
        );
    }
}
