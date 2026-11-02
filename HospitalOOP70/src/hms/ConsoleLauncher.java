package hms;

import java.util.Scanner;

/**
 * Preserved command-line interface launcher for HospitalOOP70.
 */
public class ConsoleLauncher {

    public static void runConsole(HospitalManagementSystem hms) {
        if (hms == null) {
            hms = new HospitalManagementSystem();
        }

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("=================================================");
        System.out.println("   GOVERNMENT HOSPITAL MANAGEMENT SYSTEM (HMS)");
        System.out.println("             Command-Line Interface");
        System.out.println("=================================================");

        while (running) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1. Register Patient");
            System.out.println("2. View Priority Queue");
            System.out.println("3. Process Next Patient");
            System.out.println("4. View All Patients");
            System.out.println("5. View Bed Status");
            System.out.println("6. Release / Discharge Bed");
            System.out.println("7. View Allocation History");
            System.out.println("8. Exit Console");
            System.out.print("Select an option (1-8): ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1":
                    System.out.print("Patient Name: ");
                    String name = scanner.nextLine().trim();
                    System.out.print("Age: ");
                    int age = Integer.parseInt(scanner.nextLine().trim());
                    System.out.print("Gender: ");
                    String gender = scanner.nextLine().trim();
                    System.out.print("Visit Type (OPD/IPD/EMERGENCY): ");
                    String vt = scanner.nextLine().trim();
                    System.out.print("Severity (1-5): ");
                    int sev = Integer.parseInt(scanner.nextLine().trim());
                    System.out.print("Department: ");
                    String dept = scanner.nextLine().trim();
                    Patient p = hms.registerPatient(name, age, gender, vt, sev, dept);
                    if (p != null) {
                        System.out.println("Registered Successfully! ID: " + p.getPatientId());
                    } else {
                        System.out.println("Registration failed: Invalid details.");
                    }
                    break;
                case "2":
                    System.out.println("Current Priority Queue Size: " + hms.getPriorityQueue().size());
                    for (AdmissionRequest r : hms.getPriorityQueue()) {
                        r.displayRequest();
                    }
                    break;
                case "3":
                    AdmissionRequest nextReq = hms.getPriorityQueue().poll();
                    if (nextReq != null) {
                        System.out.println("Processed Patient: " + nextReq.getPatient().getName() + " (Priority: " + nextReq.getPatient().getPriority() + ")");
                    } else {
                        System.out.println("Queue is empty.");
                    }
                    break;
                case "4":
                    for (Patient pat : hms.getPatients().values()) {
                        pat.displayPatient();
                    }
                    break;
                case "5":
                    for (Bed b : hms.getBeds().values()) {
                        b.displayBed();
                    }
                    break;
                case "6":
                    System.out.print("Enter Patient ID to discharge: ");
                    String pId = scanner.nextLine().trim();
                    boolean discharged = hms.dischargePatient(pId);
                    System.out.println(discharged ? "Patient discharged. Bed set to CLEANING." : "Discharge failed.");
                    break;
                case "7":
                    for (String log : hms.getAllocationHistory()) {
                        System.out.println(log);
                    }
                    break;
                case "8":
                    System.out.println("Exiting Console Mode.");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1-8.");
            }
        }
    }
}
