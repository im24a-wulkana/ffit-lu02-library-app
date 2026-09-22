package ch.bzz;

import ch.bzz.command.AppContext;
import ch.bzz.command.Command;
import ch.bzz.command.CommandRegistry;
import ch.bzz.command.CreateUserCommand;
import ch.bzz.command.HelpCommand;
import ch.bzz.command.ImportBooksCommand;
import ch.bzz.command.ListBooksCommand;
import ch.bzz.command.QuitCommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * Einstiegspunkt der Bibliotheksapplikation. Die Klasse liest Befehle von der
 * Konsole und delegiert sie an die passende {@link Command}-Implementierung.
 * Was ein einzelner Befehl tut, steht darum nicht hier, sondern im jeweiligen
 * Befehl selbst.
 */
public class LibraryAppMain {

    private static final Logger log = LoggerFactory.getLogger(LibraryAppMain.class);

    /** Trennt den Befehlsnamen vom Rest der Eingabe. */
    private static final String INPUT_SEPARATOR = "\s+";

    public static void main(String[] args) {
        CommandRegistry registry = createRegistry();
        AppContext context = new AppContext();

        log.info("Applikation gestartet");
        Scanner scanner = new Scanner(System.in);

        while (context.isRunning() && scanner.hasNextLine()) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            String[] parts = input.split(INPUT_SEPARATOR, 2);
            String name = parts[0];
            String argument = parts.length > 1 ? parts[1].trim() : "";

            log.debug("Befehl '{}' mit Argument '{}' erhalten", name, argument);

            Command command = registry.find(name);
            if (command == null) {
                log.warn("Unbekannter Befehl eingegeben: '{}'", name);
                System.out.println("Die Eingabe wurde nicht als Befehl erkannt: " + input);
            } else {
                command.execute(context, argument);
            }
        }

        scanner.close();
        log.info("Applikation beendet");
    }

    /**
     * Registriert alle Befehle. Ein neuer Befehl wird hier ergaenzt und
     * erscheint danach automatisch auch in der Ausgabe von "help".
     */
    private static CommandRegistry createRegistry() {
        CommandRegistry registry = new CommandRegistry();

        registry.register(new HelpCommand(registry));
        registry.register(new ListBooksCommand());
        registry.register(new ImportBooksCommand());
        registry.register(new CreateUserCommand());
        registry.register(new QuitCommand());

        return registry;
    }
}
