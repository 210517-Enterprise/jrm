package com.revature.jrm;

import java.util.List;

public abstract class Model {
    private static ConnectionPool pool = new ConnectionPool();

    public static Model get(int id) {
        return null;
    }

    public static List<Model> all() {
        return null;
    }

    public static Model find(String query) {
        return null;
    }

    public static List<Model> where(String query) {
        return null;
    }

    public static void destroy_all() {
    }

    public void save() {
    }

    public void destroy() {
    }
}