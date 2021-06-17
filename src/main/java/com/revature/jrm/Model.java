package com.revature.jrm;

public abstract class Model {
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