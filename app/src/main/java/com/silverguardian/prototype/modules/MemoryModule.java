package com.silverguardian.prototype.modules;

import com.silverguardian.prototype.data.Repository;
import com.silverguardian.prototype.models.MemoryRecord;

import java.util.ArrayList;
import java.util.List;

public class MemoryModule {
    public static final String ALL = "ALL";

    private final Repository repository;

    public MemoryModule(Repository repository) {
        this.repository = repository;
    }

    public List<MemoryRecord> getMemories() { return new ArrayList<>(repository.getMemories()); }

    public List<MemoryRecord> getMemories(String category) {
        List<MemoryRecord> visible = new ArrayList<>();
        for (MemoryRecord record : repository.getMemories()) {
            if (category == null || category.isEmpty() || ALL.equals(category) || category.equals(record.category)) visible.add(record);
        }
        return visible;
    }

    public List<String> getCategories() { return new ArrayList<>(repository.memoryCategories()); }
    public MemoryRecord addMemory(String content, String category) { return repository.addMemory(content, category); }
    public void deleteMemory(MemoryRecord record) { repository.deleteMemory(record); }
}