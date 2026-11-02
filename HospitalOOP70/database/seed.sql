-- ============================================================================
-- GOVERNMENT HOSPITAL MANAGEMENT SYSTEM (HospitalOOP70)
-- Repeatable Seed Data (DML)
-- Safe for repeated execution without duplicate key errors (ERROR 1062)
-- Target Database: hospital_oop_db
-- ============================================================================

USE hospital_oop_db;

-- ----------------------------------------------------------------------------
-- 1. SEED RBAC USERS
-- Default credentials for academic demonstration:
--   admin     / hospital123  (Role: Administrator)
--   doctor    / hospital123  (Role: Doctor)
--   reception / hospital123  (Role: Receptionist)
--   staff     / hospital123  (Role: Hospital Staff)
-- ----------------------------------------------------------------------------
INSERT INTO users (username, password_hash, role, full_name) VALUES
    ('admin',     'b1be5bca6120e7f722c81525a7d65b7f14b6ac5ab8cbbe7c87c2f0f4a9b6c86a', 'Administrator',  'Dr. System Administrator'),
    ('doctor',    'b1be5bca6120e7f722c81525a7d65b7f14b6ac5ab8cbbe7c87c2f0f4a9b6c86a', 'Doctor',         'Dr. Rajesh Sharma (MD)'),
    ('reception', 'b1be5bca6120e7f722c81525a7d65b7f14b6ac5ab8cbbe7c87c2f0f4a9b6c86a', 'Receptionist',   'Triage Intake Desk'),
    ('staff',     'b1be5bca6120e7f722c81525a7d65b7f14b6ac5ab8cbbe7c87c2f0f4a9b6c86a', 'Hospital Staff',  'Staff Nurse / Ward Manager')
ON DUPLICATE KEY UPDATE 
    password_hash = VALUES(password_hash),
    role = VALUES(role),
    full_name = VALUES(full_name);

-- ----------------------------------------------------------------------------
-- 2. SEED CLINICAL DEPARTMENTS
-- ----------------------------------------------------------------------------
INSERT INTO departments (department_id, department_name, description) VALUES
    ('DEP-01', 'Cardiology',          'Cardiac intensive care, ECG, echocardiography, coronary disease'),
    ('DEP-02', 'ICU / Critical Care', 'Intensive critical care and advanced mechanical ventilation'),
    ('DEP-03', 'General Medicine',    'Outpatient consultation and inpatient general wards'),
    ('DEP-04', 'Surgery',             'General, laparoscopic, and trauma emergency surgical theaters'),
    ('DEP-05', 'Pediatrics',          'Child healthcare, pediatric medicine, and nursery'),
    ('DEP-06', 'Emergency / Trauma',  'Immediate acute resuscitation, triaging, and trauma intervention'),
    ('DEP-07', 'Neurology',           'Brain, spine, stroke, and central nervous system diagnostics')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- ----------------------------------------------------------------------------
-- 3. SEED HOSPITALS NETWORK
-- ----------------------------------------------------------------------------
INSERT INTO hospitals (hospital_id, hospital_name, location, general_bed_capacity, icu_bed_capacity) VALUES
    ('H001', 'Government General Hospital',            'Central District', 80, 20),
    ('H002', 'Government Multi-Specialty Hospital',    'North Zone',      120, 30),
    ('H003', 'District Medical College & Hospital',     'East Zone',       150, 40),
    ('H004', 'City Emergency & Trauma Center',         'South Zone',       60, 25),
    ('H005', 'Suburban Community Health Center',        'West Zone',        40, 10)
ON DUPLICATE KEY UPDATE 
    hospital_name = VALUES(hospital_name),
    location = VALUES(location),
    general_bed_capacity = VALUES(general_bed_capacity),
    icu_bed_capacity = VALUES(icu_bed_capacity);

-- ----------------------------------------------------------------------------
-- 4. SEED DOCTORS ROSTER
-- ----------------------------------------------------------------------------
INSERT INTO doctors (doctor_id, name, department, hospital_id, on_duty) VALUES
    ('DOC-01', 'Dr. Rajesh Sharma',  'Cardiology',          'H001', TRUE),
    ('DOC-02', 'Dr. Ananya Iyer',    'ICU / Critical Care', 'H001', TRUE),
    ('DOC-03', 'Dr. Vikram Patel',   'General Medicine',    'H001', TRUE),
    ('DOC-04', 'Dr. Sneha Roy',      'Surgery',             'H001', FALSE),
    ('DOC-05', 'Dr. Manoj Nair',     'Pediatrics',          'H001', TRUE),
    ('DOC-06', 'Dr. Kavita Joshi',   'Emergency / Trauma',  'H004', TRUE),
    ('DOC-07', 'Dr. Amit Verma',     'Neurology',           'H002', TRUE)
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    department = VALUES(department),
    hospital_id = VALUES(hospital_id),
    on_duty = VALUES(on_duty);

