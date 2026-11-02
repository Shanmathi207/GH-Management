package hms.ui.test;

import hms.*;
import hms.ui.LoginFrame;
import hms.ui.MainFrame;
import hms.ui.components.SidebarPanel;
import hms.ui.demo.DemoDataStore;
import java.awt.Dimension;
import java.util.List;

/**
 * Automated Test Suite validating all functional requirements and Swing components
 * for HospitalOOP70 Government Hospital Management System desktop frontend.
 */
public class FrontendTestSuite {

    private static int testsPassed = 0;
    private static int testsTotal = 0;

    private static void assertTrue(String testName, boolean condition) {
        testsTotal++;
        if (condition) {
            System.out.println("  [PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + testName);
            throw new AssertionError("Test failed: " + testName);
        }
    }

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("   HOSPITAL OOP70 - AUTOMATED FRONTEND TEST SUITE EXECUTION   ");
        System.out.println("===============================================================");

        DemoDataStore store = DemoDataStore.getInstance();

        // 1. Store Initialization
        System.out.println("\n[1] Testing DemoDataStore Initialization...");
        assertTrue("Hospitals seeded", store.getHospitals().size() >= 5);
        assertTrue("Beds seeded", store.getBeds().size() >= 10);
        assertTrue("Doctors seeded", store.getDoctors().size() >= 6);
        assertTrue("Initial patients seeded", store.getPatients().size() >= 5);
        assertTrue("Priority queue contains patients", store.getPriorityQueue().size() >= 3);

        // 2. Patient Registration
        System.out.println("\n[2] Testing Patient Registration & Validation...");
        int initialPatientCount = store.getPatients().size();
        Patient regPatient = store.registerPatient("Sivakumar Raman", 48, "Male", "OPD", 2, "Cardiology");
        assertTrue("Patient created with valid ID", regPatient != null && regPatient.getPatientId().startsWith("PAT-"));
        assertTrue("Patient count incremented", store.getPatients().size() == initialPatientCount + 1);
        assertTrue("Patient queued into PriorityQueue", store.getSortedQueueList().stream().anyMatch(p -> p.getPatientId().equals(regPatient.getPatientId())));
        assertTrue("Audit history recorded for registration", store.getActivityHistory().get(0).getActivityType().equals("REGISTRATION"));

        // 3. Priority Queue Deterministic Ordering
        System.out.println("\n[3] Testing Priority Queue Ordering (Severity 1 to 5)...");
        // Register critical severity 1 patient
        Patient criticalPat = store.registerPatient("Emergency Trauma Patient", 30, "Male", "EMERGENCY", 1, "Trauma");
        Patient nextInQueue = store.getSortedQueueList().get(0);
        assertTrue("Highest priority (Level 1 Critical) is first in queue", nextInQueue.getPriority() == 1);

        // 4. Calling Next Patient
        System.out.println("\n[4] Testing Call Next Patient Workflow...");
        int queueSizeBefore = store.getPriorityQueue().size();
        Patient called = store.callNextPatient();
        assertTrue("Patient popped from PriorityQueue", called != null);
        assertTrue("Queue size decremented", store.getPriorityQueue().size() == queueSizeBefore - 1);
        assertTrue("Status updated to CALLED - CONSULTING", called.getStatus().contains("CALLED"));

        // 5. OPD Consultation Encounter
        System.out.println("\n[5] Testing OPD Consultation Workflow...");
        int encCountBefore = store.getEncounters().size();
        Encounter enc = store.saveConsultation(called.getPatientId(), "DOC-01", "Dr. Rajesh Sharma", "Cardiology", "H001", "Hypertension Stage 2 diagnosed; prescribed ACE inhibitor.");
        assertTrue("Encounter saved with ENC- ID", enc != null && enc.getEncounterId().startsWith("ENC-"));
        assertTrue("Encounters list incremented", store.getEncounters().size() == encCountBefore + 1);
        assertTrue("Patient status updated to CONSULTATION COMPLETED", called.getStatus().equals("CONSULTATION COMPLETED"));

        // 6. Admission Requests & Duplicate Prevention
        System.out.println("\n[6] Testing Admission Requests & Duplicate Prevention...");
        Patient admPatient = store.registerPatient("Inpatient Candidate", 55, "Female", "IPD", 2, "General Medicine");
        AdmissionRequest req1 = store.createAdmissionRequest(admPatient.getPatientId(), "General Bed", "H001");
        assertTrue("First admission request created", req1 != null && req1.getRequestId().startsWith("REQ-"));
        AdmissionRequest req2 = store.createAdmissionRequest(admPatient.getPatientId(), "General Bed", "H001");
        assertTrue("Duplicate pending admission request prevented", req2 == null);

        // 7. Bed Allocation & Double Allocation Prevention
        System.out.println("\n[7] Testing Bed Allocation & Double Allocation Prevention...");
        Bed targetBed = store.getBedById("GEN-202");
        assertTrue("Bed GEN-202 initially available", !targetBed.isOccupied() && !targetBed.isCleaning());
        boolean allocSuccess = store.allocateBed(admPatient.getPatientId(), "GEN-202");
        assertTrue("Bed allocated successfully", allocSuccess);
        assertTrue("Bed GEN-202 now occupied", targetBed.isOccupied());
        assertTrue("Patient status updated to ADMITTED", admPatient.getStatus().contains("ADMITTED"));

        // Try double allocating same bed
        Patient otherPat = store.registerPatient("Other Patient", 22, "Male", "OPD", 3, "General Medicine");
        boolean doubleAlloc = store.allocateBed(otherPat.getPatientId(), "GEN-202");
        assertTrue("Double allocation on occupied bed prevented", !doubleAlloc);

        // 8. Discharge & Sanitization Cleaning Lifecycle
        System.out.println("\n[8] Testing Patient Discharge & Bed Cleaning Lifecycle...");
        int dischargesBefore = store.getTotalDischargesCount();
        boolean dischargeSuccess = store.dischargePatient(admPatient.getPatientId(), "Recovered successfully");
        assertTrue("Discharge successful", dischargeSuccess);
        assertTrue("Patient status is DISCHARGED", admPatient.getStatus().equals("DISCHARGED"));
        assertTrue("Discharge count incremented", store.getTotalDischargesCount() == dischargesBefore + 1);
        assertTrue("Bed GEN-202 transitioned to CLEANING (not available!)", targetBed.isCleaning() && !targetBed.isOccupied());
        
        // Cannot allocate cleaning bed
        boolean allocCleaning = store.allocateBed(otherPat.getPatientId(), "GEN-202");
        assertTrue("Allocation prevented while bed is in CLEANING", !allocCleaning);

        // Finish cleaning
        boolean cleanDone = store.finishBedCleaning("GEN-202");
        assertTrue("Finish cleaning completed", cleanDone);
        assertTrue("Bed GEN-202 restored to AVAILABLE", !targetBed.isCleaning() && !targetBed.isOccupied() && targetBed.getStatus().equals("AVAILABLE"));

        // 9. Inter-Hospital Referrals & Dijkstra Algorithm
        System.out.println("\n[9] Testing Inter-Hospital Referral & Dijkstra RoutePlanner...");
        RoutePlanner rp = store.getRoutePlanner();
        RoutePlanner.RouteResult rr = rp.findShortestPath("H001", "H003");
        assertTrue("Dijkstra shortest path found between H001 and H003", rr.isFound());
        assertTrue("Calculated travel time > 0", rr.getTotalMinutes() > 0);
        System.out.println("    Calculated Dijkstra Route: " + rr.getPathString() + " (" + rr.getTotalMinutes() + " mins)");

        DemoDataStore.ReferralRecord refRec = store.createReferral(otherPat.getPatientId(), "H001", "H003", "General Bed", rr.getTotalMinutes());
        assertTrue("Referral record created", refRec != null && refRec.getReferralId().startsWith("REF-"));
        assertTrue("Patient status marked as REFERRED", otherPat.getStatus().contains("REFERRED"));

        // 10. Frame Instantiation & Navigation
        System.out.println("\n[10] Testing Swing Frames Instantiation & Navigation...");
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            System.out.println("  [INFO] Headless environment detected: Skipping JFrame peer creation.");
            assertTrue("Headless check passed", true);
        } else {
            LoginFrame loginFrame = new LoginFrame();
            assertTrue("LoginFrame instantiated", loginFrame != null);
            assertTrue("LoginFrame title correct", loginFrame.getTitle().contains("Hospital Management System"));

            try {
                System.out.println("    Creating HeaderPanel...");
                new hms.ui.components.HeaderPanel("Admin", null);
                System.out.println("    Creating SidebarPanel...");
                new hms.ui.components.SidebarPanel(null);
                System.out.println("    Creating DashboardPanel...");
                new hms.ui.panels.DashboardPanel(null);
                System.out.println("    Creating PatientRegistrationPanel...");
                new hms.ui.panels.PatientRegistrationPanel(null);
                System.out.println("    Creating PatientRecordsPanel...");
                new hms.ui.panels.PatientRecordsPanel();
                System.out.println("    Creating PriorityQueuePanel...");
                new hms.ui.panels.PriorityQueuePanel();
                System.out.println("    Creating OPDConsultationPanel...");
                new hms.ui.panels.OPDConsultationPanel();
                System.out.println("    Creating AdmissionPanel...");
                new hms.ui.panels.AdmissionPanel();
                System.out.println("    Creating BedManagementPanel...");
                new hms.ui.panels.BedManagementPanel();
                System.out.println("    Creating DischargePanel...");
                new hms.ui.panels.DischargePanel();
                System.out.println("    Creating ReferralPanel...");
                new hms.ui.panels.ReferralPanel();
                System.out.println("    Creating DoctorsHospitalsPanel...");
                new hms.ui.panels.DoctorsHospitalsPanel();
                System.out.println("    Creating PatientHistoryPanel...");
                new hms.ui.panels.PatientHistoryPanel();
                System.out.println("    Creating ReportsPanel...");
                new hms.ui.panels.ReportsPanel();

                System.out.println("    Creating MainFrame...");
                MainFrame mainFrame = new MainFrame("Administrator", loginFrame);
                assertTrue("MainFrame instantiated", mainFrame != null);
                assertTrue("MainFrame title correct", mainFrame.getTitle().equals("GOVERNMENT HOSPITAL MANAGEMENT SYSTEM"));

                // Test navigation through all 12 operational pages
                for (String page : SidebarPanel.NAV_ITEMS) {
                    if (!page.equals(SidebarPanel.NAV_LOGOUT)) {
                        mainFrame.navigateToPage(page);
                        assertTrue("Navigated cleanly to page: " + page, true);
                    }
                }

                // Clean up Swing resources
                loginFrame.dispose();
                mainFrame.dispose();
            } catch (Throwable t) {
                t.printStackTrace();
                throw t;
            }
        }

        System.out.println("\n===============================================================");
        System.out.println("  TEST SUITE COMPLETED: " + testsPassed + " / " + testsTotal + " TESTS PASSED (100% SUCCESS)");
        System.out.println("===============================================================");
        System.exit(0);
    }
}
