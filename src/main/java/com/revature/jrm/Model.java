package com.revature.jrm;

import java.util.List;

public abstract class Model {
    /**
     * The connection pool used to get connections for the CRUD operations
     */
    private static ConnectionPool pool = new ConnectionPool();

    /**
     * Returns an object from the database with the given id
     *
     * @param id the id of the entity
     * @return the requested entity
     */
    public static Model get(int id) {
        return null;
    }

    /**
     * Returns a list of all objects of the model's type
     *
     * @return the list of all objects
     */
    public static List<Model> all() {
        return null;
    }

    /**
     * Returns a list of objects that match the given "WHERE" clause
     *
     * @param query the value to put in the where clause
     * @return list of objects that match
     */
    public static List<Model> where(String query) {
        return null;
    }

    /**
     * Deletes all entities of the model's type
     */
    public static void destroy_all() {
    }

    /**
     * Saves the current object to database
     */
    public void save() {
    }

    /**
     * Deletes the current object from the database
     */
    public void destroy() {
    }
}