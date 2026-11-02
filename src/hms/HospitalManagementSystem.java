package hms;

import java.util.*;

/**
 * HospitalManagementSystem acts as the primary domain facade coordinating
 * patients, beds, doctors, medical facilities, admissions, priority queues, and referrals.
 */
public class HospitalManagementSystem {

    private final Map<String, Patient> patients;
    private final Map<String, Bed> beds;
    private final Map<String, Doctor> doctors;
    private final Map<String, Hospital> hospitals;
    private final List<Encounter> encounters;
    private final List<AdmissionRequest> admissionRequests;

    private final Queue<AdmissionRequest> waitingQueue;
    private final PriorityQueue<AdmissionRequest> priorityQueue;
    private final List<String> allocationHistory;

    private final AdmissionPolicy admissionPolicy;
    private final RoutePlanner routePlanner;
    private final ReferralService referralService;

    private Hospital primaryHospital;
    private int patientCounter = 100;
    private long arrivalCounter = 0;

    public HospitalManagementSystem() {
        this.patients = new LinkedHashMap<>();
        this.beds = new LinkedHashMap<>();
        this.doctors = new LinkedHashMap<>();
        this.hospitals = new LinkedHashMap<>();
        this.encounters = new ArrayList<>();
        this.admissionRequests = new ArrayList<>();

        this.waitingQueue = new LinkedList<>();
        this.allocationHistory = new ArrayList<>();

        this.admissionPolicy = new DefaultAdmissionPolicy();
        this.routePlanner = new RoutePlanner();
        this.referralService = new ReferralService(routePlanner);

        // Comparator: Priority ascending (1 is highest), then arrival sequence ascending
        this.priorityQueue = new PriorityQueue<>((a, b) -> {
            int pComp = Integer.compare(a.getPatient().getPriority(), b.getPatient().getPriority());
            if (pComp != 0) return pComp;
            return Long.compare(a.getArrivalSequence(), b.getArrivalSequence());
        });

        initializeSampleData();
    }

    private void initializeSampleData() {
        // Hospitals
        primaryHospital = new Hospital("H001", "Government General Hospital", "Central District", 80, 20);
        Hospital h2 = new Hospital("H002", "Government Multi-Specialty Hospital", "North Zone", 120, 30);
        Hospital h3 = new Hospital("H003", "District Medical College & Hospital", "East Zone", 150, 40);
        Hospital h4 = new Hospital("H004", "City Emergency & Trauma Center", "South Zone", 60, 25);
        Hospital h5 = new Hospital("H005", "Suburban Community Health Center", "West Zone", 40, 10);

        addHospital(primaryHospital);
        addHospital(h2);
        addHospital(h3);
        addHospital(h4);
        addHospital(h5);

        // Beds for Primary Hospital
        addBed(new ICUBed("ICU-101", "ICU", "H001"));
        addBed(new ICUBed("ICU-102", "ICU", "H001"));
        addBed(new ICUBed("ICU-103", "ICU", "H001"));
        addBed(new GeneralBed("GEN-201", "General Medicine", "H001"));
        addBed(new GeneralBed("GEN-202", "General Medicine", "H001"));
        addBed(new GeneralBed("GEN-203", "General Medicine", "H001"));
        addBed(new GeneralBed("SUR-301", "Surgery", "H001"));
        addBed(new GeneralBed("SUR-302", "Surgery", "H001"));
        addBed(new GeneralBed("CAR-401", "Cardiology", "H001"));
        addBed(new GeneralBed("PED-501", "Pediatrics", "H001"));

        // Beds for other hospitals
        addBed(new ICUBed("ICU-H201", "ICU", "H002"));
        addBed(new GeneralBed("GEN-H201", "General Ward", "H002"));
        addBed(new ICUBed("ICU-H301", "ICU", "H003"));
        addBed(new GeneralBed("GEN-H301", "General Ward", "H003"));
        addBed(new ICUBed("ICU-H401", "ICU", "H004"));
        addBed(new GeneralBed("GEN-H401", "General Ward", "H004"));

        // Doctors
        addDoctor(new Doctor("DOC-01", "Dr. Rajesh Sharma", "ICU", "H001", "Government General Hospital", true));
        addDoctor(new Doctor("DOC-02", "Dr. Ananya Iyer", "Surgery", "H001", "Government General Hospital", true));
        addDoctor(new Doctor("DOC-03", "Dr. Vikram Patel", "Cardiology", "H001", "Government General Hospital", false));
        addDoctor(new Doctor("DOC-04", "Dr. Sneha Roy", "General Medicine", "H001", "Government General Hospital", true));
        addDoctor(new Doctor("DOC-05", "Dr. Manoj Nair", "Pediatrics", "H001", "Government General Hospital", true));
        addDoctor(new Doctor("DOC-06", "Dr. Kavita Joshi", "Trauma", "H004", "City Emergency & Trauma Center", true));

        // Sample Patients
        registerPatientDirect(new Patient("PAT-101", "Ramesh Kumar", 54, "Male", "OPD", 1, "Cardiology"));
        registerPatientDirect(new Patient("PAT-102", "Lakshmi Devi", 42, "Female", "IPD", 2, "General Medicine"));
        registerPatientDirect(new Patient("PAT-103", "Suresh Menon", 29, "Male", "EMERGENCY", 1, "Trauma"));
        registerPatientDirect(new Patient("PAT-104", "Meenakshi Sundaram", 68, "Female", "OPD", 3, "General Medicine"));
        registerPatientDirect(new Patient("PAT-105", "Abdul Rahman", 35, "Male", "OPD", 4, "Pediatrics"));
    }

