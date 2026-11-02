package hms;

/**
 * Concrete domain model for an Intensive Care Unit (ICU) Bed.
 */
public class ICUBed extends Bed {

    public ICUBed(String bedId, String department) {
        super(bedId, department);
    }

    public ICUBed(String bedId, String department, String hospitalId) {
        super(bedId, department, hospitalId);
    }

    @Override
    public boolean supportsRequirement(String requirement) {
        if (requirement == null) return false;
        return requirement.equalsIgnoreCase("ICU Bed") || 
               requirement.equalsIgnoreCase("ICU") ||
               requirement.equalsIgnoreCase("Intensive Care");
    }

    @Override
    public String getBedType() {
        return "ICU BED";
    }
}
