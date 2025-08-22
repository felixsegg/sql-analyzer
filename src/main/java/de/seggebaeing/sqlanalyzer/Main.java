package de.seggebaeing.sqlanalyzer;

/**
 * This class is necessary since {@link App} is extending {@link javafx.application.Application} and JARs
 * don't really like their mains extending other classes.
 */
public class Main {
    public static void main(String[] args) {
        App.main(args);
    }
}
