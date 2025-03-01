package com.pharmacymanagement.service;

import com.pharmacymanagement.dao.MedicineDAO;
import com.pharmacymanagement.model.Medicine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class MedicineService {
    private static final Logger logger = LoggerFactory.getLogger(MedicineService.class);
    private final MedicineDAO medicineDAO;

    public MedicineService() {
        this.medicineDAO = new MedicineDAO();
    }

    public Medicine addMedicine(Medicine medicine) {
        validateMedicine(medicine);
        return medicineDAO.save(medicine);
    }

    public void updateMedicine(Medicine medicine) {
        validateMedicine(medicine);
        medicineDAO.update(medicine);
    }

    public Optional<Medicine> getMedicineById(Long id) {
        return medicineDAO.findById(id);
    }

    public List<Medicine> getAllMedicines() {
        return medicineDAO.findAll();
    }

    public List<Medicine> getLowStockMedicines() {
        return medicineDAO.findLowStock();
    }

    public void deleteMedicine(Long id) {
        medicineDAO.delete(id);
    }

    public void updateStock(Long medicineId, int quantity) {
        Medicine medicine = medicineDAO.findById(medicineId)
            .orElseThrow(() -> new RuntimeException("Medicine not found"));
        
        int newQuantity = medicine.getStockQuantity() + quantity;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Insufficient stock");
        }
        
        medicine.setStockQuantity(newQuantity);
        medicineDAO.update(medicine);
        
        if (medicine.isLowStock()) {
            logger.warn("Low stock alert for medicine: {}", medicine.getName());
        }
    }

    private void validateMedicine(Medicine medicine) {
        if (medicine.getName() == null || medicine.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Medicine name cannot be empty");
        }
        
        if (medicine.getUnitPrice() == null || medicine.getUnitPrice().signum() <= 0) {
            throw new IllegalArgumentException("Unit price must be greater than zero");
        }
        
        if (medicine.getStockQuantity() == null || medicine.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        
        if (medicine.getMinimumStockLevel() == null || medicine.getMinimumStockLevel() < 0) {
            throw new IllegalArgumentException("Minimum stock level cannot be negative");
        }
        
        if (medicine.getExpiryDate() != null && medicine.getExpiryDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Expiry date cannot be in the past");
        }
    }
}