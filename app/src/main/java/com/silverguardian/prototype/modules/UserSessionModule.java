package com.silverguardian.prototype.modules;

import com.silverguardian.prototype.data.Repository;
import com.silverguardian.prototype.models.FamilyMember;
import com.silverguardian.prototype.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserSessionModule {
    private final Repository repository;

    public UserSessionModule(Repository repository) {
        this.repository = repository;
    }

    public List<User> getUsers() {
        return new ArrayList<>(repository.getUsers());
    }

    public User addUser(String name, String pin, int age, String conditions) {
        return repository.addUser(name, pin, age, conditions);
    }

    public void deleteUser(User user) {
        repository.deleteUser(user);
    }

    public void setActiveUser(int userId) {
        repository.setActiveUser(userId);
    }

    public int getActiveUserId() {
        return repository.getActiveUserId();
    }

    public User getActiveUser() {
        for (User user : repository.getUsers()) {
            if (user.id == repository.getActiveUserId()) return user;
        }
        return repository.getUsers().isEmpty() ? null : repository.getUsers().get(0);
    }

    public List<FamilyMember> getFamilyMembers() {
        return new ArrayList<>(repository.getFamilyMembers());
    }

    public String findPrimaryFamilyPhone() {
        for (FamilyMember member : repository.getFamilyMembers()) {
            if (member.online && member.phone != null && !member.phone.isEmpty()) return member.phone;
        }
        for (FamilyMember member : repository.getFamilyMembers()) {
            if (member.phone != null && !member.phone.isEmpty()) return member.phone;
        }
        return null;
    }
}
