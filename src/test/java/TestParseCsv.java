import com.zenmo.CsvParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestParseCsv {
    @Test
    void validCsv() throws IOException {
        String validCsv = Util.loadStringFromResource("valid.csv");

        var result = CsvParser.parseCsv(validCsv);
        var expected = new LinkedHashMap<>();
        expected.put("h1", "asdf");
        expected.put("h2", "wasd");

        assertEquals(expected, result);
    }

    @Test
    void invalidCsv() throws IOException {
        String invalidCsv = Util.loadStringFromResource("invalid.csv");

        var result = CsvParser.parseCsv(invalidCsv);
        var expected = new LinkedHashMap<>();
        expected.put("h1", "asdf");
        expected.put("h2", """
                w
                hoi "internal"
                asd""");

        assertEquals(expected, result);
    }
}
