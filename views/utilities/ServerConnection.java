/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views.utilities;

import org.hsqldb.Server;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author daynehammes
 */
public class ServerConnection {

    private Server hsqlServer;
    private Connection connection;

    // Start HSQLDB Server
    public void startServer() {
        // Open Database
        hsqlServer = new Server();
        hsqlServer.setLogWriter(null);
        hsqlServer.setSilent(true);
        hsqlServer.setDatabaseName(0, "FadsData");
        hsqlServer.setDatabasePath(0, "file:FADSDataSettings");
        hsqlServer.start();

    }

    // Close HSQLDB Server
    public void closeServer() {

        if (hsqlServer != null) {
            hsqlServer.stop();
        }
        
    }

    // Create Connection to Database
    public Connection createConnection() throws ClassNotFoundException, SQLException {

        // Instantiate connection and return
        Class.forName("org.hsqldb.jdbcDriver");
        connection = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/FADSData", "sa", ""); // sa is default username

        return connection;
    }

    // Close Connection
    public void closeConnection() throws SQLException {

        if (connection != null) {
            connection.close();
        }
    }
}
