package com.revature.jrm;

public class TransactionManager {
    private static ConnectionPool pool = new ConnectionPool();

    public static void begin() {}
    public static void commit() {}
    public static void rollback() {}
    public static void rollback(String name) {}
    public static void releaseSavepoint(String name) {}
    public static void setSavepoint(String name) {}
    public static void setTransaction() {}
}
