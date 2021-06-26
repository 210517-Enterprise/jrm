package com.revature.jrm;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revature.annotations.Column;
import com.revature.annotations.Entity;
import com.revature.annotations.PrimaryKey;
import com.revature.exceptions.MultiplePrimaryKeyException;

public class Record {
	
	private static final Logger log = LoggerFactory.getLogger(Record.class);
	
	private static HashMap<String, Object> cache = new HashMap<String, Object>();
	private static HashMap<String, List<Object>> cache_list = new HashMap<String, List<Object>>();
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
    	String key = type + "," + id;
    	if(cache.containsKey(key)) {
    		System.out.println("id = "+ id + " exist in cache");
    		return  (T) cache.get(key);
    	}
  
        Entity entity = type.getDeclaredAnnotation(Entity.class);
        String id_column = "";
        for (Field field : type.getDeclaredFields()) {
            for (Annotation a : field.getDeclaredAnnotations()) {
               if (a.annotationType() == PrimaryKey.class) {
                    field.setAccessible(true);
                    PrimaryKey pk = (PrimaryKey) a;
                    id_column += pk.columnName();
                }
            }
        }
        Connection conn = ConnectionPool.getConnection();
        PreparedStatement stmt = conn.prepareStatement("select * from " + entity.tableName() + " where " + id_column + " = ?");
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
        	log.info("Retrieved entry from database");
        	T value = objFromResultSet(type, rs);
        	cache.put(key,value );
            return objFromResultSet(type, rs);
        } else {
        	log.info("Failed to retrieve entry from database");
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
    	
    	String key = type + "," + "00000";
    	if(cache_list.containsKey(key)) {
    		System.out.println("All the results from previous query for ("+ type.toString() + ") exist in cache");
    		return   (List<T>) cache_list.get(key);
    	}
    	log.info("Running query to return all entries");
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
		
    	cache_list.put(key,  (List<Object>) results);
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
    	String key = type + "," + column_name + "," + requirement;
    	if(cache_list.containsKey(key)) {
    		System.out.println("All the results from previous query for ("+ column_name +") where the entry is ("+ requirement +") exist in cache");
    		return  (List<T>) cache_list.get(key);
    	}
    	log.info("Running search to find entries");
        // 1. Use reflection API to get the table name from annotations
    	Entity entity = type.getDeclaredAnnotation(Entity.class);
        // 2. Get connection from connection pool
    	Connection conn = ConnectionPool.getConnection();
        // 3. Build a select all query with query string parameter e.g. "select * from users where ?"
    	PreparedStatement stmt = conn.prepareStatement("select * from " + entity.tableName() + " where " +column_name+ " = ?");
    	
    	//stmt.setString(1, column_name);
    	stmt.setString(1, requirement);
    	
        // 4. Create objects and return the list
		ResultSet rs = stmt.executeQuery();			// Queries the database
		
		List<T> results = new ArrayList<>();
		// So long as the ResultSet actually contains results...
		while (rs.next()) {
			
			T t = objFromResultSet(type, rs);
			
			results.add(t);
			
		}
		cache_list.put(key,(List<Object>) results);
        return results;
    }
    
    public static <T> List<T> where(Class<T> type, String column_name, int requirement) throws SQLException, InstantiationException, IllegalAccessException {
    	String key = type + "," + column_name + "," + requirement;
    	if(cache_list.containsKey(key)) {
    		System.out.println("All the results from previous query for ("+ column_name +") where the entry is ("+ requirement +") exist in cache");
    		return  (List<T>) cache_list.get(key);
    	}
    	log.info("Running search to find entries");
        // 1. Use reflection API to get the table name from annotations
    	Entity entity = type.getDeclaredAnnotation(Entity.class);
        // 2. Get connection from connection pool
    	Connection conn = ConnectionPool.getConnection();
        // 3. Build a select all query with query string parameter e.g. "select * from users where ?"
    	PreparedStatement stmt = conn.prepareStatement("select * from " + entity.tableName() + " where " +column_name+" = ?");
    	
    	stmt.setInt(1, requirement);
    	
        // 4. Create objects and return the list
		ResultSet rs = stmt.executeQuery();			// Queries the database
		
		List<T> results = new ArrayList<>();
		// So long as the ResultSet actually contains results...
		while (rs.next()) {
			
			T t = objFromResultSet(type, rs);
			
			results.add(t);
			
		}
		cache_list.put(key,(List<Object>) results);
        return results;
    }

    /**
     * Deletes all entities of the model's type
     *
     * @param type the model class to delete
     */
    public static void deleteAll(Class type) throws SQLException {
        // 1. Use reflection API to get the table name from annotations
        // 2. Get connection from connection pool
        // 3. Delete all objects e.g. "delete from users"
    	Entity entity = (Entity) type.getDeclaredAnnotation(Entity.class);
        Connection conn = ConnectionPool.getConnection();
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM " + entity.tableName());
        stmt.execute();
    }

    
    public static <T> void createTable(Class<T> type) throws NoSuchFieldException, IllegalAccessException, InstantiationException, SQLException {
    	log.info("Running query to create table");
    	
        Entity entity = type.getDeclaredAnnotation(Entity.class);
    
        int number_of_primarykeys = 0, counter=0;
        String columns = "";
        for (Field field : type.getDeclaredFields()) {
        	System.out.println(field);
        	if(field.getType() == int.class) {
	            for (Annotation a : field.getDeclaredAnnotations()) {
	                if (a.annotationType() == PrimaryKey.class) {
	                	if(number_of_primarykeys == 1) {
	                		throw new MultiplePrimaryKeyException("Only one field can be annotated with @PrimaryKey.");
	                	}
	                	PrimaryKey col = (PrimaryKey) a;
	                    field.setAccessible(true);
	                    columns += col.columnName() + " serial primary key, ";
	                    number_of_primarykeys++;
	                    counter++;
	                    System.out.println("Proccessing to pk integer class " + counter );
	                }else if(a.annotationType() == Column.class) {
	                    Column col = (Column) a;
	                    field.setAccessible(true);
	                    columns += col.columnName() + " Integer not null, ";
	                    counter++;
	                    System.out.println("Proccessing a regular integer class " + counter);
	                }  
	            }
        	}else if(field.getType() == String.class) {
	            for (Annotation a : field.getDeclaredAnnotations()) {
	                if (a.annotationType() == Column.class) {
	                    Column col = (Column) a;
	                    field.setAccessible(true);
	                    columns += col.columnName() + " varchar(30) not null, ";
	                    counter++;
	                    System.out.println("Proccessing a field of String class " + counter);
	                }
	            }
        	}
        }
        
        String columns_altered = "";
        for(int i=0; i<columns.length()-2;i++) {
        	columns_altered += columns.charAt(i); 
        }
        
        Connection conn = ConnectionPool.getConnection();
        PreparedStatement stmt = conn.prepareStatement("drop table if exists " + entity.tableName() + " cascade; "
        		+ "create table " + entity.tableName() + "( "+ columns_altered 
        		+ " );");

        stmt.execute();


    }

    public static <T> void dropTable(Class<T> type) throws NoSuchFieldException, IllegalAccessException, InstantiationException, SQLException {
    	log.info("Running query to drop table");
    	Entity entity = type.getDeclaredAnnotation(Entity.class);
        Connection conn = ConnectionPool.getConnection();
        PreparedStatement stmt = conn.prepareStatement("drop table if exists " + entity.tableName() + " cascade; ");
        stmt.execute();

    }

    public static <T> boolean tableExists(Class<T> type) throws SQLException {
        Entity entity = type.getDeclaredAnnotation(Entity.class);

        String query = "select exists ( " +
                "select from information_schema.tables " +
                "where table_name = '" + entity.tableName() + "')";

        Connection conn = ConnectionPool.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        boolean exists = rs.next() && rs.getBoolean(1);
        conn.close();
        return exists;
    }

    public static <T> boolean recordExists(T obj) throws SQLException, IllegalAccessException, NoSuchFieldException, InstantiationException {
        Class<?> type = obj.getClass();
        Entity entity = type.getDeclaredAnnotation(Entity.class);

        if (!tableExists(type)) {
            createTable(type);
            return false;
        }

        // Get the column name for the entity's primary key
        String primaryKey = "";
        int id = -1;
        for (Field field : type.getDeclaredFields()) {
            for (Annotation a : field.getDeclaredAnnotations()) {
                if (a.annotationType() == PrimaryKey.class) {
                    PrimaryKey pk = (PrimaryKey) a;
                    primaryKey = pk.columnName();
                    id = field.getInt(obj);
                    break;
                }
            }
        }

        String query = "select exists ( " +
                "select from " + entity.tableName() + " " +
                "where " + primaryKey + "=" + id + ")";

        Connection conn = ConnectionPool.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        boolean exists = rs.next() && rs.getBoolean(1);
        conn.close();
        return exists;
    }

    public static <T> void save(T obj) throws SQLException, IllegalAccessException, NoSuchFieldException, InstantiationException {
    	log.info("Running query to insert an entry");

    	// Create database table for entity if it doesn't exist
        Class<?> type = obj.getClass();

        if (!tableExists(type)) {
            createTable(type);
        }

        List<String> columnNames = new ArrayList<>();
        List<Object> columnValues = new ArrayList<>();

        // Build string of column names and placeholders for values
        for (Field field : type.getDeclaredFields()) {
            for (Annotation a : field.getDeclaredAnnotations()) {
                if (a.annotationType() == Column.class) {
                    Column col = (Column) a;
                    columnNames.add(col.columnName());
                    columnValues.add(field.get(obj));
                }
            }
        }

        // Get primary key column name
        Field primaryKeyField = null;
        String primaryKey = "";
        int id = -1;
        for (Field field : type.getDeclaredFields()) {
            for (Annotation a : field.getDeclaredAnnotations()) {
                if (a.annotationType() == PrimaryKey.class) {
                    PrimaryKey pk = (PrimaryKey) a;
                    primaryKey = pk.columnName();
                    id = field.getInt(obj);
                    primaryKeyField = field;
                    break;
                }
            }
        }

        // Build the query string and PreparedStatement
        Entity entity = type.getDeclaredAnnotation(Entity.class);
        StringBuilder query = new StringBuilder();
        if (recordExists(obj)) {
            query.append(String.format("update %s set ", entity.tableName()));

            for (int i = 0; i < columnNames.size(); i++) {
                query.append(String.format("%s = ?", columnNames.get(i)));
                if (i < columnNames.size() - 1) {
                    query.append(", ");
                }
            }

            query.append(String.format(" where %s = %s returning %s", primaryKey, id, primaryKey));
        } else {
            String columns = String.join(",", columnNames);
            String parameters = String.join(",", columnNames.stream().map((s) -> "?").collect(Collectors.toList()));
            query.append(String.format("insert into %s (%s) values (%s) returning %s", entity.tableName(), columns, parameters, primaryKey));
        }

        Connection conn = ConnectionPool.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query.toString());

        // Set column values in PreparedStatement
        for (int i = 1; i <= columnValues.size(); i++) {
            stmt.setObject(i, columnValues.get(i - 1));
        }

        // Execute query
        ResultSet rs = stmt.executeQuery();

        // Set value of primary key on object
        if (rs.next() && primaryKeyField != null) {
            primaryKeyField.set(obj, rs.getInt(1));
        }

        conn.close();
    }
}
