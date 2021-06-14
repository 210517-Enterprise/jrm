package com.revature.jrm;

public class TransactionManager {
    private static ConnectionPool pool = new ConnectionPool();

    public void begin() {}
    public void commit() {}
    public void rollback() {}
    public void rollback(String name) {}
    public void releaseSavepoint(String name) {}
    public void setSavepoint(String name) {}
    public void setTransaction() {}
}
