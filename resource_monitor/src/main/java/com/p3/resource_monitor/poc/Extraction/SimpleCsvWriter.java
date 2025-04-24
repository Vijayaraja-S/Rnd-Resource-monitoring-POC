package com.p3.resource_monitor.poc.Extraction;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class SimpleCsvWriter implements CsvWriterStrategy {

    @Override
    public void writeRecord(List<Object> data, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath, true)) { // append mode
            StringBuilder line = new StringBuilder();
            for (int i = 0; i < data.size(); i++) {
                line.append("\"").append(data.get(i)).append("\"");
                if (i < data.size() - 1) line.append(",");
            }
            writer.write(line.toString());
            writer.write("\n");
        }
    }
}
