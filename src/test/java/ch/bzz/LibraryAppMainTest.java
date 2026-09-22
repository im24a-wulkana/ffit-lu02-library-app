package ch.bzz;

import ch.bzz.config.Config;
import ch.bzz.model.Book;
import ch.bzz.model.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testet die Applikation ueber die Konsole: die Eingabe wird vorgegeben und
 * die Ausgabe geprueft. Die Tests brauchen eine erreichbare Datenbank, deren
 * Zugangsdaten in config.properties stehen.
 */
class LibraryAppMainTest {

    private static EntityManagerFactory factory;

    /** Legt zwei Buecher an, auf die sich die Tests verlassen koennen. */
    @BeforeAll
    static void setUpTestData() {
        factory = Persistence.createEntityManagerFactory("localPU", Config.getProperties());

        try (EntityManager entityManager = factory.createEntityManager()) {
            entityManager.getTransaction().begin();
            entityManager.merge(new Book(1, "9780134685991", "Effective Java", "Joshua Bloch", 2018));
            entityManager.merge(new Book(2, "9780596009205", "Head First Java", "Kathy Sierra, Bert Bates", 2005));
            entityManager.getTransaction().commit();
        }
    }

    @AfterAll
    static void tearDown() {
        if (factory != null) {
            factory.close();
        }
    }

    @Test
    void testQuitEndsProgramWithoutError() {
        // Arrange
        prepareStreams("quit\n");

        // Act + Assert
        assertDoesNotThrow(() -> LibraryAppMain.main(new String[]{}),
                "Das Programm sollte sich ohne Exception beenden");
    }

    @Test
    void testUnknownCommandContainsInput() {
        // Arrange
        ByteArrayOutputStream out = prepareStreams("foobar\nquit\n");

        // Act
        LibraryAppMain.main(new String[]{});

        // Assert
        assertTrue(out.toString().contains("foobar"),
                "Die Ausgabe sollte die ungueltige Eingabe enthalten");
    }

    @Test
    void testHelpListsAllCommands() {
        // Arrange
        ByteArrayOutputStream out = prepareStreams("help\nquit\n");

        // Act
        LibraryAppMain.main(new String[]{});

        // Assert
        String output = out.toString();
        assertTrue(output.contains("help"), "Die Ausgabe sollte 'help' enthalten");
        assertTrue(output.contains("quit"), "Die Ausgabe sollte 'quit' enthalten");
        assertTrue(output.contains("listBooks"), "Die Ausgabe sollte 'listBooks' enthalten");
        assertTrue(output.contains("importBooks"), "Die Ausgabe sollte 'importBooks' enthalten");
        assertTrue(output.contains("createUser"), "Die Ausgabe sollte 'createUser' enthalten");
    }

    @Test
    void testListBooksPrintsBooksFromDatabase() {
        // Arrange
        ByteArrayOutputStream out = prepareStreams("listBooks\nquit\n");

        // Act
        LibraryAppMain.main(new String[]{});

        // Assert
        String output = out.toString();
        assertTrue(output.contains("Effective Java"), "Die Ausgabe sollte das erste Buch enthalten");
        assertTrue(output.contains("Head First Java"), "Die Ausgabe sollte das zweite Buch enthalten");
    }

    @Test
    void testListBooksWithLimitPrintsOnlyOneBook() {
        // Arrange
        ByteArrayOutputStream out = prepareStreams("listBooks 1\nquit\n");

        // Act
        LibraryAppMain.main(new String[]{});

        // Assert
        String output = out.toString();
        assertTrue(output.contains("Effective Java"), "Die Ausgabe sollte das erste Buch enthalten");
        assertFalse(output.contains("Head First Java"), "Die Ausgabe sollte das zweite Buch nicht enthalten");
    }

    @Test
    void testListBooksWithInvalidLimitDoesNotThrow() {
        // Arrange
        ByteArrayOutputStream out = prepareStreams("listBooks SIEBEN\nquit\n");

        // Act + Assert
        assertDoesNotThrow(() -> LibraryAppMain.main(new String[]{}),
                "Eine ungueltige Zahl sollte keine Exception ausloesen");
        assertFalse(out.toString().isEmpty(),
                "Die Ausgabe sollte auf die ungueltige Zahl hinweisen");
    }

