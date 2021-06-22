package com.revature.jrm;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revature.annotations.Column;
import com.revature.annotations.Entity;
import com.revature.annotations.PrimaryKey;
import com.revature.exceptions.MultiplePrimaryKeyException;

public class Record {
	
	private static final Logger log = LoggerFactory.getLogger(Record.class);
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
    	log.info("Running search to find entries");
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
    public static void destroy_all(Class type) throws SQLException {
        // 1. Use reflection API to get the table name from annotations
        // 2. Get connection from connection pool
        // 3. Delete all objects e.g. "delete from users"
    	Entity entity = (Entity) type.getDeclaredAnnotation(Entity.class);
        Connection conn = ConnectionPool.getConnection();
        PreparedStatement stmt = conn.prepareStatement("DELETE * FROM " + entity.tableName());
        ResultSet rs = stmt.executeQuery();
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
    
    public static <T> void insert(Class<T> type, List<Object> columns_generic) throws SQLException {
    	log.info("Running query to insert an entry");
    	Entity entity = type.getDeclaredAnnotation(Entity.class);
    	Connection conn = ConnectionPool.getConnection();
    	
    	int counter=0;
    	String columns = "";
        for (Field field : type.getDeclaredFields()) {
        	System.out.println(field);
        	if(field.getType() == int.class) {
	            for (Annotation a : field.getDeclaredAnnotations()) {
	                	 if(a.annotationType() == Column.class) {
	                    Column col = (Column) a;
	                    field.setAccessible(true);
	                    columns += col.columnName() + ", ";
	                    counter++;
	                    System.out.println("Proccessing a regular integer class " + counter);
	                }
	            }
        	}else if(field.getType() == String.class) {
	            for (Annotation a : field.getDeclaredAnnotations()) {
	                if (a.annotationType() == Column.class) {
	                    Column col = (Column) a;
	                    field.setAccessible(true);
	                    columns += col.columnName() + ", ";
	                    counter++;
	                    System.out.println("Proccessing a field of String class " + counter);
	                }
	            }
        	}
        }
    	
        String altered_columns = "";
        for(int i=0;i< columns.length()-2;i++) {
        	altered_columns += columns.charAt(i);
        }
        
        String values = " ? ";
        for(int i=0; i<counter-1;i++) {
        	values += ", ? ";
        }
        
        
        PreparedStatement stmt = conn.prepareStatement("insert into " + entity.tableName() + "( " + altered_columns + ") values ( " + values + " );");
        
        int number = 1;
        for(Object t: columns_generic) {
        	if(t.getClass() == Integer.class) {
        		stmt.setInt(number,  (int) t);
        		number++;
        	}else if(t.getClass() == String.class) {
        		stmt.setString(number, (String) t);
        		number++;
        	}
        }
        
        stmt.execute();
    	
    }
    
    public static <T> void update(Class<T> type, List<Object> columns_generic, int id) throws SQLException {
    	log.info("Running query to insert an entry");
    	Entity entity = type.getDeclaredAnnotation(Entity.class);
    	Connection conn = ConnectionPool.getConnection();
    	
    	int counter=0;
    	String columns = "", id_column="";
        for (Field field : type.getDeclaredFields()) {
        	System.out.println(field);
        	if(field.getType() == int.class) {
	            for (Annotation a : field.getDeclaredAnnotations()) {
	                	 if(a.annotationType() == Column.class) {
	                    Column col = (Column) a;
	                    field.setAccessible(true);
	                    columns += col.columnName() + "=?, ";
	                    counter++;
	                    System.out.println("Proccessing a regular integer class " + counter);
	                }
	                	 if(a.annotationType() == PrimaryKey.class) {
	 	                    PrimaryKey col = (PrimaryKey) a;
	 	                    field.setAccessible(true);
	 	                    id_column += col.columnName();
	 	                }
	            }
        	}else if(field.getType() == String.class) {
	            for (Annotation a : field.getDeclaredAnnotations()) {
	                if (a.annotationType() == Column.class) {
	                    Column col = (Column) a;
	                    field.setAccessible(true);
	                    columns += col.columnName() + "=?, ";
	                    counter++;
	                    System.out.println("Proccessing a field of String class " + counter);
	                }
	            }
        	}
        }
    	
        String altered_columns = "";
        for(int i=0;i< columns.length()-2;i++) {
        	altered_columns += columns.charAt(i);
        }
        
        
        PreparedStatement stmt = conn.prepareStatement("update " + entity.tableName() + " set " + altered_columns + "  where " + id_column + "=? ");
        
        int number = 1;
        for(Object t: columns_generic) {
        	if(t.getClass() == Integer.class) {
        		stmt.setInt(number,  (int) t);
        		number++;
        	}else if(t.getClass() == String.class) {
        		stmt.setString(number, (String) t);
        		number++;
        	}
        }
        
        
        stmt.setInt(counter+1, id);
        
        stmt.execute();
    	
    }
    
}
