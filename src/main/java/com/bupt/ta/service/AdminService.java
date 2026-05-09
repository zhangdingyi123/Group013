package com.bupt.ta.service;

import com.bupt.ta.model.Admin;
import com.bupt.ta.storage.Storage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AdminService {
    public List<Admin> findAll() throws IOException {
        return Storage.loadAdmins();
    }

    public Optional<Admin> findById(String id) throws IOException {
        return Storage.loadAdmins().stream().filter(a -> id.equals(a.getId())).findFirst();
    }

    public Optional<Admin> findByEmail(String email) throws IOException {
        return Storage.loadAdmins().stream()
                .filter(a -> email != null && email.equalsIgnoreCase(a.getEmail())).findFirst();
    }

    public Admin create(String name, String email, String passwordHash) throws IOException {
        List<Admin> list = Storage.loadAdmins();
        if (list.stream().anyMatch(a -> email != null && email.equalsIgnoreCase(a.getEmail()))) {
            return null;
        }
        Admin admin = new Admin(UUID.randomUUID().toString(),
                name != null ? name.trim() : "",
                email != null ? email.trim() : "",
                passwordHash);
        list.add(admin);
        Storage.saveAdmins(list);
        return admin;
    }
}
