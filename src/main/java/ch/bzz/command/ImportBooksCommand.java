package ch.bzz.command;

import ch.bzz.io.BookTsvReader;
import ch.bzz.model.Book;
import ch.bzz.persistence.BookPersistor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

/**
 * Liest eine TSV-Datei ein und speichert die enthaltenen Buecher in der
 * Datenbank: {@code importBooks <FILE_PATH>}.
 */
public class ImportBooksCommand implements Command {

    private static final Logger log = LoggerFactory.getLogger(ImportBooksCommand.class);

    private final BookTsvReader reader;

    public ImportBooksCommand() {
        this(new BookTsvReader());
    }

    /** Erlaubt es, im Test einen anderen Reader einzusetzen. */
    public ImportBooksCommand(BookTsvReader reader) {
        this.reader = reader;
    }

    @Override
    public String getName() {
        return "importBooks";
    }

    @Override
    public String getDescription() {
        return "Importiert Buecher aus einer TSV-Datei: importBooks <FILE_PATH>";
    }

    @Override
    public void execute(AppContext context, String argument) {
        if (argument == null || argument.isBlank()) {
            log.warn("importBooks wurde ohne Dateipfad aufgerufen");
            System.out.println("Bitte einen Dateipfad angeben: importBooks <FILE_PATH>");
            return;
        }

        Path path = Path.of(argument.trim());

        try (BookPersistor persistor = new BookPersistor()) {
            List<Book> books = reader.read(path);
            persistor.saveAll(books);

            log.info("{} Buecher aus '{}' importiert", books.size(), path);
            System.out.println(books.size() + " Buecher aus " + path + " importiert.");
        } catch (NoSuchFileException e) {
            log.warn("Importdatei wurde nicht gefunden: '{}'", path);
            System.out.println("Datei nicht gefunden: " + path);
        } catch (IOException e) {
            log.error("Importdatei '{}' konnte nicht gelesen werden", path, e);
            System.out.println("Die Datei konnte nicht gelesen werden: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Importdatei '{}' hat ein ungueltiges Format", path, e);
            System.out.println("Die Datei hat ein ungueltiges Format: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Buecher aus '{}' konnten nicht gespeichert werden", path, e);
            System.out.println("Die Buecher konnten nicht gespeichert werden: " + e.getMessage());
        }
    }
}
