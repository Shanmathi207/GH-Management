# SYSTEM ARCHITECTURE — GOVERNMENT HOSPITAL MANAGEMENT SYSTEM (HospitalOOP70)

## 1. Architectural Overview
The Government Hospital Management System (**HospitalOOP70**) is engineered using a robust, decoupled **MVC (Model-View-Controller) + Service + DAO (Data Access Object)** layered architectural pattern in Core Java.

The architecture cleanly isolates presentation logic (Java Swing), clinical domain business rules (Service layer), and physical data storage (MySQL Server 8.0 via JDBC). When offline or when the `DB_PASSWORD` environment variable is not defined, the system automatically falls back to an in-memory concurrent operational cache (`DemoDataStore`) with zero runtime degradation.

```
+-----------------------------------------------------------------------------------+
|                           PRESENTATION LAYER (Java Swing)                         |
|  - LoginFrame (RBAC Authentication)        - ModernTablePanel (JTable)            |
|  - MainFrame (CardLayout Master Container) - SummaryCard (Metric Cards)           |
|  - 16 Operations Panels (Dashboard, OPD, ICU, Beds, Referrals, Triage, etc.)      |
+-----------------------------------------------------------------------------------+
                                      |
                                      v
+-----------------------------------------------------------------------------------+
|                             CONTROLLER / SESSION LAYER                            |
|  - UserSession (Role-Based Access Control: Admin, Doctor, Receptionist, Staff)    |
|  - PasswordUtils (Cryptographic SHA-256 Authentication Hash Engine)               |
+-----------------------------------------------------------------------------------+
                                      |
                                      v
+-----------------------------------------------------------------------------------+
|                        SERVICE & BUSINESS LOGIC LAYER                             |
|  - PriorityManager (Java PriorityQueue with 3-Level Deterministic Tie-Breaking)   |
|  - ReferralService & RoutePlanner (Dijkstra's Algorithm on Weighted Graph)        |
|  - DefaultAdmissionPolicy (Polymorphic Bed Allocation Engine)                     |
|  - DemoDataStore (Thread-Safe Event-Driven Mediator & State Machine)              |
+-----------------------------------------------------------------------------------+
                                      |
                                      v
+-----------------------------------------------------------------------------------+
|                            DOMAIN / OOP ENTITY LAYER                              |
|  - Patient (Demographics, Triage, Encounter Tracking)                             |
|  - Bed (Abstract) -> GeneralBed, ICUBed (5 Operational States)                    |
|  - Doctor, Hospital, Encounter, AdmissionRequest, ReferralRecord                  |
+-----------------------------------------------------------------------------------+
                                      |
                                      v
+-----------------------------------------------------------------------------------+
|                        DATA ACCESS OBJECT (DAO) LAYER                             |
|  - PatientDAO         - DoctorDAO          - HospitalDAO       - BedDAO           |
|  - AdmissionDAO       - AdmissionRequestDAO - EncounterDAO      - ReferralDAO      |
|  - RouteDAO           - HistoryDAO         - UserDAO                              |
+-----------------------------------------------------------------------------------+
                                      |
                                      v
+-----------------------------------------------------------------------------------+
|                         DATABASE & PERSISTENCE LAYER                              |
|  - DBConnection (Connection pooling, PreparedStatement, auto-reconnect)           |
|  - MySQL Server 8.0 (Database: hospital_oop_db, Non-root User: hospital_app)      |
+-----------------------------------------------------------------------------------+
```

---

## 2. Layer Descriptions

### 2.1 Presentation Layer (Swing GUI)
- **Framework:** Pure Java Swing (`javax.swing`, `java.awt`).
- **Color Palette & Design Language:** High-contrast clinical theme with dark navy headers (`#142B4A`), vibrant primary blue accents (`#2563EB`), crisp white rounded cards (`#FFFFFF`), neutral canvas (`#F4F7FB`), and WCAG-compliant status badges.
- **CardLayout Navigation:** `MainFrame` acts as the single-window viewport hosting a left-hand navigation `SidebarPanel` and a central `CardLayout` deck hosting 16 distinct clinical subpanels.

### 2.2 Controller & Security Layer
- **UserSession:** Singleton pattern managing current authenticated credentials and role permissions.
- **RBAC Matrix:**
  - `Administrator`: Complete system authority across all 17 navigation views.
  - `Doctor`: Outpatient consultations, emergency triage, clinical encounter dossiers.
  - `Receptionist`: Patient demographics intake, priority queue scheduling, patient records directory.
  - `Hospital Staff`: Inpatient ward bed management, bed sanitization turnaround, patient discharge processing.
- **Cryptographic Security:** Passwords hashed with SHA-256. Zero hardcoded passwords; database credentials read securely from `DB_PASSWORD`.

### 2.3 Service Layer & Algorithms
- **Triage Scheduling:** `java.util.PriorityQueue` using a 3-tier deterministic comparator:
  1. Primary: Triage Severity Level 1 to 5 (ascending; 1 is immediate resuscitation).
  2. Secondary: Arrival Timestamp (ascending).
  3. Tertiary: Unique monotonic arrival sequence number (deterministic tie-breaking).
- **Inter-Hospital Referrals:** Weighted graph representation of regional government hospitals. `RoutePlanner` implements **Dijkstra's Shortest Path Algorithm** with a min-heap priority queue to determine the optimal transfer hospital based on real-time bed capacity.

### 2.4 Domain Object Model
All 14 core OOP domain classes are preserved:
- `Bed` (abstract superclass): Encapsulates bed identification, department, hospital, operational status, and patient occupancy.
- `GeneralBed` & `ICUBed` (subclasses): Exhibit polymorphism by specializing equipment requirements and clinical suitability.
- `BedState`: 5 operational states: `AVAILABLE`, `RESERVED`, `OCCUPIED`, `CLEANING`, `MAINTENANCE`. Discharged beds transition to `CLEANING` and cannot be allocated until sanitized.

### 2.5 DAO Persistence Layer
- Standard JDBC operations utilizing `PreparedStatement`, parameterized queries (SQL injection prevention), `ResultSet` mapping, and atomic transaction control (`setAutoCommit(false)`, `commit()`, `rollback()`).
- Atomic conditional updates in `BedDAO.allocateBedAtomic` ensure zero race conditions or double-booking.
