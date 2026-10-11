package be.recettopia.connection;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Returns a NEW Oracle connection on each call (close it with try-with-resources).
 * No shared static connection: each HTTP request gets its own transaction.
 * Credentials are read from db.properties (not committed, see db.example.properties).
 */
public final class DatabaseConnection {

    /** Name of the configuration file */
    private static final String CONFIG_FILE = "db.properties";
    /** name of the Oracle JDBC driver class */
    private static final String ORACLE_DRIVER = "oracle.jdbc.OracleDriver";
    /** Configuration loaded once, then kept in memory (null = not loaded yet). */
    private static Properties config;

    /** Private constructor because this class only has static methods, it is never instantiated. */
    private DatabaseConnection() {
    }

    /** Opens a new connection to the Oracle database and return an open connection with auto-commit disabled so the caller must close it
     * and throws SQLException if the database cannot be reached or the credentials are wrong
     */
    public static Connection getConnection() throws SQLException {
        // Read the configuration, only the first time, then reuse it
        Properties properties = loadConfig();

        // Ask the JDBC driver for a connection with the URL user and password
        Connection connection = DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.user"),
                properties.getProperty("db.password"));

        // Nothing is saved automatically, the caller must call commit or rollback
        connection.setAutoCommit(false);
        return connection;
    }

    /** Reads db.properties once and loads the Oracle driver
     * synchronized : prevents two requests from loading it at the same time.
     * thatreturn the configuration (db.url, db.user, db.password)
     */
    private static synchronized Properties loadConfig() {
        // Already loaded: reuse it
        if (config != null) {
            return config;
        }
        // Open db.properties from the classpath
        try (InputStream in = DatabaseConnection.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                throw new IllegalStateException(CONFIG_FILE + " not found: copy db.example.properties "
                        + "to db.properties and fill in your credentials.");
            }

            // Read the "key=value" lines of the file
            Properties properties = new Properties();
            properties.load(in);
            for (String key : new String[] {"db.url", "db.user", "db.password"}) {
                if (properties.getProperty(key, "").isBlank()) {
                    throw new IllegalStateException(CONFIG_FILE + " is incomplete: " + key + " is missing.");
                }
            }
            // Tomcat does not always auto-register JDBC drivers of a web app: load it explicitly
            Class.forName("oracle.jdbc.OracleDriver");

            // Keep the configuration in memory for the next calls
            config = properties;
            return config;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read " + CONFIG_FILE + ".", e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Oracle JDBC driver (ojdbc11) not found in the application.", e);
        }
    }
}