package ch.bzz.command;

/**
 * Beendet die Applikation.
 */
public class QuitCommand implements Command {

    @Override
    public String getName() {
        return "quit";
    }

    @Override
    public String getDescription() {
        return "Beendet das Programm";
    }

    @Override
    public void execute(AppContext context, String argument) {
        context.stop();
    }
}
