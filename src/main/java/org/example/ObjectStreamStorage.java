package org.example;

import java.io.*;
import java.nio.file.Path;

public class ObjectStreamStorage implements Storage {
    @Override
    public void save(DataSnapshot data, Path path) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path.toFile()))) {
            out.writeObject(data);
        }
    }
    @Override
    public DataSnapshot load(Path path) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(path.toFile()))) {
            return (DataSnapshot) in.readObject();
        }
    }
}