    @Test
    void testImportBooksImportsFromTsvFile() throws URISyntaxException {
        // Arrange
        URL resource = getClass().getClassLoader().getResource("test_books_import.tsv");
        assertNotNull(resource, "Die Testdatei sollte vorhanden sein");
        Path path = Paths.get(resource.toURI());
        ByteArrayOutputStream out = prepareStreams("importBooks " + path + "\nlistBooks 4\nquit\n");

        // Act
        LibraryAppMain.main(new String[]{});

        // Assert
        String output = out.toString();
        assertTrue(output.contains("Domain-Driven Design"), "Das Buch mit der id 3 sollte importiert sein");
        assertTrue(output.contains("Refactoring"), "Das Buch mit der id 4 sollte importiert sein");
        assertFalse(output.contains("Clean Architecture"), "Das Buch mit der id 5 liegt ausserhalb des Limits");
    }

    @Test
    void testImportBooksWithMissingFileDoesNotThrow() {
        // Arrange
        String path = "NICHTVORHANDEN.tsv";
        assertNull(getClass().getClassLoader().getResource(path), "Die Datei sollte nicht existieren");
        ByteArrayOutputStream out = prepareStreams("importBooks " + path + "\nquit\n");

        // Act + Assert
        assertDoesNotThrow(() -> LibraryAppMain.main(new String[]{}),
                "Ein ungueltiger Pfad sollte keine Exception ausloesen");
        assertFalse(out.toString().isEmpty(),
                "Die Ausgabe sollte auf die fehlende Datei hinweisen");
    }

    @Test
    void testCreateUserStoresUserWithHashedPassword() {
        // Arrange
        String email = "max.mustermann@example.com";
        deleteUser(email);
        prepareStreams("createUser Max Mustermann 1990-05-21 " + email + " geheim123\nquit\n");

        // Act
        LibraryAppMain.main(new String[]{});

        // Assert
        try (EntityManager entityManager = factory.createEntityManager()) {
            User user = entityManager
                    .createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            assertNotNull(user, "Der Benutzer sollte in der Datenbank stehen");
            assertEquals("Max", user.getFirstname());
            assertEquals("Mustermann", user.getLastname());
            assertEquals("1990-05-21", user.getDateOfBirth().toString());
            assertNotNull(user.getPasswordSalt(), "Das Salt sollte gesetzt sein");
            assertNotNull(user.getPasswordHash(), "Der Hash sollte gesetzt sein");
            assertNotEquals("geheim123", user.getPasswordHash(),
                    "Das Passwort darf nicht im Klartext gespeichert werden");
        }
    }

    @Test
    void testCreateUserWithMissingArgumentsDoesNotThrow() {
        // Arrange
        ByteArrayOutputStream out = prepareStreams("createUser Max Mustermann\nquit\n");

        // Act + Assert
        assertDoesNotThrow(() -> LibraryAppMain.main(new String[]{}),
                "Fehlende Argumente sollten keine Exception ausloesen");
        assertFalse(out.toString().isEmpty(),
                "Die Ausgabe sollte auf die fehlenden Argumente hinweisen");
    }

    @Test
    void testCreateUserWithInvalidDateDoesNotThrow() {
        // Arrange
        ByteArrayOutputStream out = prepareStreams(
                "createUser Max Mustermann KEINDATUM max.mustermann@example.com geheim123\nquit\n");

        // Act + Assert
        assertDoesNotThrow(() -> LibraryAppMain.main(new String[]{}),
                "Ein ungueltiges Datum sollte keine Exception ausloesen");
        assertFalse(out.toString().isEmpty(),
                "Die Ausgabe sollte auf das ungueltige Datum hinweisen");
    }

    private void deleteUser(String email) {
        try (EntityManager entityManager = factory.createEntityManager()) {
            entityManager.getTransaction().begin();
            entityManager.createQuery("DELETE FROM User u WHERE u.email = :email")
                    .setParameter("email", email)
                    .executeUpdate();
            entityManager.getTransaction().commit();
        }
    }

    /**
     * Legt die vorgegebene Eingabe auf System.in und faengt System.out ab.
     *
     * @return der Stream mit der Ausgabe der Applikation
     */
    private ByteArrayOutputStream prepareStreams(String input) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        System.setIn(new ByteArrayInputStream(input.getBytes()));
        System.setOut(new PrintStream(out));

        return out;
    }
}
