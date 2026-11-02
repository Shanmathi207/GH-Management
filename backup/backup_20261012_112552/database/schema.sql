-- ============================================================================
-- GOVERNMENT HOSPITAL MANAGEMENT SYSTEM (HospitalOOP70)
-- Complete Relational Database Schema & Seed Data
-- Target Database: hospital_oop_db
-- Target User: hospital_app
-- ============================================================================

CREATE DATABASE IF NOT EXISTS hospital_oop_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE hospital_oop_db;

-- ----------------------------------------------------------------------------
-- 1. USERS & AUTHENTICATION TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'Staff Officer',
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
-- 3. MEDICAL DOCTORS TABLE
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
-- 4. INPATIENT BEDS TABLE (General & ICU)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS beds (
    bed_id VARCHAR(20) PRIMARY KEY,
    department VARCHAR(100) NOT NULL,
    hospital_id VARCHAR(20) NOT NULL,
    bed_type VARCHAR(50) NOT NULL, -- 'GENERAL BED' or 'ICU BED'
    occupied BOOLEAN NOT NULL DEFAULT FALSE,
    cleaning BOOLEAN NOT NULL DEFAULT FALSE,
    patient_id VARCHAR(20) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_bed_status (occupied, cleaning),
    INDEX idx_bed_type (bed_type),
    INDEX idx_bed_patient (patient_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 5. PATIENTS TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS patients (
    patient_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(20) NOT NULL,
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
-- 6. ADMISSION REQUESTS TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS admission_requests (
    request_id VARCHAR(30) PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    bed_requirement VARCHAR(50) NOT NULL,
    hospital_id VARCHAR(20) NOT NULL,
    arrival_sequence BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'ALLOCATED', 'REJECTED'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON UPDATE CASCADE,
    INDEX idx_req_status (status)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 7. ADMISSIONS TRACKING TABLE
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
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (bed_id) REFERENCES beds(bed_id) ON UPDATE CASCADE,
    FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON UPDATE CASCADE,
    INDEX idx_adm_status (status)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 8. CLINICAL CONSULTATION ENCOUNTERS TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS encounters (
    encounter_id VARCHAR(30) PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    doctor_id VARCHAR(20) NOT NULL,
    doctor_name VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    hospital_id VARCHAR(20) NOT NULL,
    notes TEXT,
    encounter_date VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON UPDATE CASCADE,
    FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON UPDATE CASCADE,
    INDEX idx_enc_patient (patient_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 9. INTER-HOSPITAL REFERRALS TABLE
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_ref_patient (patient_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 10. PATIENT ACTIVITY HISTORY AUDIT TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS patient_history (
    history_id VARCHAR(30) PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    activity_type VARCHAR(50) NOT NULL, -- 'REGISTRATION', 'CONSULTATION', 'ADMISSION', 'DISCHARGE', 'BED CLEANING', 'REFERRAL', 'DUTY UPDATE'
    description TEXT NOT NULL,
    staff_user VARCHAR(100) NOT NULL,
    timestamp VARCHAR(30) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_hist_patient (patient_id),
    INDEX idx_hist_type (activity_type)
) ENGINE=InnoDB;

-- ============================================================================
-- SEED INITIAL CLINICAL & RESOURCE DATA
-- ============================================================================

-- Users (Administrator and Staff Officer)
INSERT INTO users (username, password_hash, role, full_name) VALUES
    ('admin', 'hospital123', 'Administrator', 'Dr. System Administrator'),
    ('staff', 'hospital123', 'Staff Officer', 'Nurse Intake Officer')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- Hospitals Network
INSERT INTO hospitals (hospital_id, hospital_name, location, general_bed_capacity, icu_bed_capacity) VALUES
    ('H001', 'Government General Hospital', 'Central District', 80, 20),
    ('H002', 'Government Multi-Specialty Hospital', 'North Zone', 120, 30),
    ('H003', 'District Medical College & Hospital', 'East Zone', 150, 40),
    ('H004', 'City Emergency & Trauma Center', 'South Zone', 60, 25),
    ('H005', 'Suburban Community Health Center', 'West Zone', 40, 10)
ON DUPLICATE KEY UPDATE hospital_name=VALUES(hospital_name);

-- Doctors Roster
INSERT INTO doctors (doctor_id, name, department, hospital_id, on_duty) VALUES
    ('DOC-01', 'Dr. Rajesh Sharma', 'Cardiology', 'H001', TRUE),
    ('DOC-02', 'Dr. Ananya Iyer', 'ICU / Critical Care', 'H001', TRUE),
    ('DOC-03', 'Dr. Vikram Patel', 'General Medicine', 'H001', TRUE),
    ('DOC-04', 'Dr. Sneha Roy', 'Surgery', 'H001', FALSE),
    ('DOC-05', 'Dr. Manoj Nair', 'Pediatrics', 'H001', TRUE),
    ('DOC-06', 'Dr. Kavita Joshi', 'Emergency / Trauma', 'H004', TRUE),
    ('DOC-07', 'Dr. Amit Verma', 'Neurology', 'H002', TRUE)
ON DUPLICATE KEY UPDATE name=VALUES(name), on_duty=VALUES(on_duty);

-- Beds Grid
INSERT INTO beds (bed_id, department, hospital_id, bed_type, occupied, cleaning, patient_id) VALUES
    ('ICU-101', 'ICU', 'H001', 'ICU BED', TRUE, FALSE, 'PAT-101'),
    ('ICU-102', 'ICU', 'H001', 'ICU BED', FALSE, FALSE, NULL),
    ('ICU-103', 'ICU', 'H001', 'ICU BED', FALSE, FALSE, NULL),
    ('ICU-104', 'ICU', 'H001', 'ICU BED', FALSE, FALSE, NULL),
    ('GEN-201', 'General Medicine', 'H001', 'GENERAL BED', TRUE, FALSE, 'PAT-102'),
    ('GEN-202', 'General Medicine', 'H001', 'GENERAL BED', FALSE, FALSE, NULL),
    ('GEN-203', 'General Medicine', 'H001', 'GENERAL BED', FALSE, FALSE, NULL),
    ('GEN-204', 'General Medicine', 'H001', 'GENERAL BED', FALSE, FALSE, NULL),
    ('SUR-301', 'Surgery', 'H001', 'GENERAL BED', FALSE, TRUE, NULL), -- In cleaning turnaround
    ('SUR-302', 'Surgery', 'H001', 'GENERAL BED', FALSE, FALSE, NULL),
    ('CAR-401', 'Cardiology', 'H001', 'GENERAL BED', FALSE, FALSE, NULL),
    ('PED-501', 'Pediatrics', 'H001', 'GENERAL BED', FALSE, FALSE, NULL),
    ('ICU-H201', 'ICU', 'H002', 'ICU BED', FALSE, FALSE, NULL),
    ('GEN-H201', 'General Ward', 'H002', 'GENERAL BED', FALSE, FALSE, NULL),
    ('ICU-H301', 'ICU', 'H003', 'ICU BED', FALSE, FALSE, NULL),
    ('GEN-H301', 'General Ward', 'H003', 'GENERAL BED', FALSE, FALSE, NULL),
    ('ICU-H401', 'ICU', 'H004', 'ICU BED', FALSE, FALSE, NULL),
    ('GEN-H401', 'General Ward', 'H004', 'GENERAL BED', FALSE, FALSE, NULL),
    ('GEN-H501', 'General Ward', 'H005', 'GENERAL BED', FALSE, FALSE, NULL)
ON DUPLICATE KEY UPDATE occupied=VALUES(occupied), cleaning=VALUES(cleaning), patient_id=VALUES(patient_id);

-- Patients Census
INSERT INTO patients (patient_id, name, age, gender, department, visit_type, required_resource, emergency_level, priority, status, assigned_bed_id, assigned_doctor_name, assigned_hospital_id, arrival_sequence, registration_date_time) VALUES
    ('PAT-101', 'Kavitha Ranganathan', 58, 'Female', 'ICU', 'IPD', 'ICU Bed', 'CRITICAL', 1, 'ADMITTED - ICU BED', 'ICU-101', 'Dr. Ananya Iyer', 'H001', 1, '2026-10-12 08:30:00'),
    ('PAT-102', 'Murugan Velu', 45, 'Male', 'General Medicine', 'IPD', 'General Bed', 'MEDIUM', 3, 'ADMITTED - GENERAL BED', 'GEN-201', 'Dr. Vikram Patel', 'H001', 2, '2026-10-12 09:15:00'),
    ('PAT-103', 'Abdul Hameed', 34, 'Male', 'Trauma', 'EMERGENCY', 'ICU Bed', 'CRITICAL', 1, 'WAITING IN QUEUE', NULL, NULL, 'H001', 3, '2026-10-12 10:00:00'),
    ('PAT-104', 'Saraswathi Ammal', 72, 'Female', 'Cardiology', 'OPD', 'General Bed', 'HIGH', 2, 'WAITING IN QUEUE', NULL, NULL, 'H001', 4, '2026-10-12 10:20:00'),
    ('PAT-105', 'Dinesh Karthik', 26, 'Male', 'General Medicine', 'OPD', 'General Bed', 'MEDIUM', 3, 'WAITING IN QUEUE', NULL, NULL, 'H001', 5, '2026-10-12 10:45:00'),
    ('PAT-106', 'Deepa Meenakshi', 39, 'Female', 'Pediatrics', 'OPD', 'General Bed', 'LOW', 4, 'WAITING IN QUEUE', NULL, NULL, 'H001', 6, '2026-10-12 11:00:00')
ON DUPLICATE KEY UPDATE name=VALUES(name), status=VALUES(status);

-- Active Admissions
INSERT INTO admissions (patient_id, bed_id, hospital_id, admitted_at, status) VALUES
    ('PAT-101', 'ICU-101', 'H001', '2026-10-12 08:45:00', 'ACTIVE'),
    ('PAT-102', 'GEN-201', 'H001', '2026-10-12 09:30:00', 'ACTIVE');

-- Admission Requests
INSERT INTO admission_requests (request_id, patient_id, bed_requirement, hospital_id, arrival_sequence, status) VALUES
    ('REQ-301', 'PAT-101', 'ICU Bed', 'H001', 1, 'ALLOCATED'),
    ('REQ-302', 'PAT-102', 'General Bed', 'H001', 2, 'ALLOCATED'),
    ('REQ-303', 'PAT-103', 'ICU Bed', 'H001', 3, 'PENDING');

-- Audit History
INSERT INTO patient_history (history_id, patient_id, activity_type, description, staff_user, timestamp) VALUES
    ('HIST-1001', 'PAT-101', 'REGISTRATION', 'Registered as IPD [Severity 1 - Critical]', 'Dr. Ananya Iyer', '2026-10-12 08:30:00'),
    ('HIST-1002', 'PAT-101', 'ADMISSION', 'Admitted to Bed ICU-101 in Government General Hospital', 'Admin System', '2026-10-12 08:45:00'),
    ('HIST-1003', 'PAT-102', 'REGISTRATION', 'Registered as IPD [Severity 3 - Medium]', 'Staff Officer', '2026-10-12 09:15:00'),
    ('HIST-1004', 'PAT-102', 'ADMISSION', 'Admitted to Bed GEN-201 in Government General Hospital', 'Admin System', '2026-10-12 09:30:00'),
    ('HIST-1005', 'PAT-103', 'REGISTRATION', 'Emergency arrival triaged as Priority 1 (Critical)', 'Trauma Nurse', '2026-10-12 10:00:00'),
    ('HIST-1006', 'PAT-104', 'REGISTRATION', 'OPD visit registered under Cardiology (Severity 2)', 'Staff Officer', '2026-10-12 10:20:00');
