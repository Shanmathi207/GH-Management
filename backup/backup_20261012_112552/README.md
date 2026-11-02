# Government Hospital Management System (HospitalOOP70)
## Professional Java Swing Desktop Frontend Prototype

A modern, desktop frontend application for a Government Hospital Management System built purely using **Java Swing** (Java 17/21 compatible).

---

## 🏛️ System Architecture

```
src/
├── hms/                                 # Preserved Core OOP Domain Model
│   ├── Bed.java                         # Abstract Bed (supports requirement, lifecycle)
│   ├── GeneralBed.java                  # Concrete General Ward Bed
│   ├── ICUBed.java                      # Concrete ICU Bed
│   ├── Doctor.java                      # Attending Physician entity & duty status
│   ├── Hospital.java                    # Medical facility node with bed/doctor lists
│   ├── Patient.java                     # Clinical patient entity with triage priority
│   ├── AdmissionPolicy.java             # Policy interface for admission & eligibility
│   ├── DefaultAdmissionPolicy.java      # Concrete standard admission policy
│   ├── AdmissionRequest.java            # Formal bed admission request
│   ├── PriorityManager.java             # PriorityQueue wrapper for acute triage
│   ├── Encounter.java                   # Outpatient / Emergency clinical encounter
│   ├── RoutePlanner.java                # Dijkstra's Algorithm for inter-hospital transit
│   ├── ReferralService.java             # Inter-hospital referral & capacity matcher
│   ├── HospitalManagementSystem.java    # Core facade coordinator
│   ├── ConsoleLauncher.java             # Preserved Interactive CLI mode
│   └── Main.java                        # Dual GUI/CLI launcher (Swing on EDT)
│
├── hms/ui/                              # Main UI Frames & Design System
│   ├── UITheme.java                     # Color palette, fonts, card/input helpers
│   ├── LoginFrame.java                  # Demo login authentication window
│   ├── MainFrame.java                   # 1440x900 CardLayout master container
│   │
│   ├── components/                      # Reusable Modern UI Components
│   │   ├── HeaderPanel.java             # Dark navy header, role badge, live clock, logout
│   │   ├── SidebarPanel.java            # 13 ordered navigation tabs with highlight
│   │   ├── SummaryCard.java             # Rounded stat card with hover effect
│   │   ├── StatusCellRenderer.java      # Pill status badges (Green/Red/Orange/Blue)
│   │   ├── StyledButton.java            # Rounded buttons (Primary, Secondary, Success, Danger)
│   │   ├── FormPanel.java               # Aligned GridBag form layout container
│   │   └── ModernTablePanel.java        # Custom styled JTable wrapper (padding, alternating rows)
│   │
│   ├── demo/                            # Shared In-Memory Prototype Store
│   │   └── DemoDataStore.java           # Centralized reactive store with event listeners
│   │
│   ├── panels/                          # 12 Operational Screen Panels
│   │   ├── DashboardPanel.java          # Metric cards, quick actions, census telemetry
│   │   ├── PatientRegistrationPanel.java# Intake form with validation & ID generation
│   │   ├── PatientRecordsPanel.java     # Searchable, filterable patient table
│   │   ├── PriorityQueuePanel.java      # Deterministic triage PriorityQueue (Severity 1-5)
│   │   ├── OPDConsultationPanel.java    # Clinical encounter notes & doctor assignment
│   │   ├── AdmissionPanel.java          # Bed requests & duplicate prevention
│   │   ├── BedManagementPanel.java      # Bed grid with strict CLEANING lifecycle
│   │   ├── DischargePanel.java          # Checkout clearance, bed release to CLEANING
│   │   ├── ReferralPanel.java           # Dijkstra shortest path inter-hospital routing
│   │   ├── DoctorsHospitalsPanel.java   # Tabbed healthcare facilities & physician roster
│   │   ├── PatientHistoryPanel.java     # Chronological audit trail across all events
│   │   └── ReportsPanel.java            # Custom painted Swing bar charts & utilization
│   │
│   └── test/                            # Quality Assurance & Testing
│       └── FrontendTestSuite.java       # Automated 50-point functional test suite
```

---

## 🎨 Visual Design System

- **Primary Navy:** `#142B4A` (Header, sidebar, branding)
- **Primary Blue:** `#2563EB` (Primary action buttons, active navigation)
- **Content Background:** `#F4F7FB` (Light subtle desktop background)
- **Card Background:** `#FFFFFF` (Rounded anti-aliased panels with soft borders)
- **Status Badges:**
  - `AVAILABLE` / `COMPLETED`: Soft Green `#16A34A`
  - `OCCUPIED` / `CRITICAL`: Soft Red `#DC2626`
  - `CLEANING` / `PENDING`: Soft Amber `#F59E0B`
  - `ADMITTED` / `IN QUEUE`: Soft Blue `#2563EB`
- **Typography:** Modern clean system fonts (`Segoe UI` / SansSerif) with hierarchy.

---

## 🚀 How to Compile and Run (Windows)

### Quick One-Click Launch (Batch Scripts)

1. **Launch Desktop GUI:**
   ```bat
   run_gui.bat
   ```
2. **Launch Preserved Command-Line Console:**
   ```bat
   run_console.bat
   ```
3. **Compile All Sources:**
   ```bat
   build.bat
   ```

### Manual Command Line (Using Java 17+ or JDK)

```powershell
# 1. Compile all Java sources
dir /s /b src\*.java > sources.txt
javac -d bin -encoding UTF-8 @sources.txt
del sources.txt

# 2. Run Desktop GUI (Default)
java -cp bin hms.Main

# 3. Run Preserved Console Mode
java -cp bin hms.Main --console

# 4. Run Automated 50-Point Test Suite
java -cp bin hms.ui.test.FrontendTestSuite
```

---

## 🔐 Demo Credentials

- **Administrator:** `admin` / `hospital123`
- **Staff Officer:** `staff` / `hospital123`
*(Any non-empty username/password will authenticate in this prototype stage)*

---

## 📋 Features Implemented

1. **Preserved OOP Backend:** All 14 original OOP classes (`Bed`, `Doctor`, `Hospital`, `Patient`, `RoutePlanner`, etc.) preserved and cleanly packaged in `hms`. A backup copy is preserved in `backup/`.
2. **Shared In-Memory State (`DemoDataStore`):** Real-time observer pattern ensures actions on any panel (registering, admitting, discharging, cleaning, referring) immediately reflect on all other 11 panels.
3. **Deterministic Priority Queue:** Patients sorted by Severity ascending (1 is highest), then arrival time ascending, then sequence number.
4. **Bed Sanitization Lifecycle:** Beds discharged from patients strictly transition to `CLEANING` state. Only the "Finish Bed Cleaning" action restores a bed to `AVAILABLE`. Double allocation is strictly blocked.
5. **Dijkstra Inter-Hospital Routing:** Calculates actual shortest path across a weighted graph network with fictional minutes travel time display.
6. **Zero Database Dependency:** Pure desktop Swing execution without MySQL requirement, ready for future JDBC DAO integration.
