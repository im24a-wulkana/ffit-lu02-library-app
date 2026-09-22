package ch.bzz.persistence;

import ch.bzz.model.Book;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Liest und schreibt Buecher in der Datenbank.
 */
public class BookPersistor extends AbstractPersistor<Book> {

    /**
     * @return alle Buecher, nach id sortiert
     */
    public List<Book> findAll() {
        return findAll(0);
    }

    /**
     * Wie {@link #findAll()}, gibt aber hoechstens {@code limit} Buecher
     * zurueck. Ein Limit von 0 oder kleiner bedeutet "alle".
     */
    public List<Book> findAll(int limit) {
        try (EntityManager entityManager = createEntityManager()) {
            TypedQuery<Book> query =
                    entityManager.createQuery("SELECT b FROM Book b ORDER BY b.id", Book.class);

            if (limit > 0) {
                query.setMaxResults(limit);
            }

            return query.getResultList();
        }
    }

    /**
     * Speichert alle uebergebenen Buecher in einer einzigen Transaktion.
     * Bereits vorhandene Buecher werden dabei aktualisiert, damit ein Import
     * auch zum Korrigieren von Daten verwendet werden kann.
     */
    public void saveAll(List<Book> books) {
        log.debug("{} Buecher werden gespeichert", books.size());
        executeTransaction(entityManager -> books.forEach(entityManager::merge));
    }
}
