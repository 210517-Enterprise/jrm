package com.revature.jrm;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ConnectionPool {
    private static final ConnectionPool connection_factory = new ConnectionPool();
    private BasicDataSource ds;
    
    private static final Logger log = LoggerFactory.getLogger(ConnectionPool.class);


    static {
        try {
            Class.forName("org.postgresql.Driver");
        }catch (ClassNotFoundException cnfe) {
            log.error("Failed to establish a connection with the Database");
        }
    }

    /**
     * private constructor for Utils.ConnectionFactory class.
     */
    ConnectionPool() {
        try {
            Properties props = new Properties();
            props.load(new FileReader("src/main/resources/application.properties"));
            ds = new BasicDataSource();
            ds.setUrl(props.getProperty("url"));
            ds.setUsername(props.getProperty("username"));
            ds.setPassword(props.getProperty("password"));
            ds.setMinIdle(5);
            ds.setDefaultAutoCommit(false);
            ds.setMaxIdle(10);
            ds.setMaxOpenPreparedStatements(100);
            log.info("Database connection extablished!");
        }catch(IOException ioe) {
            System.out.println("sorry, no application properties file found.");
            log.error("Failed to establish a connection with the Database");
            //A place to log errors
        }
    }

    /**
     * Method to retrieve current static instance of Utils.ConnectionFactory class.
     * @return current instance of Utils.ConnectionFactory object.
     */
    public static ConnectionPool getInstance() {
        return connection_factory;
    }

    /**
     * Method to retrieve a connection to application database.
     * @return Connection object.
     */
    public Connection getConnection () {
        try {
            return ds.getConnection();
        }catch (SQLException sqle) {
        	log.error("Failed to establish a connection with the Database");
        }
        return null;
    }
}
