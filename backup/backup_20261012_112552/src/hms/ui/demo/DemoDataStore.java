package hms.ui.demo;

import hms.*;
import hms.dao.*;
import hms.db.DBConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Shared data coordinator for the Government Hospital Management System.
 * Bridges Java Swing UI components with domain services and MySQL DAO persistence.
 * 
 * Features:
 * - Persistent MySQL storage via DAO layer when connected (configured via DB_PASSWORD).
 * - Graceful fallback to rich in-memory sample data if database is offline.
 * - Reactive change listener synchronization across all Swing tabs.
 * - Maintains existing OOP hierarchy (Bed, GeneralBed, ICUBed), PriorityQueue, and Dijkstra routing.
 */
public class DemoDataStore {

    private static DemoDataStore instance;

    public static synchronized DemoDataStore getInstance() {
        if (instance == null) {
            instance = new DemoDataStore();
        }
        return instance;
    }

    public static class HistoryRecord {
        private final String historyId;
        private final String patientId;
        private final String activityType; // REGISTRATION, CONSULTATION, ADMISSION, DISCHARGE, BED CLEANING, REFERRAL, DUTY UPDATE
        private final String description;
        private final String timestamp;
        private final String staffUser;

        public HistoryRecord(String historyId, String patientId, String activityType, String description, String staffUser) {
            this(historyId, patientId, activityType, description, staffUser,
                 LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        public HistoryRecord(String historyId, String patientId, String activityType, String description, String staffUser, String timestamp) {
            this.historyId = historyId;
            this.patientId = patientId;
            this.activityType = activityType;
            this.description = description;
            this.staffUser = staffUser != null ? staffUser : "Staff Officer";
            this.timestamp = timestamp != null ? timestamp : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        public String getHistoryId() { return historyId; }
        public String getPatientId() { return patientId; }
        public String getActivityType() { return activityType; }
        public String getDescription() { return description; }
        public String getTimestamp() { return timestamp; }
        public String getStaffUser() { return staffUser; }
    }

    public static class ReferralRecord {
        private final String referralId;
        private final String patientId;
        private final String patientName;
        private final String sourceHospital;
        private final String destHospital;
        private final String bedType;
        private final int travelMinutes;
        private final String timestamp;

        public ReferralRecord(String referralId, String patientId, String patientName, 
                              String sourceHospital, String destHospital, String bedType, int travelMinutes) {
            this(referralId, patientId, patientName, sourceHospital, destHospital, bedType, travelMinutes,
                 LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        public ReferralRecord(String referralId, String patientId, String patientName, 
                              String sourceHospital, String destHospital, String bedType, int travelMinutes, String timestamp) {
            this.referralId = referralId;
            this.patientId = patientId;
            this.patientName = patientName;
            this.sourceHospital = sourceHospital;
            this.destHospital = destHospital;
            this.bedType = bedType;
            this.travelMinutes = travelMinutes;
            this.timestamp = timestamp != null ? timestamp : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        public String getReferralId() { return referralId; }
        public String getPatientId() { return patientId; }
        public String getPatientName() { return patientName; }
        public String getSourceHospital() { return sourceHospital; }
        public String getDestHospital() { return destHospital; }
        public String getBedType() { return bedType; }
        public int getTravelMinutes() { return travelMinutes; }
        public String getTimestamp() { return timestamp; }
    }

    // Collections
    private final List<Patient> patients = new ArrayList<>();
    private final Map<String, Patient> patientMap = new HashMap<>();
    private final List<Hospital> hospitals = new ArrayList<>();
    private final Map<String, Hospital> hospitalMap = new HashMap<>();
    private final List<Doctor> doctors = new ArrayList<>();
    private final Map<String, Doctor> doctorMap = new HashMap<>();
    private final List<Bed> beds = new ArrayList<>();
    private final Map<String, Bed> bedMap = new HashMap<>();
    private final List<Encounter> encounters = new ArrayList<>();
    private final List<AdmissionRequest> admissionRequests = new ArrayList<>();
    private final List<ReferralRecord> referrals = new ArrayList<>();
    private final List<HistoryRecord> activityHistory = new ArrayList<>();

    // Priority Queue with deterministic tie-breaking:
    // 1. Severity ascending (1 is highest priority)
    // 2. Arrival time ascending
    // 3. Sequence number ascending
    private final PriorityQueue<Patient> priorityQueue;

    // Route planner & Referral Service
    private final RoutePlanner routePlanner;
    private final ReferralService referralService;

    // Counters
    private int patientCounter = 100;
    private int encounterCounter = 500;
    private int admissionCounter = 300;
    private int referralCounter = 700;
    private int historyCounter = 1000;
    private long sequenceCounter = 0;
    private int totalDischarges = 0;

    // DAO Layer
    private final HospitalDAO hospitalDAO = new HospitalDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final BedDAO bedDAO = new BedDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final AdmissionRequestDAO admissionRequestDAO = new AdmissionRequestDAO();
    private final AdmissionDAO admissionDAO = new AdmissionDAO();
    private final EncounterDAO encounterDAO = new EncounterDAO();
    private final ReferralDAO referralDAO = new ReferralDAO();
    private final HistoryDAO historyDAO = new HistoryDAO();

    // Change listeners for UI synchronization
    private final List<Runnable> changeListeners = new ArrayList<>();

    private DemoDataStore() {
        this.routePlanner = new RoutePlanner();
        this.referralService = new ReferralService(routePlanner);

        this.priorityQueue = new PriorityQueue<>((p1, p2) -> {
            int sevComp = Integer.compare(p1.getPriority(), p2.getPriority());
            if (sevComp != 0) return sevComp;
            int timeComp = p1.getRegistrationDateTime().compareTo(p2.getRegistrationDateTime());
            if (timeComp != 0) return timeComp;
            return Long.compare(p1.getArrivalSequence(), p2.getArrivalSequence());
        });

        if (DBConnection.isConnected()) {
            System.out.println("[DemoDataStore] MySQL is connected. Loading persistent data from database...");
            loadFromDatabase();
        } else {
            System.out.println("[DemoDataStore] Running in offline demo mode. Loading default in-memory seed data...");
            seedInitialDemoData();
        }
    }

    public synchronized void addChangeListener(Runnable listener) {
        if (listener != null && !changeListeners.contains(listener)) {
            changeListeners.add(listener);
        }
    }

    public synchronized void notifyListeners() {
        for (Runnable r : new ArrayList<>(changeListeners)) {
            try {
                r.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // ==================== PERSISTENT DATABASE LOADING ====================
    private void loadFromDatabase() {
        try {
            // 1. Load Hospitals
            List<Hospital> dbHospitals = hospitalDAO.findAll();
            if (dbHospitals.isEmpty()) {
                seedInitialHospitals();
            } else {
                for (Hospital h : dbHospitals) {
                    hospitals.add(h);
                    hospitalMap.put(h.getHospitalId(), h);
                    routePlanner.addHospital(h.getHospitalId());
                }
            }

            // 2. Load Beds (preserves ICUBed and GeneralBed inheritance)
            List<Bed> dbBeds = bedDAO.findAll();
            if (dbBeds.isEmpty()) {
                seedInitialBeds();
            } else {
                for (Bed b : dbBeds) {
                    beds.add(b);
                    bedMap.put(b.getBedId(), b);
                    Hospital h = hospitalMap.get(b.getHospitalId());
                    if (h != null) h.addBed(b);
                }
            }

            // 3. Load Doctors
            List<Doctor> dbDoctors = doctorDAO.findAll();
            if (dbDoctors.isEmpty()) {
                seedInitialDoctors();
            } else {
                for (Doctor d : dbDoctors) {
                    doctors.add(d);
                    doctorMap.put(d.getDoctorId(), d);
                    Hospital h = hospitalMap.get(d.getHospitalId());
                    if (h != null) h.addDoctor(d);
                }
            }

            // 4. Load Patients
            List<Patient> dbPatients = patientDAO.findAll();
            if (dbPatients.isEmpty()) {
                seedInitialPatients();
            } else {
                for (Patient p : dbPatients) {
                    patients.add(p);
                    patientMap.put(p.getPatientId(), p);

                    try {
                        if (p.getPatientId().startsWith("PAT-")) {
                            int num = Integer.parseInt(p.getPatientId().substring(4));
                            if (num > patientCounter) patientCounter = num;
                        }
                    } catch (Exception ignored) {}

                    if (p.getArrivalSequence() > sequenceCounter) {
                        sequenceCounter = p.getArrivalSequence();
                    }

                    // Populate priority queue for active waiting patients
                    String st = p.getStatus() != null ? p.getStatus().toUpperCase() : "";
                    if (st.contains("WAITING") || st.contains("QUEUE")) {
                        priorityQueue.add(p);
                    }
                }
            }

            // 5. Load Admission Requests
            List<AdmissionRequest> dbReqs = admissionRequestDAO.findAll(patientMap);
            for (AdmissionRequest req : dbReqs) {
                admissionRequests.add(req);
                try {
                    if (req.getRequestId().startsWith("REQ-")) {
                        int num = Integer.parseInt(req.getRequestId().substring(4));
                        if (num > admissionCounter) admissionCounter = num;
                    }
                } catch (Exception ignored) {}
            }

            // 6. Load Encounters
            List<Encounter> dbEnc = encounterDAO.findAll();
            for (Encounter enc : dbEnc) {
                encounters.add(enc);
                try {
                    if (enc.getEncounterId().startsWith("ENC-")) {
                        int num = Integer.parseInt(enc.getEncounterId().substring(4));
                        if (num > encounterCounter) encounterCounter = num;
                    }
                } catch (Exception ignored) {}
            }

            // 7. Load Referrals
            List<ReferralRecord> dbRefs = referralDAO.findAll();
            for (ReferralRecord ref : dbRefs) {
                referrals.add(ref);
                try {
                    if (ref.getReferralId().startsWith("REF-")) {
                        int num = Integer.parseInt(ref.getReferralId().substring(4));
                        if (num > referralCounter) referralCounter = num;
                    }
                } catch (Exception ignored) {}
            }

            // 8. Load History
            List<HistoryRecord> dbHist = historyDAO.findAll();
            for (HistoryRecord h : dbHist) {
                activityHistory.add(h);
                try {
                    if (h.getHistoryId().startsWith("HIST-")) {
                        int num = Integer.parseInt(h.getHistoryId().substring(5));
                        if (num > historyCounter) historyCounter = num;
                    }
                } catch (Exception ignored) {}
            }

            // 9. Load Discharges
            totalDischarges = admissionDAO.getDischargeCount();
            if (totalDischarges == 0) totalDischarges = 3;

            System.out.println("[DemoDataStore] MySQL persistent data loaded successfully: " +
                patients.size() + " patients, " + beds.size() + " beds, " + priorityQueue.size() + " in priority queue.");
        } catch (Exception ex) {
            System.err.println("[DemoDataStore] Error loading from database, falling back to seed data: " + ex.getMessage());
            ex.printStackTrace();
            seedInitialDemoData();
        }
    }

    private void seedInitialHospitals() {
        Hospital h1 = new Hospital("H001", "Government General Hospital", "Central District", 80, 20);
        Hospital h2 = new Hospital("H002", "Government Multi-Specialty Hospital", "North Zone", 120, 30);
        Hospital h3 = new Hospital("H003", "District Medical College & Hospital", "East Zone", 150, 40);
        Hospital h4 = new Hospital("H004", "City Emergency & Trauma Center", "South Zone", 60, 25);
        Hospital h5 = new Hospital("H005", "Suburban Community Health Center", "West Zone", 40, 10);

        for (Hospital h : Arrays.asList(h1, h2, h3, h4, h5)) {
            hospitals.add(h);
            hospitalMap.put(h.getHospitalId(), h);
            routePlanner.addHospital(h.getHospitalId());
            if (DBConnection.isConnected()) {
                hospitalDAO.save(h);
            }
        }
    }

    private void seedInitialBeds() {
        addDemoBed(new ICUBed("ICU-101", "ICU", "H001"));
        addDemoBed(new ICUBed("ICU-102", "ICU", "H001"));
        addDemoBed(new ICUBed("ICU-103", "ICU", "H001"));
        addDemoBed(new ICUBed("ICU-104", "ICU", "H001"));
        addDemoBed(new GeneralBed("GEN-201", "General Medicine", "H001"));
        addDemoBed(new GeneralBed("GEN-202", "General Medicine", "H001"));
        addDemoBed(new GeneralBed("GEN-203", "General Medicine", "H001"));
        addDemoBed(new GeneralBed("GEN-204", "General Medicine", "H001"));
        addDemoBed(new GeneralBed("SUR-301", "Surgery", "H001"));
        addDemoBed(new GeneralBed("SUR-302", "Surgery", "H001"));
        addDemoBed(new GeneralBed("CAR-401", "Cardiology", "H001"));
        addDemoBed(new GeneralBed("PED-501", "Pediatrics", "H001"));

        addDemoBed(new ICUBed("ICU-H201", "ICU", "H002"));
        addDemoBed(new GeneralBed("GEN-H201", "General Ward", "H002"));
        addDemoBed(new ICUBed("ICU-H301", "ICU", "H003"));
        addDemoBed(new GeneralBed("GEN-H301", "General Ward", "H003"));
        addDemoBed(new ICUBed("ICU-H401", "ICU", "H004"));
        addDemoBed(new GeneralBed("GEN-H401", "General Ward", "H004"));
        addDemoBed(new GeneralBed("GEN-H501", "General Ward", "H005"));

        if (DBConnection.isConnected()) {
            for (Bed b : beds) {
                bedDAO.save(b);
            }
        }
    }

    private void seedInitialDoctors() {
        addDemoDoctor(new Doctor("DOC-01", "Dr. Rajesh Sharma", "Cardiology", "H001", "Government General Hospital", true));
        addDemoDoctor(new Doctor("DOC-02", "Dr. Ananya Iyer", "ICU / Critical Care", "H001", "Government General Hospital", true));
        addDemoDoctor(new Doctor("DOC-03", "Dr. Vikram Patel", "General Medicine", "H001", "Government General Hospital", true));
        addDemoDoctor(new Doctor("DOC-04", "Dr. Sneha Roy", "Surgery", "H001", "Government General Hospital", false));
        addDemoDoctor(new Doctor("DOC-05", "Dr. Manoj Nair", "Pediatrics", "H001", "Government General Hospital", true));
        addDemoDoctor(new Doctor("DOC-06", "Dr. Kavita Joshi", "Emergency / Trauma", "H004", "City Emergency & Trauma Center", true));
        addDemoDoctor(new Doctor("DOC-07", "Dr. Amit Verma", "Neurology", "H002", "Government Multi-Specialty Hospital", true));

        if (DBConnection.isConnected()) {
            for (Doctor d : doctors) {
                doctorDAO.save(d);
            }
        }
    }

    private void seedInitialPatients() {
        Patient p1 = createSeededPatient("PAT-101", "Kavitha Ranganathan", 58, "Female", "IPD", 1, "ICU", "ADMITTED - ICU BED", "ICU-101");
        Bed b1 = getBedById("ICU-101");
        if (b1 != null) b1.admitPatient("PAT-101");

        Patient p2 = createSeededPatient("PAT-102", "Murugan Velu", 45, "Male", "IPD", 3, "General Medicine", "ADMITTED - GENERAL BED", "GEN-201");
        Bed b2 = getBedById("GEN-201");
        if (b2 != null) b2.admitPatient("PAT-102");

        Bed bClean = getBedById("SUR-301");
        if (bClean != null) bClean.setCleaning(true);

        Patient p3 = createSeededPatient("PAT-103", "Abdul Hameed", 34, "Male", "EMERGENCY", 1, "Trauma", "WAITING IN QUEUE", null);
        priorityQueue.add(p3);

        Patient p4 = createSeededPatient("PAT-104", "Saraswathi Ammal", 72, "Female", "OPD", 2, "Cardiology", "WAITING IN QUEUE", null);
        priorityQueue.add(p4);

        Patient p5 = createSeededPatient("PAT-105", "Dinesh Karthik", 26, "Male", "OPD", 3, "General Medicine", "WAITING IN QUEUE", null);
        priorityQueue.add(p5);

        Patient p6 = createSeededPatient("PAT-106", "Deepa Meenakshi", 39, "Female", "OPD", 4, "Pediatrics", "WAITING IN QUEUE", null);
        priorityQueue.add(p6);

        if (DBConnection.isConnected()) {
            for (Patient p : patients) {
                patientDAO.save(p);
            }
            if (b1 != null) bedDAO.updateBedStatus("ICU-101", true, false, "PAT-101");
            if (b2 != null) bedDAO.updateBedStatus("GEN-201", true, false, "PAT-102");
            if (bClean != null) bedDAO.updateBedStatus("SUR-301", false, true, null);
        }
    }

    private void seedInitialDemoData() {
        seedInitialHospitals();
        seedInitialBeds();
        seedInitialDoctors();
        seedInitialPatients();

        activityHistory.add(new HistoryRecord("HIST-001", "PAT-101", "REGISTRATION", "Registered as IPD [Severity 1 - Critical]", "Dr. Ananya Iyer"));
        activityHistory.add(new HistoryRecord("HIST-002", "PAT-101", "ADMISSION", "Admitted to Bed ICU-101 in Government General Hospital", "Admin System"));
        activityHistory.add(new HistoryRecord("HIST-003", "PAT-102", "REGISTRATION", "Registered as IPD [Severity 3 - Medium]", "Staff Officer"));
        activityHistory.add(new HistoryRecord("HIST-004", "PAT-102", "ADMISSION", "Admitted to Bed GEN-201 in Government General Hospital", "Admin System"));
        activityHistory.add(new HistoryRecord("HIST-005", "PAT-103", "REGISTRATION", "Emergency arrival triaged as Priority 1 (Critical)", "Trauma Nurse"));
        activityHistory.add(new HistoryRecord("HIST-006", "PAT-104", "REGISTRATION", "OPD visit registered under Cardiology (Severity 2)", "Staff Officer"));

        totalDischarges = 3;
    }

    private Patient createSeededPatient(String id, String name, int age, String gender, String visitType, 
                                        int severity, String dept, String status, String bedId) {
        sequenceCounter++;
        Patient p = new Patient(id, name, age, gender, visitType, severity, dept);
        p.setStatus(status);
        p.setAssignedBedId(bedId);
        p.setArrivalSequence(sequenceCounter);
        patients.add(p);
        patientMap.put(id, p);
        return p;
    }

    public synchronized void addDemoBed(Bed bed) {
        beds.add(bed);
        bedMap.put(bed.getBedId(), bed);
        Hospital h = hospitalMap.get(bed.getHospitalId());
        if (h != null) h.addBed(bed);
    }

    public synchronized void addDemoDoctor(Doctor doc) {
        doctors.add(doc);
        doctorMap.put(doc.getDoctorId(), doc);
        Hospital h = hospitalMap.get(doc.getHospitalId());
        if (h != null) h.addDoctor(doc);
    }

    // ==================== PATIENT REGISTRATION ====================
    public synchronized Patient registerPatient(String name, int age, String gender, 
                                                String visitType, int severity, String dept) {
        patientCounter++;
        sequenceCounter++;
        String pId = "PAT-" + patientCounter;
        Patient p = new Patient(pId, name, age, gender, visitType, severity, dept);
        p.setArrivalSequence(sequenceCounter);
        p.setStatus("WAITING IN QUEUE");

        patients.add(0, p); // Add newest first
        patientMap.put(pId, p);
        priorityQueue.add(p);

        // Permanent DB Persistence
        if (DBConnection.isConnected()) {
            patientDAO.save(p);
        }

        addHistoryRecord(pId, "REGISTRATION", 
            "Registered as " + visitType + " [Severity " + severity + "] in " + dept, "Staff User");

        notifyListeners();
        return p;
    }

    // ==================== PRIORITY QUEUE OPERATIONS ====================
    public synchronized Patient callNextPatient() {
        if (priorityQueue.isEmpty()) return null;
        Patient next = priorityQueue.poll();
        if (next != null) {
            next.setStatus("CALLED - CONSULTING");

            // Permanent DB Persistence
            if (DBConnection.isConnected()) {
                patientDAO.update(next);
            }

            addHistoryRecord(next.getPatientId(), "CONSULTATION", 
                "Called from Priority Queue for OPD/Emergency Encounter", "Staff User");
            notifyListeners();
        }
        return next;
    }

    public synchronized List<Patient> getSortedQueueList() {
        PriorityQueue<Patient> copy = new PriorityQueue<>(priorityQueue);
        List<Patient> list = new ArrayList<>();
        while (!copy.isEmpty()) {
            list.add(copy.poll());
        }
        return list;
    }

    // ==================== OPD CONSULTATION ====================
    public synchronized Encounter saveConsultation(String patientId, String doctorId, String doctorName,
                                                   String department, String hospitalId, String notes) {
        encounterCounter++;
        String encId = "ENC-" + encounterCounter;
        Encounter enc = new Encounter(encId, patientId, doctorId, doctorName, department, hospitalId, notes);
        encounters.add(0, enc);

        Patient p = patientMap.get(patientId);
        if (p != null) {
            p.setStatus("CONSULTATION COMPLETED");
            if (DBConnection.isConnected()) {
                patientDAO.update(p);
            }
        }

        // Permanent DB Persistence
        if (DBConnection.isConnected()) {
            encounterDAO.save(enc);
        }

        addHistoryRecord(patientId, "CONSULTATION", 
            "Encounter " + encId + " completed with " + doctorName + " (" + department + "): " + notes, "Medical Officer");

        notifyListeners();
        return enc;
    }

    // ==================== ADMISSION & BED MANAGEMENT ====================
    public synchronized AdmissionRequest createAdmissionRequest(String patientId, String bedRequirement, String hospitalId) {
        // Prevent duplicate pending requests
        for (AdmissionRequest req : admissionRequests) {
            if (req.getPatient() != null && req.getPatient().getPatientId().equalsIgnoreCase(patientId) 
                && "PENDING".equalsIgnoreCase(req.getStatus())) {
                return null;
            }
        }

        Patient p = patientMap.get(patientId);
        if (p == null) return null;

        admissionCounter++;
        String reqId = "REQ-" + admissionCounter;
        sequenceCounter++;
        AdmissionRequest req = new AdmissionRequest(reqId, p, sequenceCounter, bedRequirement, hospitalId);
        admissionRequests.add(0, req);

        // Permanent DB Persistence
        if (DBConnection.isConnected()) {
            admissionRequestDAO.save(req);
        }

        addHistoryRecord(patientId, "ADMISSION REQUEST", 
            "Admission Request " + reqId + " created for " + bedRequirement + " at Hospital " + hospitalId, "Admissions Desk");

        notifyListeners();
        return req;
    }

    public synchronized boolean allocateBed(String patientId, String bedId) {
        Patient p = patientMap.get(patientId);
        Bed b = bedMap.get(bedId);
        if (p == null || b == null) return false;
        if (b.isOccupied() || b.isCleaning()) return false;

        b.admitPatient(patientId);
        p.setAssignedBedId(bedId);
        p.setAssignedHospitalId(b.getHospitalId());
        p.setStatus("ADMITTED - " + b.getBedType());

        // Remove from priority queue if present
        priorityQueue.remove(p);

        // Update corresponding admission request if pending
        for (AdmissionRequest req : admissionRequests) {
            if (req.getPatient() != null && req.getPatient().getPatientId().equalsIgnoreCase(patientId) 
                && "PENDING".equalsIgnoreCase(req.getStatus())) {
                req.setStatus("ALLOCATED");
                if (DBConnection.isConnected()) {
                    admissionRequestDAO.updateStatus(req.getRequestId(), "ALLOCATED");
                }
                break;
            }
        }

        // Permanent DB Persistence
        if (DBConnection.isConnected()) {
            bedDAO.updateBedStatus(bedId, true, false, patientId);
            patientDAO.update(p);
            admissionDAO.recordAdmission(patientId, bedId, b.getHospitalId());
        }

        addHistoryRecord(patientId, "ADMISSION", 
            "Allocated Bed " + bedId + " (" + b.getBedType() + ") at Hospital " + b.getHospitalId(), "Bed Manager");

        notifyListeners();
        return true;
    }

    public synchronized boolean dischargePatient(String patientId, String notes) {
        Patient p = patientMap.get(patientId);
        if (p == null) return false;

        // Find active bed
        Bed assignedBed = null;
        if (p.getAssignedBedId() != null) {
            assignedBed = bedMap.get(p.getAssignedBedId());
        }
        if (assignedBed == null) {
            for (Bed b : beds) {
                if (patientId.equalsIgnoreCase(b.getPatientId())) {
                    assignedBed = b;
                    break;
                }
            }
        }

        if (assignedBed == null && !p.getStatus().contains("ADMITTED")) {
            return false; // Patient not admitted
        }

        if (assignedBed != null) {
            assignedBed.dischargePatient(); // Leaves bed in CLEANING state
            if (DBConnection.isConnected()) {
                bedDAO.updateBedStatus(assignedBed.getBedId(), false, true, null);
            }
        }

        p.setStatus("DISCHARGED");
        p.setAssignedBedId(null);
        totalDischarges++;

        // Permanent DB Persistence
        if (DBConnection.isConnected()) {
            patientDAO.update(p);
            admissionDAO.recordDischarge(patientId, notes);
        }

        addHistoryRecord(patientId, "DISCHARGE", 
            "Patient discharged. Bed " + (assignedBed != null ? assignedBed.getBedId() : "N/A") + 
            " marked for SANITIZATION / CLEANING. " + (notes != null ? notes : ""), "Discharge Officer");

        notifyListeners();
        return true;
    }

    public synchronized boolean finishBedCleaning(String bedId) {
        Bed b = bedMap.get(bedId);
        if (b != null && b.isCleaning()) {
            b.finishCleaning(); // Transitions to AVAILABLE

            // Permanent DB Persistence
            if (DBConnection.isConnected()) {
                bedDAO.updateBedStatus(bedId, false, false, null);
            }

            addHistoryRecord("FACILITY", "BED CLEANING", 
                "Bed " + bedId + " deep sanitized and restored to AVAILABLE status.", "Housekeeping Staff");
            notifyListeners();
            return true;
        }
        return false;
    }

    // ==================== REFERRALS ====================
    public synchronized ReferralRecord createReferral(String patientId, String sourceHosp, String destHosp, 
                                                      String bedType, int travelMinutes) {
        Patient p = patientMap.get(patientId);
        referralCounter++;
        String refId = "REF-" + referralCounter;
        String pName = p != null ? p.getName() : "Unknown Patient";

        ReferralRecord ref = new ReferralRecord(refId, patientId, pName, sourceHosp, destHosp, bedType, travelMinutes);
        referrals.add(0, ref);

        if (p != null) {
            p.setStatus("REFERRED TO " + destHosp);
            if (DBConnection.isConnected()) {
                patientDAO.update(p);
            }
        }

        // Permanent DB Persistence
        if (DBConnection.isConnected()) {
            referralDAO.save(ref);
        }

        addHistoryRecord(patientId, "REFERRAL", 
            "Inter-hospital referral " + refId + " to " + destHosp + " (" + travelMinutes + " mins route travel)", "Transfer Coordinator");

        notifyListeners();
        return ref;
    }

    // ==================== DOCTOR DUTY MANAGEMENT ====================
    public synchronized void updateDoctorDuty(String doctorId, boolean onDuty) {
        Doctor d = doctorMap.get(doctorId);
        if (d != null) {
            d.setDutyStatus(onDuty);
            if (DBConnection.isConnected()) {
                doctorDAO.updateDutyStatus(doctorId, onDuty);
            }
            addHistoryRecord(doctorId, "DUTY UPDATE", 
                "Physician " + d.getName() + " duty status changed to " + (onDuty ? "ON DUTY" : "OFF DUTY"), "Medical Superintendent");
            notifyListeners();
        }
    }

    // ==================== ACTIVITY HISTORY ====================
    public synchronized void addHistoryRecord(String patientId, String activityType, String description, String staffUser) {
        historyCounter++;
        HistoryRecord hr = new HistoryRecord("HIST-" + historyCounter, patientId, activityType, description, staffUser);
        activityHistory.add(0, hr);

        // Permanent DB Persistence
        if (DBConnection.isConnected()) {
            historyDAO.save(hr);
        }
    }

    // ==================== GETTERS ====================
    public synchronized List<Patient> getPatients() { return new ArrayList<>(patients); }
    public synchronized Patient getPatientById(String id) { return patientMap.get(id); }
    public synchronized List<Hospital> getHospitals() { return new ArrayList<>(hospitals); }
    public synchronized Hospital getHospitalById(String id) { return hospitalMap.get(id); }
    public synchronized List<Doctor> getDoctors() { return new ArrayList<>(doctors); }
    public synchronized Doctor getDoctorById(String id) { return doctorMap.get(id); }
    public synchronized List<Bed> getBeds() { return new ArrayList<>(beds); }
    public synchronized Bed getBedById(String id) { return bedMap.get(id); }
    public synchronized List<Encounter> getEncounters() { return new ArrayList<>(encounters); }
    public synchronized List<AdmissionRequest> getAdmissionRequests() { return new ArrayList<>(admissionRequests); }
    public synchronized List<ReferralRecord> getReferrals() { return new ArrayList<>(referrals); }
    public synchronized List<HistoryRecord> getActivityHistory() { return new ArrayList<>(activityHistory); }
    public synchronized PriorityQueue<Patient> getPriorityQueue() { return priorityQueue; }
    public synchronized RoutePlanner getRoutePlanner() { return routePlanner; }
    public synchronized ReferralService getReferralService() { return referralService; }

    // Statistics
    public synchronized int getTotalPatientsCount() { return patients.size(); }
    public synchronized int getWaitingPatientsCount() { return priorityQueue.size(); }
    public synchronized int getAdmittedPatientsCount() {
        int count = 0;
        for (Bed b : beds) {
            if (b.isOccupied()) count++;
        }
        return count;
    }
    public synchronized int getAvailableGeneralBedsCount() {
        int count = 0;
        for (Bed b : beds) {
            if (b instanceof GeneralBed && !b.isOccupied() && !b.isCleaning()) count++;
        }
        return count;
    }
    public synchronized int getAvailableIcuBedsCount() {
        int count = 0;
        for (Bed b : beds) {
            if (b instanceof ICUBed && !b.isOccupied() && !b.isCleaning()) count++;
        }
        return count;
    }
    public synchronized int getTotalDischargesCount() { return totalDischarges; }
}
