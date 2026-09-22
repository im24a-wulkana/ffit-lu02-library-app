package ch.bzz.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Laedt die Datei "config.properties" aus dem Projektverzeichnis und macht
 * deren Werte verfuegbar. Dort stehen die Zugangsdaten zur Datenbank, die
 * nicht ins Repository gehoeren.
 */
public final class Config {

    private static final String CONFIG_FILE = "config.properties";

    private static final Properties PROPERTIES = load();

    private Config() {
        // Utility-Klasse, wird nicht instanziert
    }

    /**
     * Liefert den Wert zum angegebenen Schluessel.
     *
     * @throws IllegalStateException wenn der Schluessel fehlt oder leer ist
     */
    public static String get(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Der Wert '" + key + "' fehlt in " + CONFIG_FILE);
        }
        return value;
    }

    /**
     * Liefert alle Werte, damit sie direkt an die EntityManagerFactory
     * uebergeben werden koennen.
     */
    public static Properties getProperties() {
        return PROPERTIES;
    }

    private static Properties load() {
        Properties properties = new Properties();

        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException(CONFIG_FILE + " konnte nicht gelesen werden. "
                    + "Bitte config.properties.template kopieren und ausfuellen.", e);
        }

        return properties;
    }
}
