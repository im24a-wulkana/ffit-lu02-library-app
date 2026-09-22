package ch.bzz.command;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Kennt alle verfuegbaren Befehle. Weil "help" die Liste aus der Registry
 * holt, muss ein neuer Befehl nur hier registriert und nicht zusaetzlich in
 * der Hilfe nachgetragen werden.
 */
public class CommandRegistry {

    /** LinkedHashMap, damit "help" die Befehle in der Reihenfolge der Registrierung ausgibt. */
    private final Map<String, Command> commands = new LinkedHashMap<>();

    public void register(Command command) {
        commands.put(command.getName(), command);
    }

    /**
     * @return der Befehl mit diesem Namen oder {@code null}, wenn es keinen gibt
     */
    public Command find(String name) {
        return commands.get(name);
    }

    public Collection<Command> getCommands() {
        return commands.values();
    }
}
