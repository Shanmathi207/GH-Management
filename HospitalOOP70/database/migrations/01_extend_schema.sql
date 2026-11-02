-- ============================================================================
-- MIGRATION 01: EXTEND EXISTING TABLES SAFELY
-- Target: hospital_oop_db
-- ============================================================================

USE hospital_oop_db;

-- 1. Add contact_number and address to patients if not exists
SET @exist_col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'hospital_oop_db' AND TABLE_NAME = 'patients' AND COLUMN_NAME = 'contact_number');
SET @sql := IF(@exist_col = 0, 'ALTER TABLE patients ADD COLUMN contact_number VARCHAR(30) DEFAULT \'\' AFTER gender', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist_col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'hospital_oop_db' AND TABLE_NAME = 'patients' AND COLUMN_NAME = 'address');
SET @sql := IF(@exist_col = 0, 'ALTER TABLE patients ADD COLUMN address VARCHAR(255) DEFAULT \'\' AFTER contact_number', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2. Add status column to beds if not exists (AVAILABLE, RESERVED, OCCUPIED, CLEANING, MAINTENANCE)
SET @exist_col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'hospital_oop_db' AND TABLE_NAME = 'beds' AND COLUMN_NAME = 'status');
SET @sql := IF(@exist_col = 0, 'ALTER TABLE beds ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT \'AVAILABLE\' AFTER patient_id', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3. Add clinical encounter notes fields (symptoms, diagnosis, treatment_notes)
SET @exist_col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'hospital_oop_db' AND TABLE_NAME = 'encounters' AND COLUMN_NAME = 'symptoms');
SET @sql := IF(@exist_col = 0, 'ALTER TABLE encounters ADD COLUMN symptoms TEXT DEFAULT NULL AFTER hospital_id', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist_col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'hospital_oop_db' AND TABLE_NAME = 'encounters' AND COLUMN_NAME = 'diagnosis');
SET @sql := IF(@exist_col = 0, 'ALTER TABLE encounters ADD COLUMN diagnosis TEXT DEFAULT NULL AFTER symptoms', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist_col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'hospital_oop_db' AND TABLE_NAME = 'encounters' AND COLUMN_NAME = 'treatment_notes');
SET @sql := IF(@exist_col = 0, 'ALTER TABLE encounters ADD COLUMN treatment_notes TEXT DEFAULT NULL AFTER diagnosis', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4. Add discharge summary fields to admissions
SET @exist_col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'hospital_oop_db' AND TABLE_NAME = 'admissions' AND COLUMN_NAME = 'discharge_summary');
SET @sql := IF(@exist_col = 0, 'ALTER TABLE admissions ADD COLUMN discharge_summary TEXT DEFAULT NULL AFTER discharge_notes', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist_col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'hospital_oop_db' AND TABLE_NAME = 'admissions' AND COLUMN_NAME = 'discharging_officer');
SET @sql := IF(@exist_col = 0, 'ALTER TABLE admissions ADD COLUMN discharging_officer VARCHAR(100) DEFAULT NULL AFTER discharge_summary', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 5. Create departments table if not exists
CREATE TABLE IF NOT EXISTS departments (
    department_id VARCHAR(20) PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) DEFAULT '',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 6. Create hospital_routes table if not exists
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
