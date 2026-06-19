package com.silverguardian.prototype.modules;

import com.silverguardian.prototype.data.Repository;
import com.silverguardian.prototype.models.Medicine;
import com.silverguardian.prototype.models.MedicineLibraryItem;

import java.util.ArrayList;
import java.util.List;

public class MedicineReminderModule {
    public static final String ALL_TYPES = "ALL_TYPES";

    private final Repository repository;

    public MedicineReminderModule(Repository repository) {
        this.repository = repository;
    }

    public List<Medicine> getMedicines() { return new ArrayList<>(repository.getMedicines()); }

    public List<Medicine> getMedicines(String selectedType) {
        List<Medicine> visible = new ArrayList<>();
        for (Medicine medicine : repository.getMedicines()) {
            if (selectedType == null || selectedType.isEmpty() || ALL_TYPES.equals(selectedType) || selectedType.equals(medicine.type)) visible.add(medicine);
        }
        return visible;
    }

    public int getTakenCount() {
        int taken = 0;
        for (Medicine medicine : repository.getMedicines()) if (medicine.takenToday) taken++;
        return taken;
    }

    public List<String> getMedicineTypes() { return new ArrayList<>(repository.medicineTypes()); }
    public List<MedicineLibraryItem> searchMedicineLibrary(String keyword) { return repository.searchMedicineLibrary(keyword); }
    public Medicine addMedicine(String name, String type, String time, String method, String description) { return repository.addMedicine(name, type, time, method, description); }
    public void toggleMedicineTaken(Medicine medicine, boolean taken) { repository.toggleMedicineTaken(medicine, taken); }
    public void deleteMedicine(Medicine medicine) { repository.deleteMedicine(medicine); }
}