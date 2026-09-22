package ch.bzz.io;

import ch.bzz.model.Book;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Liest Buecher aus einer TSV-Datei (UTF-8, Tabulator als Trennzeichen).
 * Die erste Zeile ist die Kopfzeile und wird uebersprungen. Danach wird pro
 * Zeile ein Buch erwartet, mit den Spalten id, isbn, title, author, year.
 */
public class BookTsvReader {

    private static final String DELIMITER = "\t";

    /** Zeichen, das am Anfang einer UTF-8-Datei stehen kann (Byte Order Mark). */
    private static final char BYTE_ORDER_MARK = '﻿';

    private static final int COLUMN_ID = 0;
    private static final int COLUMN_ISBN = 1;
    private static final int COLUMN_TITLE = 2;
    private static final int COLUMN_AUTHOR = 3;
    private static final int COLUMN_YEAR = 4;

    /**
     * @return die Buecher aus der Datei, in der Reihenfolge der Zeilen
     * @throws IOException              wenn die Datei nicht gelesen werden kann
     * @throws IllegalArgumentException wenn eine Zeile nicht dem Format entspricht
     */
    public List<Book> read(Path path) throws IOException {
        List<Book> books = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line = reader.readLine(); // Kopfzeile ueberspringen

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                books.add(parseLine(line));
            }
        }

        return books;
    }

    private Book parseLine(String line) {
        String[] fields = line.split(DELIMITER, -1);

        if (fields.length <= COLUMN_AUTHOR) {
            throw new IllegalArgumentException("Zeile hat zu wenige Spalten: " + line);
        }

        String isbn = fields[COLUMN_ISBN].trim();
        String title = fields[COLUMN_TITLE].trim();
        String author = fields[COLUMN_AUTHOR].trim();

        try {
            int id = Integer.parseInt(stripByteOrderMark(fields[COLUMN_ID].trim()));
            int year = parseYear(fields);
            return new Book(id, isbn, title, author, year);
        } catch (NumberFormatException e) {
            // Als IllegalArgumentException weitergeben, damit der aufrufende
            // Befehl alle Formatfehler an einer Stelle behandeln kann.
            throw new IllegalArgumentException("Zeile enthaelt eine ungueltige Zahl: " + line, e);
        }
    }

    private int parseYear(String[] fields) {
        if (fields.length <= COLUMN_YEAR || fields[COLUMN_YEAR].isBlank()) {
            return 0;
        }
        return Integer.parseInt(fields[COLUMN_YEAR].trim());
    }

    private String stripByteOrderMark(String value) {
        if (!value.isEmpty() && value.charAt(0) == BYTE_ORDER_MARK) {
            return value.substring(1);
        }
        return value;
    }
}
