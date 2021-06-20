package com.revature.jrm;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.revature.annotations.Column;
import com.revature.annotations.Entity;
import com.revature.annotations.PrimaryKey;

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
                    field.setAccessible(true);
                    field.set(obj, rs.getObject(col.columnName()));
                } else if (a.annotationType() == PrimaryKey.class) {
                    field.setAccessible(true);
                    PrimaryKey pk = (PrimaryKey) a;
                    field.set(obj, rs.getObject(pk.columnName()));
                }
            }
        }

        return obj;
    }

    /**
     * Returns an object from the database with the given id
     *
     * @param id the id of the entry
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
     * @throws SQLException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public static <T> List<T> all(Class<T> type) throws SQLException, InstantiationException, IllegalAccessException {
        // 1. Use reflection API to get the table name from annotations
    	Entity entity = type.getDeclaredAnnotation(Entity.class);
        // 2. Get connection from connection pool
    	Connection conn = ConnectionPool.getConnection();
        // 3. Query for all entities
    	PreparedStatement stmt = conn.prepareStatement("select * from " + entity.tableName() + " ; ");
    	
    	ResultSet rs = stmt.executeQuery();			// Queries the database
    	
        // 4. Create objects and return the list
    	List<T> results = new ArrayList<>();
		// So long as the ResultSet actually contains results...
		while (rs.next()) {
			
			T t = objFromResultSet(type, rs);
			
			results.add(t);
			
		}
        return results;
    }

    /**
     * Returns a list of objects that match the given "WHERE" clause
     *
     * @param query the value to put in the where clause
     * @return list of objects that match
     * @throws SQLException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public static <T> List<T> where(Class<T> type, String column_name, String requirement) throws SQLException, InstantiationException, IllegalAccessException {
        // 1. Use reflection API to get the table name from annotations
    	Entity entity = type.getDeclaredAnnotation(Entity.class);
        // 2. Get connection from connection pool
    	Connection conn = ConnectionPool.getConnection();
        // 3. Build a select all query with query string parameter e.g. "select * from users where ?"
    	PreparedStatement stmt = conn.prepareStatement("select * from " + entity.tableName() + " where  ? = ?");
    	
    	stmt.setString(1, column_name);
    	stmt.setString(2, requirement);
    	
        // 4. Create objects and return the list
		ResultSet rs = stmt.executeQuery();			// Queries the database
		
		List<T> results = new ArrayList<>();
		// So long as the ResultSet actually contains results...
		while (rs.next()) {
			
			T t = objFromResultSet(type, rs);
			
			results.add(t);
			
		}
        return results;
    }

    /**
     * Deletes all entities of the model's type
     */
    public static void destroy_all() {
        // 1. Use reflection API to get the table name from annotations
        // 2. Get connection from connection pool
        // 3. Delete all objects e.g. "delete from users"
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
     * Returns boolean value for whether the query to alter the table has ran
     *
     * @return boolean whether query was successful
     */
    public static <T> boolean alterTable(Class<T> type) throws NoSuchFieldException, IllegalAccessException, InstantiationException, SQLException {
        Entity entity = type.getDeclaredAnnotation(Entity.class);
        PrimaryKey primarykey = type.getDeclaredAnnotation(PrimaryKey.class);
        
        String columns = "";
        for (Field field : type.getDeclaredFields()) {
        	if(field.getType() == Integer.class) {
	            for (Annotation a : field.getDeclaredAnnotations()) {
	                if (a.annotationType() == Column.class) {
	                    Column col = (Column) a;
	                    field.setAccessible(true);
	                    columns += col.columnName() + " Integer not null, ";
	                }
	            }
        	}else if(field.getType() == String.class) {
	            for (Annotation a : field.getDeclaredAnnotations()) {
	                if (a.annotationType() == Column.class) {
	                    Column col = (Column) a;
	                    field.setAccessible(true);
	                    columns += col.columnName() + " varchar(30) not null, ";
	                }
	            }
        	}
        }
        
        String columns_altered = "";
        for(int i=0; i<columns.length()-2;i++) {
        	columns_altered += columns.charAt(i); 
        }
        
        
        
        Connection conn = ConnectionPool.getConnection();
        PreparedStatement stmt = conn.prepareStatement("alter table" + entity.tableName() + "( " + primarykey.columnName() + 
        		" serial primary key, "+ columns_altered 
        		+ " );");

        if (stmt.executeUpdate() != 0) 
			return true;
		else
			return false;


    }
}
