package ch.bzz.command;

/**
 * Ein Befehl, der ueber die Konsole aufgerufen werden kann. Ein neuer Befehl
 * implementiert dieses Interface und wird in der {@link CommandRegistry}
 * registriert; an der Eingabeschleife selbst muss dann nichts geaendert werden.
 */
public interface Command {

    /** Der Name, mit dem der Befehl aufgerufen wird. */
    String getName();

    /** Kurze Beschreibung fuer die Ausgabe von "help". */
    String getDescription();

    /**
     * Fuehrt den Befehl aus.
     *
     * @param context  Zustand der Applikation
     * @param argument alles, was hinter dem Befehlsnamen eingegeben wurde,
     *                 oder ein leerer String, wenn nichts angegeben wurde
     */
    void execute(AppContext context, String argument);
}
