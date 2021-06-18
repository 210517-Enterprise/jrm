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
    private static BasicDataSource ds;

    private static final Logger log = LoggerFactory.getLogger(ConnectionPool.class);

    static {
        try {
            Properties props = new Properties();
            props.load(new FileReader("src/main/resources/application.properties"));
            ds = new BasicDataSource();
            ds.setUrl(props.getProperty("url"));
            ds.setUsername(props.getProperty("username"));
            ds.setPassword(props.getProperty("password"));
            ds.setMinIdle(5);
            ds.setDefaultAutoCommit(true);
            ds.setMaxIdle(10);
            ds.setMaxOpenPreparedStatements(100);
            log.info("Database connection established!");
        } catch (IOException e) {
            System.out.println("sorry, no application properties file found.");
            log.error("Failed to establish a connection with the Database: " + e);
            //A place to log errors
        }
    }

    /**
     * Replaces datasource for connection pool
     *
     * @param bds the new datasource
     */
    public static void setDataSource(BasicDataSource bds) {
        ds = bds;
    }

    /**
     * Method to retrieve a connection to application database.
     *
     * @return Connection object.
     */
    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
