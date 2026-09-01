package ch.bzz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

public class LibraryAppMain {

    private static final Logger log = LoggerFactory.getLogger(LibraryAppMain.class);

    private static final String IMPORT_FILE = "books.csv";

    private static final List<Book> books = new ArrayList<>();

    public static void main(String[] args) {
        log.info("Applikation gestartet");

        Map<String, Consumer<String>> commands = new LinkedHashMap<>();
        commands.put("help", argument -> System.out.println(String.join(", ", commands.keySet())));
        commands.put("importBooks", argument -> importBooks());
        commands.put("listBooks", LibraryAppMain::listBooks);
        commands.put("quit", argument -> { });

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine().trim();

                if ("quit".equals(input)) {
                    break;
                }

                String command = input;
                String argument = "";
                int separator = input.indexOf(' ');
                if (separator > -1) {
                    command = input.substring(0, separator);
                    argument = input.substring(separator + 1).trim();
                }

                log.debug("Befehl '{}' mit Argument '{}' erhalten", command, argument);

                Consumer<String> action = commands.get(command);
                if (action != null) {
                    action.accept(argument);
                } else {
                    log.warn("Unbekannter Befehl eingegeben: '{}'", command);
                    System.out.println("Befehl nicht erkannt: " + command);
                }
            }
        }

        log.info("Applikation beendet");
    }

   
    private static void importBooks() {
        try (InputStream input = LibraryAppMain.class.getClassLoader().getResourceAsStream(IMPORT_FILE)) {
            if (input == null) {
                log.warn("Importdatei '{}' wurde nicht gefunden, es werden keine Buecher importiert", IMPORT_FILE);
                System.out.println("Importdatei nicht gefunden: " + IMPORT_FILE);
                return;
            }

            List<Book> imported = readBooks(input);
            books.clear();
            books.addAll(imported);

            log.info("{} Buecher aus '{}' importiert", imported.size(), IMPORT_FILE);
            System.out.println(imported.size() + " Buecher importiert.");
        } catch (IOException e) {
            log.error("Fehler beim Lesen der Importdatei '{}'", IMPORT_FILE, e);
            System.out.println("Import fehlgeschlagen: " + IMPORT_FILE);
        }
    }

    private static List<Book> readBooks(InputStream input) throws IOException {
        List<Book> imported = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            boolean header = true;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (header) {
                    header = false;
                    continue;
                }
                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split(";");
                if (parts.length < 3) {
                    log.warn("Zeile {} hat zu wenige Felder und wird uebersprungen: '{}'", lineNumber, line);
                    continue;
                }

                try {
                    imported.add(new Book(parts[0].trim(), parts[1].trim(), Integer.parseInt(parts[2].trim())));
                } catch (NumberFormatException e) {
                    log.warn("Zeile {} enthaelt kein gueltiges Jahr und wird uebersprungen: '{}'", lineNumber, line);
                }
            }
        }

        return imported;
    }

    
    private static void listBooks(String argument) {
        int limit = books.size();

        if (!argument.isEmpty()) {
            try {
                limit = Integer.parseInt(argument);

                if (limit < 0) {
                    log.warn("Negatives Limit '{}' angegeben, es werden alle Buecher ausgegeben", argument);
                    limit = books.size();
                } else {
                    limit = Math.min(limit, books.size());
                }
            } catch (NumberFormatException e) {
                log.warn("Ungueltiges Limit '{}' angegeben, es werden alle Buecher ausgegeben", argument, e);
                System.out.println("Ungueltiges Limit: " + argument + " - es werden alle Buecher ausgegeben.");
            }
        }

        if (books.isEmpty()) {
            log.info("listBooks aufgerufen, es sind keine Buecher geladen");
            System.out.println("Keine Buecher vorhanden. Zuerst 'importBooks' ausfuehren.");
            return;
        }

        for (Book book : books.subList(0, limit)) {
            System.out.println(book);
        }

        log.debug("{} von {} Buechern ausgegeben", limit, books.size());
    }
}
