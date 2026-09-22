package ch.bzz.command;

import ch.bzz.model.User;
import ch.bzz.persistence.UserPersistor;
import ch.bzz.security.PasswordHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Base64;

/**
 * Legt einen neuen Benutzer an:
 * {@code createUser <VORNAME> <NACHNAME> <GEBURTSDATUM> <EMAIL> <PASSWORT>}.
 * Das Geburtsdatum wird im Format yyyy-MM-dd erwartet. Vom Passwort werden nur
 * Hash und Salt gespeichert.
 */
public class CreateUserCommand implements Command {

    private static final Logger log = LoggerFactory.getLogger(CreateUserCommand.class);

    private static final String USAGE =
            "createUser <VORNAME> <NACHNAME> <GEBURTSDATUM yyyy-MM-dd> <EMAIL> <PASSWORT>";

    private static final int EXPECTED_ARGUMENTS = 5;

    private static final int ARGUMENT_FIRSTNAME = 0;
    private static final int ARGUMENT_LASTNAME = 1;
    private static final int ARGUMENT_DATE_OF_BIRTH = 2;
    private static final int ARGUMENT_EMAIL = 3;
    private static final int ARGUMENT_PASSWORD = 4;

    @Override
    public String getName() {
        return "createUser";
    }

    @Override
    public String getDescription() {
        return "Legt einen neuen Benutzer an: " + USAGE;
    }

    @Override
    public void execute(AppContext context, String argument) {
        String[] parts = (argument == null ? "" : argument.trim()).split("\s+");

        if (parts.length != EXPECTED_ARGUMENTS) {
            log.warn("createUser wurde mit {} statt {} Argumenten aufgerufen: '{}'",
                    parts.length, EXPECTED_ARGUMENTS, argument);
            System.out.println("Bitte alle Werte angeben: " + USAGE);
            return;
        }

        String firstname = parts[ARGUMENT_FIRSTNAME];
        String lastname = parts[ARGUMENT_LASTNAME];
        String email = parts[ARGUMENT_EMAIL];
        String password = parts[ARGUMENT_PASSWORD];

        LocalDate dateOfBirth = parseDateOfBirth(parts[ARGUMENT_DATE_OF_BIRTH]);
        if (dateOfBirth == null) {
            return;
        }

        try (UserPersistor persistor = new UserPersistor()) {
            byte[] salt = PasswordHandler.generateSalt();
            byte[] hash = PasswordHandler.hashPassword(password, salt);

            User user = new User(firstname, lastname, dateOfBirth, email,
                    Base64.getEncoder().encodeToString(hash),
                    Base64.getEncoder().encodeToString(salt));
            persistor.save(user);

            log.info("Benutzer '{}' wurde angelegt", email);
            System.out.println("Benutzer angelegt: " + firstname + " " + lastname + " <" + email + ">");
        } catch (NoSuchAlgorithmException e) {
            log.error("Das Passwort konnte nicht gehasht werden", e);
            System.out.println("Der Benutzer konnte nicht angelegt werden: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Benutzer '{}' konnte nicht gespeichert werden", email, e);
            System.out.println("Der Benutzer konnte nicht gespeichert werden: " + e.getMessage());
        }
    }

    /**
     * @return das Geburtsdatum oder {@code null}, wenn die Eingabe ungueltig ist
     */
    private LocalDate parseDateOfBirth(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            log.warn("createUser wurde mit ungueltigem Geburtsdatum aufgerufen: '{}'", value);
            System.out.println("Ungueltiges Geburtsdatum: '" + value + "'. Erwartet wird yyyy-MM-dd.");
            return null;
        }
    }
}
