import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zenmo.CsvParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class TestCleanCsv {
    @Test
    void emptyLine() {
        var result = CsvParser.escapeQuotes("");
        assertEquals("", result);
    }

    @Test
    void validCsv() throws IOException {
        String validCsv = Util.loadStringFromResource("valid.csv");

        var result = CsvParser.escapeQuotes(validCsv);
        assertEquals(validCsv, result);
    }

    @Test
    void invvalidCsv() throws IOException {
        String invalidCsv = Util.loadStringFromResource("invalid.csv");
        String cleanedCsv = Util.loadStringFromResource("invalid_cleaned.csv");

        var result = CsvParser.escapeQuotes(invalidCsv);
        assertEquals(cleanedCsv, result);
    }
}

