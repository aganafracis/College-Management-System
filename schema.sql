-- =====================================================================
-- Team 3 Shared Authentication & Profile Database Schema
-- Version: 1.0.0
-- Database Engine: InnoDB
-- Character Set: utf8mb4 / utf8mb4_unicode_ci
-- =====================================================================

CREATE DATABASE IF NOT EXISTS team3_auth_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE team3_auth_db;

-- ---------------------------------------------------------------------
-- 1. Accounts Table (Shared Authentication Entity)
-- Stores credentials and user role. Passwords stored as PBKDF2 hashes.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS accounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('STUDENT', 'FACULTY') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_accounts_username (username),
    INDEX idx_accounts_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 2. Student Profiles Table
-- 1-to-1 relationship with accounts. Stores student-specific attributes.
-- Enforces uniqueness on student register number and student email.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS student_profiles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    register_number VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    date_of_birth DATE NOT NULL,
    department VARCHAR(20) NOT NULL,
    semester VARCHAR(10) NOT NULL,
    course_program VARCHAR(50) NOT NULL,
    terms_accepted BOOLEAN NOT NULL DEFAULT TRUE,
    terms_accepted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_student_account FOREIGN KEY (account_id)
        REFERENCES accounts(id) ON DELETE CASCADE,
    INDEX idx_student_reg_no (register_number),
    INDEX idx_student_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 3. Faculty Profiles Table
-- 1-to-1 relationship with accounts. Stores faculty-specific attributes.
-- Enforces uniqueness on faculty ID and faculty official email.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS faculty_profiles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    faculty_id VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    office_extension VARCHAR(20) DEFAULT NULL,
    department VARCHAR(20) NOT NULL,
    designation VARCHAR(50) NOT NULL,
    specialization VARCHAR(100) DEFAULT NULL,
    current_course_load VARCHAR(30) NOT NULL,
    terms_accepted BOOLEAN NOT NULL DEFAULT TRUE,
    terms_accepted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_faculty_account FOREIGN KEY (account_id)
        REFERENCES accounts(id) ON DELETE CASCADE,
    INDEX idx_faculty_id (faculty_id),
    INDEX idx_faculty_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
