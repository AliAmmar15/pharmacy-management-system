-- Database Schema for Pharmacy Management System

-- Patients Table
CREATE TABLE IF NOT EXISTS patients (
    patient_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    age INTEGER NOT NULL,
    gender TEXT NOT NULL,
    contact_number TEXT,
    email TEXT,
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Doctors Table
CREATE TABLE IF NOT EXISTS doctors (
    doctor_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    specialization TEXT,
    contact_number TEXT,
    email TEXT,
    license_number TEXT UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Medicine Inventory Table
CREATE TABLE IF NOT EXISTS medicine_inventory (
    medicine_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    generic_name TEXT,
    manufacturer TEXT,
    category TEXT,
    unit_price DECIMAL(10,2) NOT NULL,
    stock_quantity INTEGER NOT NULL,
    minimum_stock_level INTEGER DEFAULT 10,
    expiry_date DATE,
    batch_number TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Medical Conditions Table
CREATE TABLE IF NOT EXISTS medical_conditions (
    condition_id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER,
    condition_name TEXT NOT NULL,
    diagnosis_date DATE,
    notes TEXT,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id)
);

-- Drug Interactions Table
CREATE TABLE IF NOT EXISTS drug_interactions (
    interaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
    medicine_id_1 INTEGER,
    medicine_id_2 INTEGER,
    severity TEXT NOT NULL,
    description TEXT,
    FOREIGN KEY (medicine_id_1) REFERENCES medicine_inventory(medicine_id),
    FOREIGN KEY (medicine_id_2) REFERENCES medicine_inventory(medicine_id)
);

-- Sales Transactions Table
CREATE TABLE IF NOT EXISTS sales_transactions (
    sale_id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER,
    doctor_id INTEGER,
    total_amount DECIMAL(10,2) NOT NULL,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_method TEXT,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id)
);

-- Sale Items Table
CREATE TABLE IF NOT EXISTS sale_items (
    sale_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    sale_id INTEGER,
    medicine_id INTEGER,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (sale_id) REFERENCES sales_transactions(sale_id),
    FOREIGN KEY (medicine_id) REFERENCES medicine_inventory(medicine_id)
);

-- Patient Medications Table
CREATE TABLE IF NOT EXISTS patient_medications (
    medication_id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER,
    medicine_id INTEGER,
    doctor_id INTEGER,
    prescribed_date DATE NOT NULL,
    end_date DATE,
    dosage TEXT,
    frequency TEXT,
    active BOOLEAN DEFAULT 1,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
    FOREIGN KEY (medicine_id) REFERENCES medicine_inventory(medicine_id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_patient_name ON patients(name);
CREATE INDEX IF NOT EXISTS idx_doctor_name ON doctors(name);
CREATE INDEX IF NOT EXISTS idx_medicine_name ON medicine_inventory(name);
CREATE INDEX IF NOT EXISTS idx_sale_date ON sales_transactions(sale_date);
CREATE INDEX IF NOT EXISTS idx_patient_medications_active ON patient_medications(active);