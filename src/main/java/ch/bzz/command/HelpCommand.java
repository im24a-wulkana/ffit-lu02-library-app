package ch.bzz.command;

/**
 * Gibt alle registrierten Befehle mit ihrer Beschreibung aus.
 */
public class HelpCommand implements Command {

    private final CommandRegistry registry;

    public HelpCommand(CommandRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Zeigt alle verfuegbaren Befehle an";
    }

    @Override
    public void execute(AppContext context, String argument) {
        System.out.println("Verfuegbare Befehle:");
        for (Command command : registry.getCommands()) {
            System.out.println("  " + command.getName() + " - " + command.getDescription());
        }
    }
}
