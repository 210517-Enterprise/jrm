package com.revature.jrm;

import com.revature.annotations.Column;
import com.revature.annotations.Entity;
import com.revature.annotations.PrimaryKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class Record {
    /**
     * Returns an object from the specified class using results from ResultSet
     *
     * @param type the class type of the object being created
     * @param rs the current ResultSet with values after using .next()
     * @return the newly created object of the specified type
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws SQLException
     */
    private static <T> T objFromResultSet(Class<T> type, ResultSet rs) throws InstantiationException, IllegalAccessException, SQLException {
        T obj = type.newInstance();
        for (Field field : type.getDeclaredFields()) {
            for (Annotation a : field.getDeclaredAnnotations()) {
                if (a.annotationType() == Column.class) {
                    Column col = (Column) a;
                    field.set(obj, rs.getObject(col.columnName()));
                } else if (a.annotationType() == PrimaryKey.class) {
                    PrimaryKey pk = (PrimaryKey) a;
                    field.set(obj, rs.getObject(pk.columnName()));
                }
            }
        }

        return obj;
    }
    
    /**
     * Returns boolean value for whether the query to create the table has ran
     *
     * @return boolean whether query was successful
     */
    public static <T> boolean createTable(Class<T> type) throws NoSuchFieldException, IllegalAccessException, InstantiationException, SQLException {
        Entity entity = type.getDeclaredAnnotation(Entity.class);
        Connection conn = ConnectionPool.getConnection();
        PreparedStatement stmt = conn.prepareStatement("drop table if exists" + entity.tableName() + " cascade; "
        		+ "create table " + entity.tableName() + ";");
  
        if (stmt.executeUpdate() != 0) 
			return true;
		else
			return false;

        
    }
    
    /**
     * Returns boolean value for whether the query to drop the table has ran
     *
     * @return boolean whether query was successful
     */
    public static <T> boolean dropTable(Class<T> type) throws NoSuchFieldException, IllegalAccessException, InstantiationException, SQLException {
        Entity entity = type.getDeclaredAnnotation(Entity.class);
        Connection conn = ConnectionPool.getConnection();
        PreparedStatement stmt = conn.prepareStatement("drop table if exists" + entity.tableName() + " cascade; ");
  
        if (stmt.executeUpdate() != 0) 
			return true;
		else
			return false;

        
    }

    /**
     * Returns an object from the database with the given id
     *
     * @param id the id of the row entry
     * @return the requested entry
     */
    public static <T> T get(Class<T> type, int id) throws NoSuchFieldException, IllegalAccessException, InstantiationException, SQLException {
        Entity entity = type.getDeclaredAnnotation(Entity.class);
        PrimaryKey primarykey = type.getDeclaredAnnotation(PrimaryKey.class);
        Connection conn = ConnectionPool.getConnection();
        PreparedStatement stmt = conn.prepareStatement("select * from " + entity.tableName() + " where " + primarykey.columnName() + " = ?");
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return objFromResultSet(type, rs);
        } else {
            return null;
        }
    }

    /**
     * Returns a list of all objects of the model's type
     *
     * @return the list of all objects
     */
    public static <T> List<T> all(Class<T> type) {
        // 1. Use reflection API to get the table name from annotations
        // 2. Get connection from connection pool
        // 3. Query for all entities
        // 4. Create objects and return the list
        return null;
    }

    /**
     * Returns a list of objects that match the given "WHERE" clause
     *
     * @param query the value to put in the where clause
     * @return list of objects that match
     */
    public static <T> List<T> where(String query) {
        // 1. Use reflection API to get the table name from annotations
        // 2. Get connection from connection pool
        // 3. Build a select all query with query string parameter e.g. "select * from users where ?"
        // 4. Create objects and return the list
        return null;
    }

    /**
     * Deletes all entities of the model's type
     */
    public static void destroy_all() {
        // 1. Use reflection API to get the table name from annotations
        // 2. Get connection from connection pool
        // 3. Delete all objects e.g. "delete from users"
    }
}
