package com.bupt.ta.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Text-file storage using JSON. No database (per project requirements).
 */
public class Storage {
    private static final String DATA_DIR = "data";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path basePath;

    public Storage() {
        this.basePath = Paths.get(DATA_DIR).toAbsolutePath();
    }

    public Storage(String dataDir) {
        this.basePath = Paths.get(dataDir).toAbsolutePath();
    }

    private Path ensureDir() throws IOException {
        if (!Files.exists(basePath)) {
            Files.createDirectories(basePath);
        }
        return basePath;
    }

    private Path file(String name) throws IOException {
        return ensureDir().resolve(name);
    }

    public <T> List<T> loadList(String filename, Class<T> elementType) {
        Path p;
        try {
            p = file(filename);
        } catch (IOException e) {
            return new ArrayList<>();
        }
        if (!Files.exists(p)) {
            return new ArrayList<>();
        }
        try {
            String json = Files.readString(p, StandardCharsets.UTF_8);
            Type listType = TypeToken.getParameterized(List.class, elementType).getType();
            List<T> list = GSON.fromJson(json, listType);
            return list != null ? list : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public <T> void saveList(String filename, List<T> list) throws IOException {
        Path p = file(filename);
        String json = GSON.toJson(list);
        Files.writeString(p, json, StandardCharsets.UTF_8);
    }

    public String getDataDir() {
        return basePath.toString();
    }
}
