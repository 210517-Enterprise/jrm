package com.revature.jrm;

import java.util.List;

public abstract class Model {
    /**
     * The connection pool used to get connections for the CRUD operations
     */
    private static ConnectionPool pool = new ConnectionPool();
    /**
     * The transaction manager is used to alter behavior if a transaction is in progress
     */
    private static TransactionManager transactionManager = new TransactionManager();

    /**
     * Returns an object from the database with the given id
     *
     * @param id the id of the entity
     * @return the requested entity
     */
    public static Model get(int id) {
        // 1. Use reflection API to get the table name from annotations
        // 2. Get connection from connection pool
        // 3. Query for entity using "where id=?"
        // 4. Construct object and return it
        return null;
    }

    /**
     * Returns a list of all objects of the model's type
     *
     * @return the list of all objects
     */
    public static List<Model> all() {
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
    public static List<Model> where(String query) {
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

    /**
     * Saves the current object to database
     */
    public void save() {
        // 1. Use reflection API to get the table name from annotations
        // 2. Get connection from connection pool
        // 3. Check if table exists, create it if it does not exist (maybe separate function)
        // 4. Check if the model object's id attribute is null
        // 5. Use TransactionManager to check if transaction in progress, if it is use that connection and do conn.commit()
        // 6. If the id is null then use insert to insert to database and set id attribute after insertion
        // 7. If the id is not null then use update statement instead
    }

    /**
     * Deletes the current object from the database
     */
    public void destroy() {
        // 1. Use reflection API to get the table name from annotations
        // 2. Get connection from connection pool
        // 3. Delete using where statement with id where id equals this object's id
    }
}