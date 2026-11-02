# USER MANUAL & SYSTEM GUIDE — HospitalOOP70

## 1. System Requirements & Setup
- **Operating System:** Windows 10 / Windows 11 (PowerShell 5.1+ or 7+)
- **Java Development Kit:** OpenJDK 21 or Java 17 (Detected at `C:\Program Files\Android\Android Studio\jbr\bin`)
- **Database Engine (Optional for offline demo):** MySQL Server 8.0 (`localhost:3306`, Database: `hospital_oop_db`, User: `hospital_app`)

---

## 2. Launching the Application

### 2.1 Compiling the Codebase
Open Windows PowerShell, navigate to the project directory, and execute:
```powershell
cd E:\HospitalOOP70\HospitalOOP70
.\build.bat
```

### 2.2 Launching the Modern Swing Desktop GUI
```powershell
.\run_gui.bat
```

### 2.3 Launching the Interactive Console CLI
```powershell
.\run_console.bat
```

### 2.4 Running the Automated Integration Tests
```powershell
& "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" "-Djava.awt.headless=true" -cp "bin;lib/*" hms.test.FullProjectIntegrationTest
```

---

## 3. Login & Authentication
Upon launching the GUI, the clinical portal login dialog is displayed.

### 3.1 Demo Credentials:
| Role | Username | Password | Permitted Operations |
|---|---|---|---|
| **Administrator** | `admin` | `hospital123` | Complete access to all 17 navigation views and system settings |
| **Doctor** | `doctor` | `hospital123` | OPD Consultations, Emergency Registration, Clinical History, Physician Roster |
| **Receptionist** | `receptionist` | `hospital123` | Patient Registration, Priority Queue, Patient Directory, Hospital Referrals |
| **Hospital Staff** | `staff` | `hospital123` | Inpatient Bed Grid, Sanitization Turnaround, Patient Discharge Processing |

---

## 4. Operational Screen Walkthroughs

### 4.1 Dashboard
- Real-time census counters: Total Registered Patients, Waiting Queue, Inpatient Admissions, Available General Beds, Available ICU Beds, and Cumulative Discharges.
- Quick actions: `+ Register Patient`, `Bed Management`, `⚡ Call Next Patient`.
- Live feed of recent patient activity and admission logs.

### 4.2 Patient Registration
1. Select Target Department and Visit Type (`OPD`, `IPD`, or `EMERGENCY`).
2. Enter Patient Full Name, Age (1–125), Contact Number, and Residence Address.
3. Select Triage Severity (1 to 5).
4. Click **Register Patient** to enroll the patient into MySQL and the active Java PriorityQueue.

### 4.3 Priority Queue
- Displays waiting patients sorted deterministically: Priority 1 cases appear first, ordered by arrival timestamp and sequence number.
- Click **⚡ Call Next Patient** to call the highest-priority patient for immediate clinical encounter.

### 4.4 OPD Consultation
1. Select patient from the waiting dropdown.
2. Select consulting physician.
3. Record presenting symptoms, clinical diagnosis, and prescribed treatment notes.
4. If inpatient stay is required, select bed requirement and click **Submit Admission Request**.
5. Click **Complete Consultation Encounter** to save permanently.

### 4.5 Bed Management
- Color-coded bed cards displaying bed status:
  - 🟢 **AVAILABLE**: Ready for intake.
  - 🟡 **RESERVED**: Earmarked for incoming case.
  - 🔴 **OCCUPIED**: Currently assigned to inpatient.
  - 🟠 **CLEANING**: Undergoing sanitization turnaround.
  - ⚪ **MAINTENANCE**: Engineering lock.
- Filter by ward or facility.
- Click **Finish Bed Cleaning** on a cleaning bed to verify sanitization and restore it to `AVAILABLE`.

### 4.6 Patient Discharge
1. Select an admitted patient from the list or table.
2. Enter clinical discharge summary notes.
3. Click **🖨 Preview Summary** to inspect the draft certificate.
4. Click **Process Patient Discharge** to confirm departure.
5. The bed automatically enters the `CLEANING` infection control state, and an official printable discharge certificate is generated.

### 4.7 Hospital Referrals (Dijkstra Shortest Path)
1. Select patient requiring transfer.
2. Choose source facility and required bed type (`General Bed` or `ICU Bed`).
3. Click **Find Referral Destinations (Dijkstra Routing)**.
4. The system evaluates partner facilities with available bed quotas and computes the shortest weighted route in minutes.
5. Click **Confirm Hospital Referral** to execute the transfer.
