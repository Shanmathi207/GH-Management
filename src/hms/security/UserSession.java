package hms.security;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * UserSession holds the authenticated user credentials and provides
 * Role-Based Access Control (RBAC) permission checking.
 * 
 * Supported Roles:
 * - Administrator: Full system access across all 17 screens
 * - Doctor: Medical consultations, emergency intake, clinical history, physician roster
 * - Receptionist: Demographics registration, priority queue triage, records lookup, transfers
 * - Hospital Staff: Inpatient ward bed grid, sanitization turnaround, discharge management
 */
public class UserSession {

    private static UserSession instance;

    private String username;
    private String role;
    private String fullName;
    private String loginTimestamp;

    private UserSession() {
        this.username = "admin";
        this.role = "Administrator";
        this.fullName = "Dr. System Administrator";
        this.loginTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public synchronized void login(String username, String role, String fullName) {
        this.username = username;
        this.role = role != null ? role : "Hospital Staff";
        this.fullName = fullName != null ? fullName : username;
        this.loginTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public synchronized void logout() {
        this.username = "guest";
        this.role = "Guest";
        this.fullName = "Unauthenticated";
        this.loginTimestamp = "";
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getFullName() {
        return fullName;
    }

    public String getLoginTimestamp() {
        return loginTimestamp;
    }

    public boolean isAdmin() {
        return "Administrator".equalsIgnoreCase(role);
    }

    public boolean isDoctor() {
        return "Doctor".equalsIgnoreCase(role) || isAdmin();
    }

    public boolean isReceptionist() {
        return "Receptionist".equalsIgnoreCase(role) || isAdmin();
    }

    public boolean isStaff() {
        return "Hospital Staff".equalsIgnoreCase(role) || "Staff Officer".equalsIgnoreCase(role) || isAdmin();
    }

    /**
     * Checks if current user has permission to access a specific navigation screen or operation.
     */
    public boolean canAccessScreen(String screenKey) {
        if (isAdmin()) return true;

        switch (screenKey) {
            case "DASHBOARD":
            case "OPD_IPD_WORKFLOW":
            case "REPORTS":
            case "HISTORY":
            case "SETTINGS":
                return true;

            case "REGISTRATION":
            case "EMERGENCY":
            case "RECORDS":
            case "PRIORITY_QUEUE":
            case "REFERRALS":
                return isReceptionist();

            case "OPD_CONSULTATION":
            case "DOCTORS":
                return isDoctor();

            case "ADMISSIONS":
            case "BED_MANAGEMENT":
            case "DISCHARGE":
            case "HOSPITALS":
                return isStaff() || isDoctor();

            default:
                return true;
        }
    }
}
