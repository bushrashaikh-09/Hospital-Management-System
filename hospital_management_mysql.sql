-- Safe setup: create the database only when it does not already exist.
CREATE DATABASE IF NOT EXISTS hospital_management
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE hospital_management;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN','USER') NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS patients (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNIQUE,
    name VARCHAR(255) NOT NULL,
    age INT,
    department VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'Active',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS doctors (
    id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(255) NOT NULL,
    specialty VARCHAR(255) NOT NULL,
    services TEXT NOT NULL,
    experience_years INT NOT NULL DEFAULT 0,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS appointments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    patient_name VARCHAR(255) NOT NULL,
    doctor_name VARCHAR(255) NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    department VARCHAR(100) NOT NULL,
    status ENUM('Confirmed','Pending','Completed','Cancelled') NOT NULL DEFAULT 'Pending',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS reports (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    patient_name VARCHAR(255) NOT NULL,
    report_type VARCHAR(100) NOT NULL,
    description TEXT,
    department VARCHAR(255) NOT NULL DEFAULT 'General Medicine',
    sent_to_user BOOLEAN NOT NULL DEFAULT FALSE,
    summary TEXT,
    report_date VARCHAR(20) NOT NULL DEFAULT '2026-01-01',
    status VARCHAR(50) NOT NULL DEFAULT 'Approved',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS invoices (
    id BIGINT NOT NULL AUTO_INCREMENT,
    invoice_number VARCHAR(255) NOT NULL UNIQUE,
    patient_name VARCHAR(255) NOT NULL,
    doctor_name VARCHAR(255) NOT NULL,
    department VARCHAR(255) NOT NULL,
    appointment_date DATE,
    invoice_date DATE NOT NULL,
    due_date DATE,
    amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(50) NOT NULL DEFAULT 'Pending',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    receipt_number VARCHAR(255) NOT NULL UNIQUE,
    patient_name VARCHAR(255) NOT NULL,
    invoice_number VARCHAR(255),
    amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    payment_method VARCHAR(100) NOT NULL DEFAULT 'Cash',
    payment_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'Completed',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Default admin
INSERT INTO users (full_name, email, password, role, enabled)
SELECT 'System Administrator', 'admin@hospital.com', 'admin123', 'ADMIN', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@hospital.com');

-- Default sample users
INSERT INTO users (full_name, email, password, role, enabled)
SELECT 'John Smith', 'john@example.com', 'user123', 'USER', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'john@example.com');

INSERT INTO users (full_name, email, password, role, enabled)
SELECT 'Alice Johnson', 'alice@example.com', 'user123', 'USER', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'alice@example.com');

-- Sample doctors
INSERT INTO doctors (full_name, specialty, services, experience_years, available)
SELECT 'Dr. Rahul Mehta', 'Cardiology', 'ECG, Heart Consultation, Stress Testing', 14, TRUE
WHERE NOT EXISTS (SELECT 1 FROM doctors WHERE full_name = 'Dr. Rahul Mehta');

INSERT INTO doctors (full_name, specialty, services, experience_years, available)
SELECT 'Dr. Priya Shah', 'Neurology', 'MRI Review, Headache Care, Neurology Checkup', 11, TRUE
WHERE NOT EXISTS (SELECT 1 FROM doctors WHERE full_name = 'Dr. Priya Shah');

INSERT INTO doctors (full_name, specialty, services, experience_years, available)
SELECT 'Dr. Ananya Nair', 'Dermatology', 'Skin Consultation, Acne Treatment, Allergy Testing', 9, TRUE
WHERE NOT EXISTS (SELECT 1 FROM doctors WHERE full_name = 'Dr. Ananya Nair');

INSERT INTO doctors (full_name, specialty, services, experience_years, available)
SELECT 'Dr. Karan Singh', 'Orthopedics', 'Joint Care, Bone Scan Review, Physiotherapy', 12, TRUE
WHERE NOT EXISTS (SELECT 1 FROM doctors WHERE full_name = 'Dr. Karan Singh');

INSERT INTO doctors (full_name, specialty, services, experience_years, available)
SELECT 'Dr. Meera Joshi', 'Pediatrics', 'Child Wellness, Vaccination Support, Development Review', 10, TRUE
WHERE NOT EXISTS (SELECT 1 FROM doctors WHERE full_name = 'Dr. Meera Joshi');

INSERT INTO doctors (full_name, specialty, services, experience_years, available)
SELECT 'Dr. Vikram Rao', 'General Medicine', 'General Consultation, Diabetes Care, Preventive Care', 15, TRUE
WHERE NOT EXISTS (SELECT 1 FROM doctors WHERE full_name = 'Dr. Vikram Rao');

INSERT INTO doctors (full_name, specialty, services, experience_years, available)
SELECT 'Dr. Sanya Khanna', 'Gynecology', 'Prenatal Care, Wellness Consultation, Family Planning', 13, TRUE
WHERE NOT EXISTS (SELECT 1 FROM doctors WHERE full_name = 'Dr. Sanya Khanna');

INSERT INTO doctors (full_name, specialty, services, experience_years, available)
SELECT 'Dr. Arjun Das', 'Oncology', 'Cancer Screening, Supportive Care, Treatment Review', 16, TRUE
WHERE NOT EXISTS (SELECT 1 FROM doctors WHERE full_name = 'Dr. Arjun Das');

INSERT INTO doctors (full_name, specialty, services, experience_years, available)
SELECT 'Dr. Nisha Verma', 'ENT', 'Sinus Care, Hearing Test, Speech Review', 8, TRUE
WHERE NOT EXISTS (SELECT 1 FROM doctors WHERE full_name = 'Dr. Nisha Verma');

INSERT INTO doctors (full_name, specialty, services, experience_years, available)
SELECT 'Dr. Rohan Kulkarni', 'Endocrinology', 'Hormone Review, Thyroid Care, Diabetes Planning', 12, TRUE
WHERE NOT EXISTS (SELECT 1 FROM doctors WHERE full_name = 'Dr. Rohan Kulkarni');

INSERT INTO doctors (full_name, specialty, services, experience_years, available)
SELECT 'Dr. Shreya Iyer', 'Gastroenterology', 'Digestive Care, Colonoscopy, Ultrasound Review', 9, TRUE
WHERE NOT EXISTS (SELECT 1 FROM doctors WHERE full_name = 'Dr. Shreya Iyer');

INSERT INTO doctors (full_name, specialty, services, experience_years, available)
SELECT 'Dr. Gautam Nanda', 'Pulmonology', 'Lung Checkup, Breath Therapy, Sleep Evaluation', 10, TRUE
WHERE NOT EXISTS (SELECT 1 FROM doctors WHERE full_name = 'Dr. Gautam Nanda');

INSERT INTO doctors (full_name, specialty, services, experience_years, available)
SELECT 'Dr. Kavya Menon', 'Psychiatry', 'Counseling, Stress Management, Mental Wellness', 11, TRUE
WHERE NOT EXISTS (SELECT 1 FROM doctors WHERE full_name = 'Dr. Kavya Menon');

INSERT INTO doctors (full_name, specialty, services, experience_years, available)
SELECT 'Dr. Aditya Bose', 'Urology', 'Kidney Care, Urine Analysis, Stone Treatment', 10, TRUE
WHERE NOT EXISTS (SELECT 1 FROM doctors WHERE full_name = 'Dr. Aditya Bose');

INSERT INTO doctors (full_name, specialty, services, experience_years, available)
SELECT 'Dr. Tanya Malhotra', 'Rheumatology', 'Joint Pain Care, Autoimmune Review, Recovery Planning', 7, TRUE
WHERE NOT EXISTS (SELECT 1 FROM doctors WHERE full_name = 'Dr. Tanya Malhotra');

INSERT INTO doctors (full_name, specialty, services, experience_years, available)
SELECT 'Dr. Ishaan Patel', 'Nephrology', 'Kidney Function Test, Fluid Management, Dialysis Review', 13, TRUE
WHERE NOT EXISTS (SELECT 1 FROM doctors WHERE full_name = 'Dr. Ishaan Patel');

INSERT INTO doctors (full_name, specialty, services, experience_years, available)
SELECT 'Dr. Simran Gupta', 'Ophthalmology', 'Eye Checkup, Vision Test, Cataract Care', 9, TRUE
WHERE NOT EXISTS (SELECT 1 FROM doctors WHERE full_name = 'Dr. Simran Gupta');

INSERT INTO doctors (full_name, specialty, services, experience_years, available)
SELECT 'Dr. Varun Chawla', 'Dentistry', 'Dental Checkup, Teeth Whitening, Root Canal Care', 8, TRUE
WHERE NOT EXISTS (SELECT 1 FROM doctors WHERE full_name = 'Dr. Varun Chawla');

INSERT INTO doctors (full_name, specialty, services, experience_years, available)
SELECT 'Dr. Aisha Khan', 'Infectious Disease', 'Fever Review, Infection Care, Prevention Advice', 12, TRUE
WHERE NOT EXISTS (SELECT 1 FROM doctors WHERE full_name = 'Dr. Aisha Khan');

INSERT INTO doctors (full_name, specialty, services, experience_years, available)
SELECT 'Dr. Kunal Sharma', 'Hematology', 'Blood Panel, Anemia Care, Transfusion Review', 11, TRUE
WHERE NOT EXISTS (SELECT 1 FROM doctors WHERE full_name = 'Dr. Kunal Sharma');

-- Sample appointments
INSERT INTO appointments (patient_name, doctor_name, appointment_date, appointment_time, department, status, notes)
SELECT 'John Smith', 'Dr. Rahul Mehta', '2026-09-02', '10:30:00', 'Cardiology', 'Confirmed', 'Follow-up consultation'
WHERE NOT EXISTS (SELECT 1 FROM appointments WHERE patient_name = 'John Smith' AND doctor_name = 'Dr. Rahul Mehta');

INSERT INTO appointments (patient_name, doctor_name, appointment_date, appointment_time, department, status, notes)
SELECT 'Alice Johnson', 'Dr. Priya Shah', '2026-09-05', '14:00:00', 'Neurology', 'Pending', 'MRI review'
WHERE NOT EXISTS (SELECT 1 FROM appointments WHERE patient_name = 'Alice Johnson' AND doctor_name = 'Dr. Priya Shah');

-- Sample reports
INSERT INTO reports (title, patient_name, report_type, description)
SELECT 'Cardiology Checkup', 'John Smith', 'Medical', 'Routine heart health review.'
WHERE NOT EXISTS (SELECT 1 FROM reports WHERE title = 'Cardiology Checkup' AND patient_name = 'John Smith');

INSERT INTO reports (title, patient_name, report_type, description)
SELECT 'Neurology Summary', 'Alice Johnson', 'Medical', 'Neurological assessment summary.'
WHERE NOT EXISTS (SELECT 1 FROM reports WHERE title = 'Neurology Summary' AND patient_name = 'Alice Johnson');

SELECT 'Database setup completed successfully.' AS status;
SELECT * FROM users;
SELECT * FROM doctors;
SELECT * FROM appointments;
SELECT * FROM reports;
SELECT * FROM invoices ORDER BY created_at DESC;
SELECT * FROM payments ORDER BY created_at DESC;
