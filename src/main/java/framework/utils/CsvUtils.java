package framework.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class CsvUtils {
    public static List<CSVRecord> read(String resourcePath) {
        try {
            Reader in = new InputStreamReader(CsvUtils.class.getClassLoader().getResourceAsStream(resourcePath));
            return new ArrayList<>(CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in).getRecords());
        } catch (Exception e) {
            throw new RuntimeException("Read CSV failed: " + resourcePath, e);
        }
    }
}