# CLINICAL & OPERATIONAL WORKFLOWS — HospitalOOP70

## 1. Outpatient & Inpatient Clinical Journey

```mermaid
flowchart TD
    A([Patient Arrival]) --> B{Is Case Critical / Trauma?}
    B -- Yes --> C[Emergency Intake Panel<br/>Severity 1 or 2]
    B -- No --> D[Standard Registration Panel<br/>Severity 3, 4, or 5]
    
    C --> E[Deterministic PriorityQueue<br/>Auto-Triage Sorting]
    D --> E
    
    E --> F[OPD Consultation Screen<br/>⚡ Call Next Patient]
    F --> G[Doctor Encounter Dossier<br/>Record Symptoms, Diagnosis & Treatment]
    
    G --> H{Requires Inpatient Bed?}
    H -- No --> I[Prescription & Routine Outpatient Discharge]
    H -- Yes --> J{Local Beds Available?}
    
    J -- Yes --> K[Create Admission Request<br/>General or ICU Bed]
    K --> L[Atomic Bed Allocation<br/>Status -> OCCUPIED]
    L --> M[Inpatient Care & Ward Monitoring]
    
    M --> N[Discharge Processing Screen<br/>Authorized Doctor Sign-off]
    N --> O[Printable Clinical Discharge Certificate]
    O --> P[Bed Status -> CLEANING<br/>Locked against new admissions]
    P --> Q[Bed Management Screen<br/>Sanitization turnaround verified]
    Q --> R[Bed Restored to AVAILABLE]
    
    J -- No --> S[Hospital Referral Management<br/>Run Dijkstra Route Planner]
    S --> T[Select Destination Partner Hospital<br/>Verify Bed Quota & Shortest Route]
    T --> U[Execute Inter-Hospital Patient Transfer]
```

---

## 2. Bed Sanitization & Turnaround Lifecycle
A vital government infection control protocol enforced in HospitalOOP70:

1. **AVAILABLE:** Bed is unoccupied and sanitized. Ready for new admission.
2. **RESERVED:** Bed is temporarily earmarked for an inbound emergency case or incoming transfer.
3. **OCCUPIED:** Patient is admitted. Protected against concurrent double-booking via atomic SQL updates and memory locks.
4. **CLEANING:** When a patient is discharged, the bed **NEVER** transitions immediately to available. It transitions to `CLEANING`. Any attempt to assign a cleaning bed is rejected.
5. **MAINTENANCE:** Engineering inspection lock for broken mechanical or monitoring fixtures.
6. **FINISH CLEANING:** Housekeeping confirms deep sanitation, linen change, and disinfection, restoring the unit to `AVAILABLE`.

---

## 3. Inter-Hospital Referral Algorithm (Dijkstra's Shortest Path)
1. **Source Node:** The origin hospital lacking available ICU or general beds.
2. **Eligibility Filter:** Only target hospitals with `availableCompatibleBeds > 0` are evaluated.
3. **Graph Traversal:** Evaluates weighted edges (fictional highway travel minutes) using a min-heap priority queue.
4. **Optimal Path Selection:** Produces the optimal multi-hop transfer path with the lowest cumulative route weight.
