package com.silverguardian.prototype.models;

public class MedicineLibraryItem {
    public int id;
    public String disease;
    public String name;
    public String brand;
    public String type;
    public String description;

    public MedicineLibraryItem(int id, String disease, String name, String brand, String type, String description) {
        this.id = id;
        this.disease = disease;
        this.name = name;
        this.brand = brand;
        this.type = type;
        this.description = description;
    }
}
