package com.bupt.ta.service;

import com.bupt.ta.model.ModuleOrganiser;
import com.bupt.ta.storage.Storage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ModuleOrganiserService {
    private static final String FILE = "module_organisers.json";
    private final Storage storage;

    public ModuleOrganiserService(Storage storage) {
        this.storage = storage;
    }

    public List<ModuleOrganiser> findAll() {
        return storage.loadList(FILE, ModuleOrganiser.class);
    }

    public Optional<ModuleOrganiser> findById(String id) {
        return findAll().stream().filter(m -> id.equals(m.getId())).findFirst();
    }

    public ModuleOrganiser create(String name, String email) {
        ModuleOrganiser mo = new ModuleOrganiser(UUID.randomUUID().toString(), name, email);
        save(mo);
        return mo;
    }

    public void save(ModuleOrganiser mo) {
        List<ModuleOrganiser> list = findAll();
        list.removeIf(m -> m.getId().equals(mo.getId()));
        list.add(mo);
        try {
            storage.saveList(FILE, list);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save module organisers", e);
        }
    }
}
