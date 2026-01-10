package org.example;

import java.io.IOException;

public interface Storage {
    void save(DataSnapshot data, String dataSource) throws IOException;
    DataSnapshot load(String dataSource) throws IOException, ClassNotFoundException;
}
