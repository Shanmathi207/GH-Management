package hms;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain model representing a Hospital facility.
 */
public class Hospital {

    private String hospitalId;
    private String hospitalName;
    private String location;
    private int generalBedCapacity;
    private int icuBedCapacity;

    private List<Bed> beds;
    private List<Doctor> doctors;

    public Hospital(String hospitalId, String hospitalName) {
        this(hospitalId, hospitalName, "Central District", 50, 15);
    }

    public Hospital(String hospitalId, String hospitalName, String location, int generalBedCapacity, int icuBedCapacity) {
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
        this.location = location;
        this.generalBedCapacity = generalBedCapacity;
        this.icuBedCapacity = icuBedCapacity;
        this.beds = new ArrayList<>();
        this.doctors = new ArrayList<>();
    }

    public String getHospitalId() {
        return hospitalId;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getGeneralBedCapacity() {
        return generalBedCapacity;
    }

    public void setGeneralBedCapacity(int generalBedCapacity) {
        this.generalBedCapacity = generalBedCapacity;
    }

    public int getIcuBedCapacity() {
        return icuBedCapacity;
    }

    public void setIcuBedCapacity(int icuBedCapacity) {
        this.icuBedCapacity = icuBedCapacity;
    }

    public List<Bed> getBeds() {
        return beds;
    }

    public List<Doctor> getDoctors() {
        return doctors;
    }

    public void addBed(Bed bed) {
        bed.setHospitalId(this.hospitalId);
        beds.add(bed);
    }

    public void addDoctor(Doctor doctor) {
        doctors.add(doctor);
    }

    public int getAvailableGeneralBedsCount() {
        int count = 0;
        for (Bed bed : beds) {
            if (bed instanceof GeneralBed && !bed.isOccupied() && !bed.isCleaning()) {
                count++;
            }
        }
        return count;
    }

    public int getAvailableICUBedsCount() {
        int count = 0;
        for (Bed bed : beds) {
            if (bed instanceof ICUBed && !bed.isOccupied() && !bed.isCleaning()) {
                count++;
            }
        }
        return count;
    }

    public void displayBeds() {
        System.out.println("\n========== BED STATUS FOR " + hospitalName + " ==========");
        for (Bed bed : beds) {
            bed.displayBed();
        }
    }

    public void displayDoctors() {
        System.out.println("\n========== DOCTOR STATUS FOR " + hospitalName + " ==========");
        for (Doctor doctor : doctors) {
            doctor.displayDoctor();
        }
    }
}
