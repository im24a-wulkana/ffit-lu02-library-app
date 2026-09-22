package ch.bzz.persistence;

import ch.bzz.config.Config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Gemeinsame Basis aller Persistor-Klassen. Hier stehen die Dinge, die jeder
 * Persistor braucht: die EntityManagerFactory und das Transaktionshandling.
 * So muss das nicht in jeder Unterklasse wiederholt werden.
 * <p>
 * Ein Persistor haelt eine Verbindung offen und wird darum am besten in einem
 * try-with-resources verwendet.
 *
 * @param <T> Typ der Entitaet, die dieser Persistor speichert
 */
public abstract class AbstractPersistor<T> implements AutoCloseable {

    /** Logger der jeweiligen Unterklasse, damit die Log-Eintraege zuordenbar sind. */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final EntityManagerFactory factory;

    protected AbstractPersistor() {
        this.factory = Persistence.createEntityManagerFactory("localPU", Config.getProperties());
    }

    /**
     * Speichert die Entitaet. Ein bereits vorhandener Datensatz mit derselben
     * id wird dabei aktualisiert.
     */
    public void save(T entity) {
        executeTransaction(entityManager -> entityManager.merge(entity));
    }

    /**
     * Fuehrt die uebergebene Aktion in einer Transaktion aus und macht sie bei
     * einem Fehler wieder rueckgaengig.
     */
    protected void executeTransaction(Consumer<EntityManager> action) {
        try (EntityManager entityManager = factory.createEntityManager()) {
            try {
                entityManager.getTransaction().begin();
                action.accept(entityManager);
                entityManager.getTransaction().commit();
            } catch (RuntimeException e) {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                    log.warn("Transaktion wurde zurueckgerollt");
                }
                log.error("Fehler waehrend der Transaktion", e);
                throw e;
            }
        }
    }

    /**
     * Erzeugt einen EntityManager fuer lesende Zugriffe. Der Aufrufer ist
     * dafuer verantwortlich, ihn wieder zu schliessen.
     */
    protected EntityManager createEntityManager() {
        return factory.createEntityManager();
    }

    @Override
    public void close() {
        if (factory.isOpen()) {
            factory.close();
            log.debug("EntityManagerFactory geschlossen");
        }
    }
}
