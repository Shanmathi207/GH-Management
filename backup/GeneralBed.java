package hms;

/**
 * Concrete domain model for a General Ward Bed.
 */
public class GeneralBed extends Bed {

    public GeneralBed(String bedId, String department) {
        super(bedId, department);
    }

    public GeneralBed(String bedId, String department, String hospitalId) {
        super(bedId, department, hospitalId);
    }

    @Override
    public boolean supportsRequirement(String requirement) {
        if (requirement == null) return false;
        return requirement.equalsIgnoreCase("General Bed") || 
               requirement.equalsIgnoreCase("General") ||
               requirement.equalsIgnoreCase("General Ward");
    }

    @Override
    public String getBedType() {
        return "GENERAL BED";
    }
}
