package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.file.Path;

public class JsonStorage implements Storage {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void save(DataSnapshot data, String dataSource) throws IOException {
        Path path = Path.of(dataSource);
        try (Writer w = new FileWriter(path.toFile())) { gson.toJson(data, w); }
    }
    @Override
    public DataSnapshot load(String dataSource) throws IOException {
        Path path = Path.of(dataSource);
        try (Reader r = new FileReader(path.toFile())) { return gson.fromJson(r, DataSnapshot.class); }
    }
}
