package pack.smartwaste.Utils;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlUtils {
    //public static final String REACT_URL = "http://10.100.94.8:5173";
    public static final String REACT_URL_LOCALHOST = "http://localhost:5173";
    public static final String REACT_URL = "http://192.168.1.9:5173";

    public static String extractUniqueId(String url) {
        try {
            URI uri = new URI(url);
            String path = uri.getPath(); // Get the path part of the URL

            // Extract the unique identifier (e.g., "9c598287-34a2-4faa-b151-5bd055083674.jpg")
            String[] parts = path.split("/");
            if (parts.length > 0) {
                String lastPart = parts[parts.length - 1];
                return decodeUrl(lastPart); // Decode URL-encoded characters
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
        return null;
    }

    private static String decodeUrl(String urlPart) {
        try {
            return java.net.URLDecoder.decode(urlPart, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode URL part: " + urlPart, e);
        }
    }
}
