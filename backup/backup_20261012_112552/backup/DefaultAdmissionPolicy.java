package hms;

/**
 * Standard implementation of AdmissionPolicy adhering to government hospital rules.
 */
public class DefaultAdmissionPolicy implements AdmissionPolicy {

    @Override
    public int calculatePriority(String emergencyLevel) {
        if (emergencyLevel == null) return 3;

        switch (emergencyLevel.trim().toUpperCase()) {
            case "CRITICAL":
            case "1":
                return 1;
            case "HIGH":
            case "2":
                return 2;
            case "MEDIUM":
            case "3":
                return 3;
            case "LOW":
            case "4":
                return 4;
            case "NON-URGENT":
            case "5":
            default:
                return 5;
        }
    }

    @Override
    public boolean isEligible(Patient patient) {
        if (patient == null) return false;
        if (patient.getName() == null || patient.getName().trim().isEmpty()) {
            return false;
        }
        if (patient.getAge() <= 0 || patient.getAge() > 130) {
            return false;
        }
        return true;
    }
}
