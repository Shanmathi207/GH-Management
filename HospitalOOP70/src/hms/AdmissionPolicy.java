package hms;

/**
 * Interface or policy definition for patient admission priority and eligibility.
 */
public interface AdmissionPolicy {

    /**
     * Calculates priority integer based on emergency level string (1 is highest, 5 lowest).
     */
    int calculatePriority(String emergencyLevel);

    /**
     * Validates whether a patient record is complete and eligible for admission.
     */
    boolean isEligible(Patient patient);
}
