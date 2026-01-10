package org.example;

import java.io.*;
import java.nio.file.Path;

public class ObjectStreamStorage implements Storage {
    @Override
    public void save(DataSnapshot data, String dataSource) throws IOException {
        Path path = Path.of(dataSource);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path.toFile()))) {
            out.writeObject(data);
        }
    }
    @Override
    public DataSnapshot load(String dataSource) throws IOException, ClassNotFoundException {
        Path path = Path.of(dataSource);
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(path.toFile()))) {
            return (DataSnapshot) in.readObject();
        }
    }
}
