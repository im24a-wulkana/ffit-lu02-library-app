package ch.bzz.command;

/**
 * Haelt den Zustand der laufenden Applikation. Damit ein Befehl das Programm
 * beenden kann, ohne die Eingabeschleife selbst zu kennen, bekommt er diesen
 * Kontext uebergeben.
 */
public class AppContext {

    private boolean running = true;

    public boolean isRunning() {
        return running;
    }

    /** Sorgt dafuer, dass die Eingabeschleife nach dem aktuellen Befehl endet. */
    public void stop() {
        running = false;
    }
}
