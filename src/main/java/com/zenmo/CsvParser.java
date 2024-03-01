package com.zenmo;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class CsvParser {
    public static Map<String, String> parseCsv(String csv) throws IOException {
        var map = new LinkedHashMap<String, String>();

        var correctedCsv = CsvParser.escapeQuotes(csv);

        Iterable<CSVRecord> records = CSVFormat.DEFAULT.builder()
                .setSkipHeaderRecord(false)
                .setAllowMissingColumnNames(false) // headers are missing for the last columns
                .setDelimiter(';')
                .build()
                .parse(new StringReader(correctedCsv));

        var recordList = new ArrayList<CSVRecord>();
        records.forEach(recordList::add);

        if (recordList.size() < 2) {
            System.out.println("---ERROR---");
            System.out.println(correctedCsv);
            throw new RuntimeException("Too few rows (%d). Expected one header row and one data row.".formatted(recordList.size()));
        }

        if (recordList.size() > 2) {
            throw new RuntimeException("Too many rows  (%d). Expected one header row and one data row.".formatted(recordList.size()));
        }

        var headerRecord = recordList.get(0);
        var dataRecord = recordList.get(1);

        for (var i = 0; i < headerRecord.size(); i++) {
            var key = headerRecord.get(i);
            if (key == null || key.isBlank()) {
                key = "Lege Kolom";
            }
            while (map.containsKey(key)) {
                key += "_";
            }

            map.put(key, dataRecord.get(i));
        }

        return map;
    }

    /**
     * The CSV format we receive is borked.
     * This attempts to escape quotes-within-quotes.
     * Not 100% accurate.
     */
    public static String escapeQuotes(String csv) {
        var pattern = Pattern.compile("(?>[^;\n])(?<test>\\\"+)(?!$|;)");
        var matcher = pattern.matcher(csv);
        ArrayList<String> parts = new ArrayList<>();

        var startIndex = 0;
        while (matcher.find()) {
            parts.add(csv.substring(startIndex, matcher.start(1)));
            startIndex = matcher.end(1);
        }
        parts.add(csv.substring(startIndex));

        return String.join("\"\"", parts).trim();
    }
}
