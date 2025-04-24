package com.p3.resource_monitor.poc.Extraction;

import java.io.IOException;
import java.util.List;

public interface CsvWriterStrategy {
    void writeRecord(List<Object> data, String filePath) throws IOException;
}
