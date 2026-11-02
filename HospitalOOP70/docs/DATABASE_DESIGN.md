# DATABASE ARCHITECTURE & SCHEMA DESIGN — HospitalOOP70

## 1. Database Overview
- **RDBMS Engine:** MySQL Server 8.0
- **Database Name:** `hospital_oop_db`
- **Application User:** `hospital_app` (strictly non-root with `SELECT`, `INSERT`, `UPDATE`, `DELETE` privileges)
- **Host / Port:** `localhost:3306`
- **Security:** Credentials loaded dynamically from the `DB_PASSWORD` environment variable. Never hardcoded.

---

## 2. Entity-Relationship Schema (12 Tables)

### 2.1 `users`
System administrative and clinical user accounts with role-based access control.
- `user_id` INT AUTO_INCREMENT PRIMARY KEY
- `username` VARCHAR(50) UNIQUE NOT NULL
- `password_hash` VARCHAR(255) NOT NULL (SHA-256 cryptographic digest)
- `full_name` VARCHAR(100) NOT NULL
- `role` VARCHAR(50) NOT NULL (`Administrator`, `Doctor`, `Receptionist`, `Hospital Staff`)
- `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP

### 2.2 `hospitals`
Government district health facilities in the clinical network.
- `hospital_id` VARCHAR(20) PRIMARY KEY
- `name` VARCHAR(100) NOT NULL
- `city` VARCHAR(50) NOT NULL
- `total_general_beds` INT DEFAULT 50
- `total_icu_beds` INT DEFAULT 10
- `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP

### 2.3 `departments`
Clinical departments within each hospital.
- `dept_id` VARCHAR(20) PRIMARY KEY
- `hospital_id` VARCHAR(20) REFERENCES `hospitals(hospital_id)`
- `name` VARCHAR(100) NOT NULL

### 2.4 `doctors`
Medical officers and clinical consultants.
- `doctor_id` VARCHAR(20) PRIMARY KEY
- `name` VARCHAR(100) NOT NULL
- `specialty` VARCHAR(100) NOT NULL
- `hospital_id` VARCHAR(20) REFERENCES `hospitals(hospital_id)`
- `department` VARCHAR(100)
- `on_duty` BOOLEAN DEFAULT TRUE

### 2.5 `beds`
Hospital inpatient ward and ICU bed registry.
- `bed_id` VARCHAR(20) PRIMARY KEY
- `hospital_id` VARCHAR(20) REFERENCES `hospitals(hospital_id)`
- `bed_type` VARCHAR(50) NOT NULL (`General Bed`, `ICU Bed`)
- `department` VARCHAR(100) NOT NULL
- `is_occupied` BOOLEAN DEFAULT FALSE
- `is_cleaning` BOOLEAN DEFAULT FALSE
- `status` VARCHAR(20) DEFAULT 'AVAILABLE' (`AVAILABLE`, `RESERVED`, `OCCUPIED`, `CLEANING`, `MAINTENANCE`)
- `current_patient_id` VARCHAR(20)

### 2.6 `patients`
Master patient index across outpatient, inpatient, and emergency services.
- `patient_id` VARCHAR(20) PRIMARY KEY
- `name` VARCHAR(100) NOT NULL
- `age` INT NOT NULL
- `gender` VARCHAR(10) NOT NULL
- `contact_number` VARCHAR(20)
- `address` VARCHAR(255)
- `visit_type` VARCHAR(20) NOT NULL (`OPD`, `IPD`, `EMERGENCY`)
- `priority` INT NOT NULL (1 to 5)
- `department` VARCHAR(100) NOT NULL
- `status` VARCHAR(50) NOT NULL
- `assigned_bed_id` VARCHAR(20)
- `assigned_hospital_id` VARCHAR(20)
- `arrival_sequence` BIGINT NOT NULL
- `registration_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP

### 2.7 `admission_requests`
Requests for inpatient ward or ICU placement.
- `request_id` VARCHAR(20) PRIMARY KEY
- `patient_id` VARCHAR(20) REFERENCES `patients(patient_id)`
- `doctor_id` VARCHAR(20)
- `target_hospital_id` VARCHAR(20)
- `bed_requirement` VARCHAR(50) NOT NULL
- `arrival_sequence` BIGINT NOT NULL
- `status` VARCHAR(20) DEFAULT 'PENDING' (`PENDING`, `ALLOCATED`, `CANCELLED`)
- `request_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP

### 2.8 `admissions`
Historical and active bed stay ledger.
- `admission_id` INT AUTO_INCREMENT PRIMARY KEY
- `patient_id` VARCHAR(20) REFERENCES `patients(patient_id)`
- `bed_id` VARCHAR(20) REFERENCES `beds(bed_id)`
- `hospital_id` VARCHAR(20) REFERENCES `hospitals(hospital_id)`
- `admission_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- `discharge_date` TIMESTAMP NULL
- `discharge_notes` TEXT
- `status` VARCHAR(20) DEFAULT 'ACTIVE' (`ACTIVE`, `DISCHARGED`)

### 2.9 `encounters`
Clinical consultation records (OPD and Emergency triage).
- `encounter_id` VARCHAR(20) PRIMARY KEY
- `patient_id` VARCHAR(20) REFERENCES `patients(patient_id)`
- `doctor_id` VARCHAR(20) REFERENCES `doctors(doctor_id)`
- `doctor_name` VARCHAR(100)
- `department` VARCHAR(100)
- `hospital_id` VARCHAR(20)
- `symptoms` TEXT
- `diagnosis` TEXT
- `treatment_notes` TEXT
- `notes` TEXT
- `status` VARCHAR(20) DEFAULT 'COMPLETED'
- `encounter_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP

### 2.10 `referrals`
Inter-hospital patient transfer records.
- `referral_id` VARCHAR(20) PRIMARY KEY
- `patient_id` VARCHAR(20) REFERENCES `patients(patient_id)`
- `patient_name` VARCHAR(100)
- `source_hospital_id` VARCHAR(20)
- `dest_hospital_id` VARCHAR(20)
- `bed_type` VARCHAR(50)
- `route_minutes` INT NOT NULL
- `status` VARCHAR(20) DEFAULT 'COMPLETED'
- `referral_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP

### 2.11 `hospital_routes`
Weighted graph edge definitions for Dijkstra shortest-path network.
- `route_id` INT AUTO_INCREMENT PRIMARY KEY
- `source_hospital_id` VARCHAR(20) NOT NULL
- `dest_hospital_id` VARCHAR(20) NOT NULL
- `travel_minutes` INT NOT NULL
- UNIQUE KEY `uk_route` (`source_hospital_id`, `dest_hospital_id`)

### 2.12 `patient_history`
Immutable clinical audit log tracking all actions across the system.
- `event_id` VARCHAR(20) PRIMARY KEY
- `patient_id` VARCHAR(20)
- `event_type` VARCHAR(50) NOT NULL
- `description` TEXT NOT NULL
- `staff_user` VARCHAR(100)
- `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP

---

## 3. Safe Schema Execution & Migration
- Schema DDL: `database/schema.sql` (Creates tables with IF NOT EXISTS)
- Seed Data: `database/seed.sql` (Uses `ON DUPLICATE KEY UPDATE` and `WHERE NOT EXISTS` to completely prevent duplicate primary key collisions such as `ERROR 1062 (Duplicate entry 'REQ-301')`)
- Migration: `database/migrations/01_extend_schema.sql` (Adds missing columns safely)
