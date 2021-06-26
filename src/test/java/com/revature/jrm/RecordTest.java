package com.revature.jrm;

import com.revature.annotations.Column;
import com.revature.annotations.Entity;
import com.revature.annotations.PrimaryKey;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.util.PSQLException;

import java.io.FileReader;
import java.sql.*;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;


@Entity(tableName = "example")
class Example {
    @PrimaryKey(columnName = "id")
    public int id;

    @Column(columnName = "foo")
    public String foo;

    @Column(columnName = "bar")
    public int bar;
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

    public static void createExampleTable() throws SQLException {
        Connection conn = ConnectionPool.getConnection();
        Statement stmt = conn.createStatement();
        stmt.execute("create table if not exists example (id serial primary key, foo varchar(64), bar integer)");
        conn.close();
    }

    @After
    public void tearDown() throws Exception {
        Connection conn = ConnectionPool.getConnection();
        Statement stmt = conn.createStatement();
        stmt.execute("drop table if exists example");
        conn.close();
    }

    private static int insertExample(String foo, int bar) throws SQLException {
        createExampleTable();
        Connection conn = ConnectionPool.getConnection();
        PreparedStatement stmt =  conn.prepareStatement("insert into example (foo, bar) values (?, ?)");
        stmt.setString(1, foo);
        stmt.setInt(2, bar);
        int id = stmt.executeUpdate();
        conn.close();
        return id;
    }

    @Test
    public void get() throws SQLException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        int id = insertExample("bar", 42);
        assertEquals(id, 1);
//        Example e = Record.get(Example.class, id);
//        assertEquals(id, e.id);
//        assertEquals("bar", e.foo);
    }

    @Test
    public void all() throws SQLException, InstantiationException, IllegalAccessException {
        int id1 = insertExample("foo", 42);
        int id2 = insertExample("bar", 42);
        List<Example> examples = Record.all(Example.class);
        assertEquals(examples.size(), 2);
    }

    @Test
    public void deleteAll() throws SQLException {
        int id1 = insertExample("foo", 42);
        int id2 = insertExample("bar", 42);
        Record.destroyAll(Example.class);

        Connection conn = ConnectionPool.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select count(*) from example");

        if (rs.next()) {
            int count = rs.getInt(1);
            assertEquals(0, count);
        }
        conn.close();
    }

    @Test
    public void createTable() throws SQLException, NoSuchFieldException, IllegalAccessException, InstantiationException {
        Record.createTable(Example.class);
        Connection conn = ConnectionPool.getConnection();
        Statement stmt = conn.createStatement();

        try {
            stmt.execute("select * from example");
        } catch (Exception e) {
            fail();
        }
    }

    @Test(expected = PSQLException.class)
    public void dropTable() throws SQLException, NoSuchFieldException, IllegalAccessException, InstantiationException {
        createExampleTable();
        Record.dropTable(Example.class);

        Connection conn = ConnectionPool.getConnection();
        Statement stmt = conn.createStatement();
        stmt.execute("select * from example");
    }

    @Test
    public void save() throws SQLException, IllegalAccessException, NoSuchFieldException, InstantiationException {
        Example ex = new Example();
        ex.foo = "bar";
        ex.bar = 2;
        Record.save(ex);

        Connection conn = ConnectionPool.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select foo, bar from example");

        if (rs.next()) {
            String foo = rs.getString(1);
            int bar = rs.getInt(2);
            assertEquals(foo, ex.foo);
            assertEquals(bar, ex.bar);
        }


        ex.foo = "zzz";
        ex.bar = 42;
        Record.save(ex);

        rs = stmt.executeQuery("select foo, bar from example");

        if (rs.next()) {
            String foo = rs.getString(1);
            int bar = rs.getInt(2);
            assertEquals(foo, ex.foo);
            assertEquals(bar, ex.bar);
        }
    }

    @Test
    public void tableExists() throws SQLException {
        assertFalse(Record.tableExists(Example.class));
        createExampleTable();
        assertTrue(Record.tableExists(Example.class));
    }

    @Test
    public void recordExists() throws SQLException, IllegalAccessException, NoSuchFieldException, InstantiationException {
        Example ex = new Example();
        ex.id = 42;
        assertFalse(Record.recordExists(ex));
        ex.id = insertExample("bar", 42);
        assertTrue(Record.recordExists(ex));
    }

    @Test
    public void destroy() throws SQLException, IllegalAccessException {
        int id = insertExample("bar", 42);
        Example ex = new Example();
        ex.id = id;
        ex.foo = "bar";
        ex.bar = 42;

        Record.destroy(ex);

        Connection conn = ConnectionPool.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select * from example where id = " + id);

        if (rs.next()) {
            fail();
        }
    }

    @Test
    public void transactionSuccess() throws SQLException, NoSuchFieldException, IllegalAccessException, InstantiationException {
        Record.beginTransaction();
        Example ex = new Example();
        ex.foo = "bar";
        ex.bar = 42;
        Record.save(ex);
        ex.bar = 39;
        Record.save(ex);
        Record.commitTransaction();

        Connection conn = ConnectionPool.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select bar from example where id = " + ex.id);

        if (rs.next()) {
            assertEquals(39, rs.getInt(1));
        }
    }

    @Test
    public void savepointSuccess() throws SQLException, NoSuchFieldException, IllegalAccessException, InstantiationException {
        Record.beginTransaction();
        Example ex = new Example();
        ex.foo = "bar";
        ex.bar = 42;
        Record.save(ex);
        Record.setSavepoint("foo");
        ex.bar = 39;
        Record.save(ex);
        Record.rollback("foo");
        Record.commitTransaction();

        Connection conn = ConnectionPool.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select bar from example where id = " + ex.id);

        if (rs.next()) {
            assertEquals(42, rs.getInt(1));
        }
    }
}