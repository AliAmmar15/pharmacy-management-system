package com.pharmacymanagement.service;

import com.pharmacymanagement.dao.MedicineDAO;
import com.pharmacymanagement.model.Medicine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MedicineService {
    private static final Logger logger = LoggerFactory.getLogger(MedicineService.class);
    private final MedicineDAO medicineDAO = new MedicineDAO();

    public Medicine addMedicine(Medicine medicine) {
        validateMedicine(medicine);
        return medicineDAO.save(medicine);
    }

    public void updateMedicine(Medicine medicine) {
        validateMedicine(medicine);
        medicineDAO.update(medicine);
    }

    public Medicine getMedicineById(Long medicineId) {
        return medicineDAO.findById(medicineId)
            .orElseThrow(() -> new RuntimeException("Medicine not found with id: " + medicineId));
    }

    public List<Medicine> getAllMedicines() {
        return medicineDAO.findAll();
    }

    public void deleteMedicine(Long medicineId) {
        medicineDAO.delete(medicineId);
        logger.info("Medicine deleted with ID: {}", medicineId);
    }

    private boolean validateMedicine(Medicine medicine) {
        if (medicine == null) return false;
        return medicine.getMedicineId() != null; // Changed getId() to getMedicineId()
    }

    public List<Medicine> getLowStockMedicines() {
        return medicineDAO.findAll().stream()
                .filter(m -> m.getStockQuantity() <= m.getMinimumStockLevel())
                .toList();
    }

    public List<Medicine> getExpiringMedicines(int daysThreshold) {
        LocalDate thresholdDate = LocalDate.now().plusDays(daysThreshold);
        return medicineDAO.findAll().stream()
                .filter(m -> m.getExpiryDate() != null && 
                           !m.getExpiryDate().isAfter(thresholdDate))
                .toList();
    }

    public void updateStock(Long medicineId, int quantityChange) {
        Medicine medicine = getMedicineById(medicineId);
        if (medicine != null) {
            medicine.setStockQuantity(medicine.getStockQuantity() + quantityChange);
            saveMedicine(medicine);
            logger.info("Stock updated for medicine: {}", medicine.getName());
        }
    }

    public void saveMedicine(Medicine medicine) {
        validateMedicine(medicine);
        if (medicine.getMedicineId() == null) {
            medicineDAO.save(medicine);
        } else {
            medicineDAO.update(medicine);
        }
        logger.info("Medicine saved: {}", medicine.getName());
    }
}