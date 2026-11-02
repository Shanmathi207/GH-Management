-- ============================================================================
-- GOVERNMENT HOSPITAL MANAGEMENT SYSTEM (HospitalOOP70)
-- Complete Relational Database Schema (DDL)
-- Target Database: hospital_oop_db
-- Target User: hospital_app
-- ============================================================================

CREATE DATABASE IF NOT EXISTS hospital_oop_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE hospital_oop_db;

-- ----------------------------------------------------------------------------
-- 1. USERS & RBAC AUTHENTICATION TABLE
-- Roles: Administrator, Doctor, Receptionist, Hospital Staff
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'Hospital Staff',
    full_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_role (role)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 2. HOSPITALS DIRECTORY TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hospitals (
    hospital_id VARCHAR(20) PRIMARY KEY,
    hospital_name VARCHAR(150) NOT NULL,
    location VARCHAR(150) NOT NULL,
    general_bed_capacity INT NOT NULL DEFAULT 50,
    icu_bed_capacity INT NOT NULL DEFAULT 15,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_hosp_location (location)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 3. CLINICAL DEPARTMENTS DIRECTORY
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS departments (
    department_id VARCHAR(20) PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) DEFAULT '',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 4. MEDICAL DOCTORS TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS doctors (
    doctor_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    hospital_id VARCHAR(20) NOT NULL,
    on_duty BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_doc_dept (department),
    INDEX idx_doc_duty (on_duty)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 5. INPATIENT BEDS TABLE (General & ICU)
-- States: AVAILABLE, RESERVED, OCCUPIED, CLEANING, MAINTENANCE
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS beds (
    bed_id VARCHAR(20) PRIMARY KEY,
    department VARCHAR(100) NOT NULL,
    hospital_id VARCHAR(20) NOT NULL,
    bed_type VARCHAR(50) NOT NULL, -- 'GENERAL BED' or 'ICU BED'
    occupied BOOLEAN NOT NULL DEFAULT FALSE,
    cleaning BOOLEAN NOT NULL DEFAULT FALSE,
    patient_id VARCHAR(20) DEFAULT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_bed_status (occupied, cleaning, status),
    INDEX idx_bed_type (bed_type),
    INDEX idx_bed_patient (patient_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 6. PATIENTS TABLE
-- Demographics, contact info, visit category, and severity triage
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS patients (
    patient_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(20) NOT NULL,
    contact_number VARCHAR(30) DEFAULT '',
    address VARCHAR(255) DEFAULT '',
    department VARCHAR(100) NOT NULL,
    visit_type VARCHAR(30) NOT NULL, -- 'OPD', 'IPD', 'EMERGENCY'
    required_resource VARCHAR(50) NOT NULL,
    emergency_level VARCHAR(30) NOT NULL, -- 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'NON-URGENT'
    priority INT NOT NULL, -- 1 (Highest) to 5 (Lowest)
    status VARCHAR(50) NOT NULL, -- 'WAITING IN QUEUE', 'ADMITTED - ...', 'DISCHARGED', etc.
    assigned_bed_id VARCHAR(20) DEFAULT NULL,
    assigned_doctor_name VARCHAR(100) DEFAULT NULL,
    assigned_hospital_id VARCHAR(20) DEFAULT 'H001',
    arrival_sequence BIGINT NOT NULL,
    registration_date_time VARCHAR(30) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (assigned_hospital_id) REFERENCES hospitals(hospital_id) ON UPDATE CASCADE,
    INDEX idx_pat_priority (priority, arrival_sequence),
    INDEX idx_pat_status (status),
    INDEX idx_pat_visit_type (visit_type)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 7. ADMISSION REQUESTS TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS admission_requests (
    request_id VARCHAR(30) PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    bed_requirement VARCHAR(50) NOT NULL,
    hospital_id VARCHAR(20) NOT NULL,
    arrival_sequence BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'ALLOCATED', 'CANCELLED', 'REJECTED'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON UPDATE CASCADE,
    INDEX idx_req_status (status)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 8. ADMISSIONS TRACKING TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS admissions (
    admission_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    bed_id VARCHAR(20) NOT NULL,
    hospital_id VARCHAR(20) NOT NULL,
    admitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    discharged_at TIMESTAMP NULL DEFAULT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE', 'DISCHARGED'
    discharge_notes TEXT DEFAULT NULL,
    discharge_summary TEXT DEFAULT NULL,
    discharging_officer VARCHAR(100) DEFAULT NULL,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (bed_id) REFERENCES beds(bed_id) ON UPDATE CASCADE,
    FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON UPDATE CASCADE,
    INDEX idx_adm_status (status)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 9. CLINICAL CONSULTATION ENCOUNTERS TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS encounters (
    encounter_id VARCHAR(30) PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    doctor_id VARCHAR(20) NOT NULL,
    doctor_name VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    hospital_id VARCHAR(20) NOT NULL,
    symptoms TEXT DEFAULT NULL,
    diagnosis TEXT DEFAULT NULL,
    treatment_notes TEXT DEFAULT NULL,
    notes TEXT DEFAULT NULL,
    encounter_date VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON UPDATE CASCADE,
    FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON UPDATE CASCADE,
    INDEX idx_enc_patient (patient_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 10. INTER-HOSPITAL REFERRALS TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS referrals (
    referral_id VARCHAR(30) PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    patient_name VARCHAR(100) NOT NULL,
    source_hospital VARCHAR(50) NOT NULL,
    dest_hospital VARCHAR(50) NOT NULL,
    bed_type VARCHAR(50) NOT NULL,
    travel_minutes INT NOT NULL,
    timestamp VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_ref_patient (patient_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 11. HOSPITAL ROUTE GRAPH EDGES (Dijkstra Shortest Path Weights)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hospital_routes (
    route_id INT AUTO_INCREMENT PRIMARY KEY,
    source_hospital_id VARCHAR(20) NOT NULL,
    dest_hospital_id VARCHAR(20) NOT NULL,
    travel_minutes INT NOT NULL,
    distance_km DOUBLE NOT NULL DEFAULT 0.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (source_hospital_id) REFERENCES hospitals(hospital_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (dest_hospital_id) REFERENCES hospitals(hospital_id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY uk_route (source_hospital_id, dest_hospital_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 12. PATIENT ACTIVITY & AUDIT HISTORY TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS patient_history (
    history_id VARCHAR(30) PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    staff_user VARCHAR(100) NOT NULL,
    timestamp VARCHAR(30) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_hist_patient (patient_id),
    INDEX idx_hist_type (activity_type)
) ENGINE=InnoDB;