    public void addHospital(Hospital hospital) {
        hospitals.put(hospital.getHospitalId(), hospital);
        routePlanner.addHospital(hospital.getHospitalId());
    }

    public void addBed(Bed bed) {
        beds.put(bed.getBedId(), bed);
        Hospital h = hospitals.get(bed.getHospitalId());
        if (h != null) {
            h.addBed(bed);
        }
    }

    public void addDoctor(Doctor doctor) {
        doctors.put(doctor.getDoctorId(), doctor);
        Hospital h = hospitals.get(doctor.getHospitalId());
        if (h != null) {
            h.addDoctor(doctor);
        }
    }

    public void registerPatientDirect(Patient patient) {
        patients.put(patient.getPatientId(), patient);
        arrivalCounter++;
        patient.setArrivalSequence(arrivalCounter);
        AdmissionRequest req = new AdmissionRequest(patient, arrivalCounter);
        priorityQueue.add(req);
        admissionRequests.add(req);
    }

    public Patient registerPatient(String name, int age, String gender, String visitType, int severity, String department) {
        patientCounter++;
        String pId = "PAT-" + patientCounter;
        Patient p = new Patient(pId, name, age, gender, visitType, severity, department);
        if (!admissionPolicy.isEligible(p)) {
            return null;
        }
        registerPatientDirect(p);
        allocationHistory.add("REGISTRATION: " + pId + " (" + name + ") registered under " + visitType + " [Severity " + severity + "]");
        return p;
    }

    public Bed findCompatibleBed(String hospitalId, String requiredType) {
        for (Bed bed : beds.values()) {
            if (bed.getHospitalId().equalsIgnoreCase(hospitalId) && 
                !bed.isOccupied() && !bed.isCleaning() && 
                bed.supportsRequirement(requiredType)) {
                return bed;
            }
        }
        return null;
    }

    public boolean allocateBed(String patientId, String bedId) {
        Patient p = patients.get(patientId);
        Bed b = beds.get(bedId);
        if (p == null || b == null) return false;
        if (b.isOccupied() || b.isCleaning()) return false;

        b.admitPatient(patientId);
        p.setAssignedBedId(bedId);
        p.setStatus("ADMITTED - " + b.getBedType());
        allocationHistory.add("BED ALLOCATION: Patient " + patientId + " admitted to Bed " + bedId);
        return true;
    }

    public boolean dischargePatient(String patientId) {
        Patient p = patients.get(patientId);
        if (p == null) return false;

        for (Bed b : beds.values()) {
            if (patientId.equalsIgnoreCase(b.getPatientId())) {
                b.dischargePatient(); // transitions to cleaning
                p.setStatus("DISCHARGED");
                p.setAssignedBedId(null);
                allocationHistory.add("DISCHARGE: Patient " + patientId + " discharged from Bed " + b.getBedId() + " (Bed sent to CLEANING)");
                return true;
            }
        }
        return false;
    }

    public boolean finishBedCleaning(String bedId) {
        Bed b = beds.get(bedId);
        if (b != null && b.isCleaning()) {
            b.finishCleaning();
            allocationHistory.add("MAINTENANCE: Bed " + bedId + " sanitized and returned to AVAILABLE");
            return true;
        }
        return false;
    }

    // Getters
    public Map<String, Patient> getPatients() { return patients; }
    public Map<String, Bed> getBeds() { return beds; }
    public Map<String, Doctor> getDoctors() { return doctors; }
    public Map<String, Hospital> getHospitals() { return hospitals; }
    public List<Encounter> getEncounters() { return encounters; }
    public List<AdmissionRequest> getAdmissionRequests() { return admissionRequests; }
    public PriorityQueue<AdmissionRequest> getPriorityQueue() { return priorityQueue; }
    public Queue<AdmissionRequest> getWaitingQueue() { return waitingQueue; }
    public List<String> getAllocationHistory() { return allocationHistory; }
    public RoutePlanner getRoutePlanner() { return routePlanner; }
    public ReferralService getReferralService() { return referralService; }
    public Hospital getPrimaryHospital() { return primaryHospital; }
}
