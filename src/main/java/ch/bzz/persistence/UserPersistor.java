package ch.bzz.persistence;

import ch.bzz.model.User;

/**
 * Speichert Benutzer in der Datenbank. Das Speichern selbst steckt bereits in
 * {@link AbstractPersistor}, darum ist hier nichts weiter noetig.
 */
public class UserPersistor extends AbstractPersistor<User> {
}
