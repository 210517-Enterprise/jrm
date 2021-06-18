package com.revature.jrm;

import com.revature.annotations.Column;
import com.revature.annotations.Entity;
import com.revature.annotations.PrimaryKey;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;


@Entity(tableName = "example")
class Example extends Model {
    @PrimaryKey(columnName = "id")
    public int id;

    @Column(columnName = "foo")
    public String foo;
}

public class RecordTest {
    @BeforeClass
    public static void beforeClass() throws Exception {
        Connection conn = ConnectionPool.getConnection();
        Statement stmt = conn.createStatement();
        String sql = "drop database if exists test; create database test";
        stmt.executeUpdate(sql);

        Properties props = new Properties();
        props.load(new FileReader("src/main/resources/application.properties"));
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl(props.getProperty("url") + "test");
        ds.setUsername(props.getProperty("username"));
        ds.setPassword(props.getProperty("password"));
        ds.setMinIdle(5);
        ds.setDefaultAutoCommit(true);
        ds.setMaxIdle(10);
        ds.setMaxOpenPreparedStatements(100);

        ConnectionPool.setDataSource(ds);
    }

    @Before
    public void setUp() throws Exception {
        Connection conn = ConnectionPool.getConnection();
        Statement stmt = conn.createStatement();
        stmt.execute("create table if not exists example (id serial primary key, foo varchar(64))");
        conn.close();
    }

    @After
    public void tearDown() throws Exception {
        Connection conn = ConnectionPool.getConnection();
        Statement stmt = conn.createStatement();
        stmt.execute("drop table example");
        conn.close();
    }

    private static int insertExample(String foo) throws SQLException {
        Connection conn = ConnectionPool.getConnection();
        PreparedStatement stmt =  conn.prepareStatement("insert into example (foo) values (?)");
        stmt.setString(1, foo);
        int id = stmt.executeUpdate();
        conn.close();
        return id;
    }

    @Test
    public void get() throws SQLException, IllegalAccessException, InstantiationException {
        int id = insertExample("bar");
        assertEquals(id, 1);
        Example e = Record.get(Example.class, id);
        assertEquals(id, e.id);
        assertEquals("bar", e.foo);
    }

    @Test
    public void all() throws SQLException {
        int id1 = insertExample("foo");
        int id2 = insertExample("bar");
        List<Example> examples = Record.all(Example.class);
        assertEquals(examples.size(), 2);
    }

    @Test
    public void destroy_all() throws SQLException {
        int id1 = insertExample("foo");
        int id2 = insertExample("bar");
        Record.destroy_all(Example.class);

        Connection conn = ConnectionPool.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select count(*) from example");

        if (rs.next()) {
            int count = rs.getInt(1);
            assertEquals(0, count);
        }
        conn.close();
    }
}