-- ----------------------------------------------------------------------------
-- 5. SEED INPATIENT BEDS
-- ----------------------------------------------------------------------------
INSERT INTO beds (bed_id, department, hospital_id, bed_type, occupied, cleaning, patient_id, status) VALUES
    ('ICU-101',  'ICU',              'H001', 'ICU BED',     TRUE,  FALSE, 'PAT-101', 'OCCUPIED'),
    ('ICU-102',  'ICU',              'H001', 'ICU BED',     FALSE, FALSE, NULL,      'AVAILABLE'),
    ('ICU-103',  'ICU',              'H001', 'ICU BED',     FALSE, FALSE, NULL,      'AVAILABLE'),
    ('ICU-104',  'ICU',              'H001', 'ICU BED',     FALSE, FALSE, NULL,      'AVAILABLE'),
    ('GEN-201',  'General Medicine', 'H001', 'GENERAL BED', TRUE,  FALSE, 'PAT-102', 'OCCUPIED'),
    ('GEN-202',  'General Medicine', 'H001', 'GENERAL BED', FALSE, FALSE, NULL,      'AVAILABLE'),
    ('GEN-203',  'General Medicine', 'H001', 'GENERAL BED', FALSE, FALSE, NULL,      'AVAILABLE'),
    ('GEN-204',  'General Medicine', 'H001', 'GENERAL BED', FALSE, FALSE, NULL,      'AVAILABLE'),
    ('SUR-301',  'Surgery',          'H001', 'GENERAL BED', FALSE, TRUE,  NULL,      'CLEANING'),
    ('SUR-302',  'Surgery',          'H001', 'GENERAL BED', FALSE, FALSE, NULL,      'AVAILABLE'),
    ('CAR-401',  'Cardiology',       'H001', 'GENERAL BED', FALSE, FALSE, NULL,      'AVAILABLE'),
    ('PED-501',  'Pediatrics',       'H001', 'GENERAL BED', FALSE, FALSE, NULL,      'AVAILABLE'),
    ('ICU-H201', 'ICU',              'H002', 'ICU BED',     FALSE, FALSE, NULL,      'AVAILABLE'),
    ('GEN-H201', 'General Ward',     'H002', 'GENERAL BED', FALSE, FALSE, NULL,      'AVAILABLE'),
    ('ICU-H301', 'ICU',              'H003', 'ICU BED',     FALSE, FALSE, NULL,      'AVAILABLE'),
    ('GEN-H301', 'General Ward',     'H003', 'GENERAL BED', FALSE, FALSE, NULL,      'AVAILABLE'),
    ('ICU-H401', 'ICU',              'H004', 'ICU BED',     FALSE, FALSE, NULL,      'AVAILABLE'),
    ('GEN-H401', 'General Ward',     'H004', 'GENERAL BED', FALSE, FALSE, NULL,      'AVAILABLE'),
    ('GEN-H501', 'General Ward',     'H005', 'GENERAL BED', FALSE, FALSE, NULL,      'AVAILABLE')
ON DUPLICATE KEY UPDATE 
    department = VALUES(department),
    hospital_id = VALUES(hospital_id),
    bed_type = VALUES(bed_type),
    occupied = VALUES(occupied),
    cleaning = VALUES(cleaning),
    patient_id = VALUES(patient_id),
    status = VALUES(status);

-- ----------------------------------------------------------------------------
-- 6. SEED PATIENTS CENSUS
-- ----------------------------------------------------------------------------
INSERT INTO patients (patient_id, name, age, gender, contact_number, address, department, visit_type, required_resource, emergency_level, priority, status, assigned_bed_id, assigned_doctor_name, assigned_hospital_id, arrival_sequence, registration_date_time) VALUES
    ('PAT-101', 'Kavitha Ranganathan', 58, 'Female', '+91 98401 23456', '12 Gandhi Road, Chennai',       'ICU',              'IPD',       'ICU Bed',     'CRITICAL', 1, 'ADMITTED - ICU BED',     'ICU-101', 'Dr. Ananya Iyer',   'H001', 1, '2026-10-12 08:30:00'),
    ('PAT-102', 'Murugan Velu',        45, 'Male',   '+91 98402 34567', '45 Anna Nagar, Madurai',        'General Medicine', 'IPD',       'General Bed', 'MEDIUM',   3, 'ADMITTED - GENERAL BED', 'GEN-201', 'Dr. Vikram Patel',  'H001', 2, '2026-10-12 09:15:00'),
    ('PAT-103', 'Abdul Hameed',        34, 'Male',   '+91 98403 45678', '78 Triplicane High Rd, Chennai','Emergency / Trauma','EMERGENCY', 'ICU Bed',     'CRITICAL', 1, 'WAITING IN QUEUE',       NULL,      'Dr. Kavita Joshi',  'H001', 3, '2026-10-12 10:00:00'),
    ('PAT-104', 'Saraswathi Ammal',    72, 'Female', '+91 98404 56789', '21 Temple St, Trichy',          'Cardiology',       'OPD',       'General Bed', 'HIGH',     2, 'WAITING IN QUEUE',       NULL,      'Dr. Rajesh Sharma', 'H001', 4, '2026-10-12 10:20:00'),
    ('PAT-105', 'Dinesh Karthik',      26, 'Male',   '+91 98405 67890', '9 North St, Salem',             'General Medicine', 'OPD',       'General Bed', 'MEDIUM',   3, 'WAITING IN QUEUE',       NULL,      'Dr. Vikram Patel',  'H001', 5, '2026-10-12 10:45:00'),
    ('PAT-106', 'Deepa Meenakshi',     39, 'Female', '+91 98406 78901', '33 Bharathi Nagar, Coimbatore',  'Pediatrics',       'OPD',       'General Bed', 'LOW',      4, 'WAITING IN QUEUE',       NULL,      'Dr. Manoj Nair',    'H001', 6, '2026-10-12 11:00:00')
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    contact_number = VALUES(contact_number),
    address = VALUES(address),
    status = VALUES(status);

