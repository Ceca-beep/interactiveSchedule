package org.example;

import java.io.IOException;
import java.nio.file.Path;

public interface Storage {
    void save(DataSnapshot data, Path path) throws IOException;
    DataSnapshot load(Path path) throws IOException, ClassNotFoundException;
}
