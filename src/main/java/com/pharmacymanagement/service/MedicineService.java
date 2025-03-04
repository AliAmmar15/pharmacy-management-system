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

    public void deleteMedicine(Long id) {
        medicineDAO.delete(id);
    }

    private void validateMedicine(Medicine medicine) {
        if (medicine.getName() == null || medicine.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Medicine name is required");
        }

        if (medicine.getGenericName() == null || medicine.getGenericName().trim().isEmpty()) {
            throw new IllegalArgumentException("Generic name is required");
        }

        if (medicine.getUnitPrice() < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }

        if (medicine.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        if (medicine.getExpiryDate() != null && medicine.getExpiryDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Expiry date cannot be in the past");
        }

        if (medicine.getMinimumStockLevel() < 0) {
            throw new IllegalArgumentException("Minimum stock level cannot be negative");
        }
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
}