-- ----------------------------------------------------------------------------
-- 7. SEED ADMISSION REQUESTS (Idempotent - Prevents ERROR 1062)
-- ----------------------------------------------------------------------------
INSERT INTO admission_requests (request_id, patient_id, bed_requirement, hospital_id, arrival_sequence, status) VALUES
    ('REQ-301', 'PAT-101', 'ICU Bed',     'H001', 1, 'ALLOCATED'),
    ('REQ-302', 'PAT-102', 'General Bed', 'H001', 2, 'ALLOCATED'),
    ('REQ-303', 'PAT-103', 'ICU Bed',     'H001', 3, 'PENDING')
ON DUPLICATE KEY UPDATE 
    bed_requirement = VALUES(bed_requirement),
    status = VALUES(status);

-- ----------------------------------------------------------------------------
-- 8. SEED ACTIVE ADMISSIONS
-- ----------------------------------------------------------------------------
INSERT INTO admissions (patient_id, bed_id, hospital_id, admitted_at, status)
SELECT 'PAT-101', 'ICU-101', 'H001', '2026-10-12 08:45:00', 'ACTIVE'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM admissions WHERE patient_id = 'PAT-101' AND status = 'ACTIVE');

INSERT INTO admissions (patient_id, bed_id, hospital_id, admitted_at, status)
SELECT 'PAT-102', 'GEN-201', 'H001', '2026-10-12 09:30:00', 'ACTIVE'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM admissions WHERE patient_id = 'PAT-102' AND status = 'ACTIVE');

-- ----------------------------------------------------------------------------
-- 9. SEED HOSPITAL ROUTE GRAPH EDGES (Dijkstra Min-Heap Routing)
-- ----------------------------------------------------------------------------
INSERT INTO hospital_routes (source_hospital_id, dest_hospital_id, travel_minutes, distance_km) VALUES
    ('H001', 'H002', 18, 12.5),
    ('H001', 'H003', 25, 18.0),
    ('H001', 'H004', 12,  8.2),
    ('H001', 'H005', 30, 22.0),
    ('H002', 'H001', 18, 12.5),
    ('H002', 'H003', 15, 10.0),
    ('H003', 'H001', 25, 18.0),
    ('H003', 'H002', 15, 10.0),
    ('H003', 'H004', 22, 16.5),
    ('H004', 'H001', 12,  8.2),
    ('H004', 'H003', 22, 16.5),
    ('H005', 'H001', 30, 22.0)
ON DUPLICATE KEY UPDATE 
    travel_minutes = VALUES(travel_minutes),
    distance_km = VALUES(distance_km);

-- ----------------------------------------------------------------------------
-- 10. SEED AUDIT & ACTIVITY HISTORY
-- ----------------------------------------------------------------------------
INSERT INTO patient_history (history_id, patient_id, activity_type, description, staff_user, timestamp) VALUES
    ('HIST-1001', 'PAT-101', 'REGISTRATION', 'Registered as IPD [Severity 1 - Critical]', 'Dr. Ananya Iyer', '2026-10-12 08:30:00'),
    ('HIST-1002', 'PAT-101', 'ADMISSION',    'Admitted to Bed ICU-101 in Government General Hospital', 'Dr. System Administrator', '2026-10-12 08:45:00'),
    ('HIST-1003', 'PAT-102', 'REGISTRATION', 'Registered as IPD [Severity 3 - Medium]', 'Staff Nurse', '2026-10-12 09:15:00'),
    ('HIST-1004', 'PAT-102', 'ADMISSION',    'Admitted to Bed GEN-201 in Government General Hospital', 'Dr. System Administrator', '2026-10-12 09:30:00'),
    ('HIST-1005', 'PAT-103', 'REGISTRATION', 'Emergency arrival triaged as Priority 1 (Critical)', 'Triage Intake Desk', '2026-10-12 10:00:00'),
    ('HIST-1006', 'PAT-104', 'REGISTRATION', 'OPD visit registered under Cardiology (Severity 2)', 'Triage Intake Desk', '2026-10-12 10:20:00')
ON DUPLICATE KEY UPDATE description = VALUES(description);
