# UML CLASS DIAGRAM & OBJECT-ORIENTED DESIGN — HospitalOOP70

## 1. Class Diagram (Mermaid)

```mermaid
classDiagram
    %% Core Abstract Bed Hierarchy
    class Bed {
        <<abstract>>
        -String bedId
        -String bedType
        -String department
        -String hospitalId
        -String patientId
        -String status
        -boolean occupied
        -boolean cleaning
        -boolean underMaintenance
        +admitPatient(patientId: String) void
        +dischargePatient() void
        +finishCleaning() void
        +reserveBed(patientId: String) void
        +setMaintenance(underMaintenance: boolean) void
        +supportsRequirement(requiredType: String)* boolean
        +getStatusDescription() String
    }

    class GeneralBed {
        +supportsRequirement(requiredType: String) boolean
    }

    class ICUBed {
        +supportsRequirement(requiredType: String) boolean
    }

    Bed <|-- GeneralBed : Inheritance
    Bed <|-- ICUBed : Inheritance

    %% Hospital Aggregation
    class Hospital {
        -String hospitalId
        -String name
        -String city
        -int totalGeneralBeds
        -int totalICUBeds
        -List~Doctor~ doctors
        -List~Bed~ beds
        +addDoctor(doc: Doctor) void
        +addBed(bed: Bed) void
        +getAvailableGeneralBedsCount() int
        +getAvailableICUBedsCount() int
    }

    Hospital "1" *-- "many" Bed : Aggregates
    Hospital "1" *-- "many" Doctor : Aggregates

    %% Doctor & Patient
    class Doctor {
        -String doctorId
        -String name
        -String specialty
        -String hospitalId
        -boolean onDuty
        +setDutyStatus(onDuty: boolean) void
    }

    class Patient {
        -String patientId
        -String name
        -int age
        -String gender
        -String contactNumber
        -String address
        -String visitType
        -int priority
        -String department
        -String status
        -String assignedBedId
        -String assignedHospitalId
        -long arrivalSequence
        -String registrationDateTime
        +updateDemographics(name, age, gender, contact, address) void
        +getEmergencyLevel() String
    }

    %% Encounter & Admission
    class Encounter {
        -String encounterId
        -String patientId
        -String doctorId
        -String doctorName
        -String department
        -String hospitalId
        -String symptoms
        -String diagnosis
        -String treatmentNotes
        -String notes
        -String encounterDateTime
        -String status
    }

    class AdmissionRequest {
        -String requestId
        -Patient patient
        -long arrivalSequence
        -String bedRequirement
        -String targetHospitalId
        -String requestDateTime
        -String status
        +setStatus(status: String) void
    }

    Patient "1" <-- "many" Encounter : Associated
    Patient "1" <-- "many" AdmissionRequest : Requests

    %% Admission Policy
    class AdmissionPolicy {
        <<interface>>
        +findCompatibleBed(hospital: Hospital, request: AdmissionRequest) Bed
    }

    class DefaultAdmissionPolicy {
        +findCompatibleBed(hospital: Hospital, request: AdmissionRequest) Bed
    }

    AdmissionPolicy <|.. DefaultAdmissionPolicy : Implements

    %% Graph & Referral
    class RoutePlanner {
        -Map~String, List~RouteEdge~~ adjacencyList
        +addEdge(from: String, to: String, travelMinutes: int) void
        +findShortestPath(startHospitalId: String, destHospitalId: String) RouteResult
    }

    class ReferralService {
        -RoutePlanner routePlanner
        +findEligibleDestinations(sourceId: String, bedType: String, hospitals: List~Hospital~) List~DestinationCandidate~
    }

    ReferralService --> RoutePlanner : Uses
```

---

## 2. Core OOP Principles Applied

| OOP Principle | Implementation in HospitalOOP70 |
|---|---|
| **Encapsulation** | Strict private fields with guarded getters/setters, parameter validation (e.g. `age >= 1 && age <= 125`), and internal state mutators (`dischargePatient()`, `finishCleaning()`). |
| **Inheritance** | Abstract superclass `Bed` extended by specialized subclasses `GeneralBed` and `ICUBed`, sharing common bed attributes while specializing equipment types. |
| **Polymorphism** | Runtime dispatch through `Bed.supportsRequirement(requiredType)`. Hospitals and data stores handle mixed collections of `Bed` references uniformly without downcasting. |
| **Abstraction** | Abstract contract `Bed` and admission decision interface `AdmissionPolicy` hiding internal traversal and prioritization logic from callers. |
| **Composition & Aggregation** | `Hospital` aggregates collections of `Doctor` and `Bed` entities. `Encounter` and `AdmissionRequest` compose clinical patient records. |
