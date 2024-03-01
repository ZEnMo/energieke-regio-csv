import java.io.IOException;

public class Util {
    static String loadStringFromResource(String resourceName) throws IOException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        return new String(classloader.getResourceAsStream(resourceName).readAllBytes());
    }
}
