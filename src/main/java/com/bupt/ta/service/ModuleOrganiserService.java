package com.bupt.ta.service;

import com.bupt.ta.model.ModuleOrganiser;
import com.bupt.ta.storage.Storage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ModuleOrganiserService {
    public List<ModuleOrganiser> findAll() throws IOException {
        return Storage.loadModuleOrganisers();
    }

    public Optional<ModuleOrganiser> findById(String id) throws IOException {
        return Storage.loadModuleOrganisers().stream().filter(m -> id.equals(m.getId())).findFirst();
    }

    public Optional<ModuleOrganiser> findByEmail(String email) throws IOException {
        return Storage.loadModuleOrganisers().stream()
                .filter(m -> email.equalsIgnoreCase(m.getEmail())).findFirst();
    }

    public ModuleOrganiser create(String name, String email, String passwordHash, String department) throws IOException {
        List<ModuleOrganiser> list = Storage.loadModuleOrganisers();
        if (list.stream().anyMatch(m -> email.equalsIgnoreCase(m.getEmail()))) {
            return null;
        }
        ModuleOrganiser mo = new ModuleOrganiser(UUID.randomUUID().toString(), name, email, passwordHash, department);
        list.add(mo);
        Storage.saveModuleOrganisers(list);
        return mo;
    }

    public boolean update(ModuleOrganiser mo) throws IOException {
        List<ModuleOrganiser> list = Storage.loadModuleOrganisers();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(mo.getId())) {
                list.set(i, mo);
                Storage.saveModuleOrganisers(list);
                return true;
            }
        }
        return false;
    }
}
