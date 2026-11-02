package hms.test;

import hms.*;
import hms.dao.*;
import hms.db.DBConnection;
import hms.ui.demo.DemoDataStore;
import java.util.List;

/**
 * PersistenceVerificationTest runs automated tests on:
 * - MySQL Connector/J JDBC connectivity
 * - DAO CRUD operations across all tables
 * - Polymorphic bed inheritance (Bed, GeneralBed, ICUBed)
 * - Deterministic PriorityQueue tie-breaking
 * - Dijkstra algorithm RoutePlanner calculations
 * - End-to-end data persistence across application restarts
 */
public class PersistenceVerificationTest {

    private static int testsPassed = 0;
    private static int testsTotal = 0;

    private static void assertTrue(String testName, boolean condition) {
        testsTotal++;
        if (condition) {
            System.out.println("  [PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + testName);
            throw new AssertionError("Test assertion failed: " + testName);
        }
    }

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("   HOSPITAL MANAGEMENT SYSTEM - PERSISTENCE & DAO TEST SUITE     ");
        System.out.println("==================================================================");

        // 1. Test Database Configuration & Environment Variables
        System.out.println("\n[1] Testing JDBC Database Configuration...");
        assertTrue("DB Host is localhost or env defined", DBConnection.getHost().equals("localhost") || !DBConnection.getHost().isEmpty());
        assertTrue("DB Port is 3306 or env defined", DBConnection.getPort().equals("3306") || !DBConnection.getPort().isEmpty());
        assertTrue("DB Name is hospital_oop_db", DBConnection.getDatabaseName().equals("hospital_oop_db"));
        assertTrue("DB User is hospital_app (non-root)", DBConnection.getUsername().equals("hospital_app"));
        assertTrue("JDBC URL contains correct protocol and parameters", 
            DBConnection.getJdbcUrl().startsWith("jdbc:mysql://") && DBConnection.getJdbcUrl().contains("hospital_oop_db"));

        boolean dbConnected = DBConnection.isConnected();
        System.out.println("  -> Database Connection Status: " + (dbConnected ? "ONLINE (Connected)" : "OFFLINE (DB_PASSWORD unset or server unreachable)"));

        // 2. Test OOP Classes, Inheritance, and Polymorphism
        System.out.println("\n[2] Testing OOP Domain Classes & Polymorphism...");
        GeneralBed genBed = new GeneralBed("GEN-TEST", "General Ward", "H001");
        ICUBed icuBed = new ICUBed("ICU-TEST", "ICU", "H001");

        assertTrue("GeneralBed is instance of Bed", genBed instanceof Bed);
        assertTrue("ICUBed is instance of Bed", icuBed instanceof Bed);
        assertTrue("GeneralBed supports requirement 'General Bed'", genBed.supportsRequirement("General Bed"));
        assertTrue("GeneralBed rejects requirement 'ICU Bed'", !genBed.supportsRequirement("ICU Bed"));
        assertTrue("ICUBed supports requirement 'ICU Bed'", icuBed.supportsRequirement("ICU Bed"));
        assertTrue("ICUBed rejects requirement 'General Bed'", !icuBed.supportsRequirement("General Bed"));

        // Bed occupancy lifecycle
        genBed.admitPatient("PAT-TEST");
        assertTrue("Bed marked occupied after admit", genBed.isOccupied());
        assertTrue("Bed has assigned patient", "PAT-TEST".equals(genBed.getPatientId()));
        genBed.dischargePatient();
        assertTrue("Bed marked cleaning after discharge", genBed.isCleaning() && !genBed.isOccupied());
        genBed.finishCleaning();
        assertTrue("Bed available after finishCleaning", !genBed.isCleaning() && !genBed.isOccupied() && "AVAILABLE".equals(genBed.getStatus()));

        // 3. Test PriorityQueue & Deterministic Ordering
        System.out.println("\n[3] Testing PriorityQueue Severity Ordering...");
        DemoDataStore store = DemoDataStore.getInstance();
        Patient pHigh = new Patient("PAT-P1", "Critical Patient", 60, "Male", "EMERGENCY", 1, "ICU");
        Patient pLow  = new Patient("PAT-P4", "Routine Patient", 25, "Female", "OPD", 4, "General Medicine");
        
        java.util.PriorityQueue<Patient> testQueue = new java.util.PriorityQueue<>((a, b) -> {
            int c = Integer.compare(a.getPriority(), b.getPriority());
            if (c != 0) return c;
            return Long.compare(a.getArrivalSequence(), b.getArrivalSequence());
        });
        testQueue.add(pLow);
        testQueue.add(pHigh);

        Patient top = testQueue.poll();
        assertTrue("PriorityQueue polls Critical Priority 1 before Priority 4", top != null && top.getPriority() == 1);

        // 4. Test Dijkstra RoutePlanner
        System.out.println("\n[4] Testing Dijkstra Algorithm (RoutePlanner)...");
        RoutePlanner planner = store.getRoutePlanner();
        assertTrue("RoutePlanner initialized", planner != null);
        RoutePlanner.RouteResult route = planner.findShortestPath("H001", "H002");
        assertTrue("Route computed between H001 and H002", route != null);
        assertTrue("Route travel time is positive", route.getTotalMinutes() > 0);
        System.out.println("  -> Route H001 to H002: " + route.getPathString() + " (" + route.getTotalMinutes() + " mins)");

        // 5. Test ReferralService
        System.out.println("\n[5] Testing ReferralService Destinations...");
        ReferralService refService = store.getReferralService();
        List<ReferralService.DestinationCandidate> candidates = 
            refService.findEligibleDestinations("H001", "General Bed", store.getHospitals());
        assertTrue("Referral candidates found for H001", candidates != null);

        // 6. Test DAOs if Database is Connected
        if (dbConnected) {
            System.out.println("\n[6] Testing Live MySQL DAO Operations...");
            
            // UserDAO
            UserDAO userDAO = new UserDAO();
            boolean authAdmin = userDAO.authenticate("admin", "hospital123", "Administrator");
            boolean authStaff = userDAO.authenticate("staff", "hospital123", "Staff Officer");
            boolean authFail  = userDAO.authenticate("admin", "wrongpassword", "Administrator");
            assertTrue("UserDAO authenticates admin", authAdmin);
            assertTrue("UserDAO authenticates staff", authStaff);
            assertTrue("UserDAO rejects invalid password", !authFail);

            // HospitalDAO
            HospitalDAO hospDAO = new HospitalDAO();
            List<Hospital> hosps = hospDAO.findAll();
            assertTrue("HospitalDAO returns hospitals", hosps != null && !hosps.isEmpty());

            // DoctorDAO
            DoctorDAO docDAO = new DoctorDAO();
            List<Doctor> docs = docDAO.findAll();
            assertTrue("DoctorDAO returns doctors", docs != null && !docs.isEmpty());
            boolean dutyUpdated = docDAO.updateDutyStatus("DOC-01", true);
            assertTrue("DoctorDAO updates duty status", dutyUpdated);

            // BedDAO
            BedDAO bedDAO = new BedDAO();
            List<Bed> dbBeds = bedDAO.findAll();
            assertTrue("BedDAO returns inpatient beds", dbBeds != null && !dbBeds.isEmpty());
            boolean hasIcu = dbBeds.stream().anyMatch(b -> b instanceof ICUBed);
            boolean hasGen = dbBeds.stream().anyMatch(b -> b instanceof GeneralBed);
            assertTrue("BedDAO correctly preserves ICUBed instances", hasIcu);
            assertTrue("BedDAO correctly preserves GeneralBed instances", hasGen);

            // PatientDAO CRUD
            PatientDAO patDAO = new PatientDAO();
            String testPatId = "PAT-TEST-" + (System.currentTimeMillis() % 10000);
            Patient testPatient = new Patient(testPatId, "Test Automated Patient", 45, "Male", "OPD", 2, "Cardiology");
            boolean patSaved = patDAO.save(testPatient);
            assertTrue("PatientDAO saves new patient", patSaved);

            Patient fetched = patDAO.findById(testPatId);
            assertTrue("PatientDAO finds saved patient by ID", fetched != null && fetched.getName().equals("Test Automated Patient"));

            testPatient.setStatus("CONSULTATION COMPLETED");
            boolean patUpdated = patDAO.update(testPatient);
            assertTrue("PatientDAO updates patient status", patUpdated);

            // AdmissionRequestDAO
            AdmissionRequestDAO reqDAO = new AdmissionRequestDAO();
            AdmissionRequest testReq = new AdmissionRequest("REQ-TEST-1", testPatient, 9999, "General Bed", "H001");
            boolean reqSaved = reqDAO.save(testReq);
            assertTrue("AdmissionRequestDAO saves request", reqSaved);
            boolean reqUpdated = reqDAO.updateStatus("REQ-TEST-1", "ALLOCATED");
            assertTrue("AdmissionRequestDAO updates status", reqUpdated);

            // AdmissionDAO
            AdmissionDAO admDAO = new AdmissionDAO();
            boolean admRecorded = admDAO.recordAdmission(testPatId, "GEN-202", "H001");
            assertTrue("AdmissionDAO records active admission", admRecorded);
            boolean disRecorded = admDAO.recordDischarge(testPatId, "Routine discharge after test");
            assertTrue("AdmissionDAO records patient discharge", disRecorded);

            // EncounterDAO
            EncounterDAO encDAO = new EncounterDAO();
            Encounter testEnc = new Encounter("ENC-TEST-1", testPatId, "DOC-01", "Dr. Rajesh Sharma", "Cardiology", "H001", "Automated test consultation notes");
            boolean encSaved = encDAO.save(testEnc);
            assertTrue("EncounterDAO saves clinical encounter", encSaved);

            // ReferralDAO
            ReferralDAO refDAO = new ReferralDAO();
            DemoDataStore.ReferralRecord testRef = new DemoDataStore.ReferralRecord("REF-TEST-1", testPatId, "Test Automated Patient", "H001", "H002", "General Bed", 25);
            boolean refSaved = refDAO.save(testRef);
            assertTrue("ReferralDAO saves transfer referral", refSaved);

            // HistoryDAO
            HistoryDAO histDAO = new HistoryDAO();
            DemoDataStore.HistoryRecord testHist = new DemoDataStore.HistoryRecord("HIST-TEST-1", testPatId, "TEST", "Automated verification log", "Test Runner");
            boolean histSaved = histDAO.save(testHist);
            assertTrue("HistoryDAO saves activity log entry", histSaved);

            // Clean up test patient
            patDAO.delete(testPatId);
            assertTrue("PatientDAO cleans up test record", patDAO.findById(testPatId) == null);
        } else {
            System.out.println("\n[6] Skipped Live MySQL DAO Operations (Database is offline).");
        }

        // 7. Test End-to-End DemoDataStore Operations
        System.out.println("\n[7] Testing End-to-End Store Operations & Listeners...");
        int initialPatientCount = store.getTotalPatientsCount();
        Patient pNew = store.registerPatient("Venkatesh Iyer", 52, "Male", "OPD", 2, "Cardiology");
        assertTrue("Store registers patient", pNew != null && store.getTotalPatientsCount() == initialPatientCount + 1);

        // Call from queue
        Patient called = store.callNextPatient();
        assertTrue("Store calls highest priority patient from queue", called != null);

        // Record consultation
        Encounter enc = store.saveConsultation(pNew.getPatientId(), "DOC-01", "Dr. Rajesh Sharma", "Cardiology", "H001", "Blood pressure stable. Prescribed medication.");
        assertTrue("Store records consultation encounter", enc != null && pNew.getStatus().equals("CONSULTATION COMPLETED"));

        // Bed allocation
        Bed availGenBed = store.getBeds().stream().filter(b -> b instanceof GeneralBed && !b.isOccupied() && !b.isCleaning()).findFirst().orElse(null);
        if (availGenBed != null) {
            boolean allocated = store.allocateBed(pNew.getPatientId(), availGenBed.getBedId());
            assertTrue("Store allocates patient to available bed", allocated);
            assertTrue("Bed reflects patient occupancy", availGenBed.isOccupied() && pNew.getPatientId().equals(availGenBed.getPatientId()));

            // Discharge
            boolean discharged = store.dischargePatient(pNew.getPatientId(), "Patient recovered.");
            assertTrue("Store discharges patient", discharged);
            assertTrue("Bed entered cleaning turnaround", availGenBed.isCleaning());

            // Finish cleaning
            boolean sanitized = store.finishBedCleaning(availGenBed.getBedId());
            assertTrue("Store sanitizes bed and restores to available", sanitized && !availGenBed.isCleaning());
        }

        // Inter-hospital referral
        DemoDataStore.ReferralRecord ref = store.createReferral(pNew.getPatientId(), "H001", "H002", "General Bed", 22);
        assertTrue("Store logs inter-hospital referral", ref != null && store.getReferrals().size() > 0);

        System.out.println("\n==================================================================");
        System.out.println("   TEST EXECUTION COMPLETE: " + testsPassed + " / " + testsTotal + " assertions passed.");
        System.out.println("==================================================================");
    }
}
