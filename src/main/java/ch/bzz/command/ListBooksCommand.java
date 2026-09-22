package ch.bzz.command;

import ch.bzz.model.Book;
import ch.bzz.persistence.BookPersistor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Gibt die Buecher aus der Datenbank aus, eines pro Zeile. Optional kann die
 * Anzahl begrenzt werden: {@code listBooks 10}.
 */
public class ListBooksCommand implements Command {

    private static final Logger log = LoggerFactory.getLogger(ListBooksCommand.class);

    /** Wert, mit dem der Persistor alle Buecher zurueckgibt. */
    private static final int NO_LIMIT = 0;

    @Override
    public String getName() {
        return "listBooks";
    }

    @Override
    public String getDescription() {
        return "Listet die vorhandenen Buecher auf: listBooks [<LIMIT>]";
    }

    @Override
    public void execute(AppContext context, String argument) {
        Integer limit = parseLimit(argument);
        if (limit == null) {
            // Ungueltiges Argument, die Meldung wurde bereits ausgegeben.
            return;
        }

        try (BookPersistor persistor = new BookPersistor()) {
            List<Book> books = persistor.findAll(limit);

            if (books.isEmpty()) {
                System.out.println("Es sind keine Buecher vorhanden. Zuerst 'importBooks' ausfuehren.");
                return;
            }

            for (Book book : books) {
                System.out.println(book.getTitle());
            }

            log.debug("{} Buecher ausgegeben", books.size());
        } catch (RuntimeException e) {
            log.error("Buecher konnten nicht aus der Datenbank geladen werden", e);
            System.out.println("Die Buecher konnten nicht geladen werden: " + e.getMessage());
        }
    }

    /**
     * @return das gewuenschte Limit, {@link #NO_LIMIT} wenn kein Argument
     *         angegeben wurde, oder {@code null} bei einer ungueltigen Eingabe
     */
    private Integer parseLimit(String argument) {
        if (argument == null || argument.isBlank()) {
            return NO_LIMIT;
        }

        try {
            int limit = Integer.parseInt(argument.trim());

            if (limit < 0) {
                log.warn("Negatives Limit fuer listBooks: {}", limit);
                System.out.println("Das Limit darf nicht negativ sein: " + argument);
                return null;
            }

            return limit;
        } catch (NumberFormatException e) {
            log.warn("Ungueltiges Limit fuer listBooks: '{}'", argument);
            System.out.println("Ungueltige Zahl: '" + argument + "'. Bitte eine ganze Zahl angeben.");
            return null;
        }
    }
}
