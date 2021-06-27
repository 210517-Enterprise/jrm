# JRM

## Project Description
A java based ORM for simplifying connecting to and from an SQL database without the need for SQL or connection management. 

## Technologies Used

* PostgreSQL - version 42.2.12  
* Java - version 8.0  
* Apache commons - version 2.1  
* Log4j - version 1.7.30  
* Junit - version 4.13.1

## Features

List of features ready and TODOs for future development  
* Easy to use and straightforward user API.  
* No need for SQL, HQL, or any databse specific language.  
* Straightforward and simple Annotation based for ease of use.  

## Getting Started  
Currently project must be included as local dependency. to do so:
```shell
  git clone <this-repo>
  cd JRM
  mvn install
```
Next, place the following inside your project pom.xml file:
```XML
  <dependency>
    <groupId>com.revature</groupId>
    <artifactId>jrm</artifactId>
    <version>1.0-SNAPSHOT</version>
  </dependency>

```

Finally, inside your project structure you need a application.proprties file. 
 (typically located src/main/resources/)
 ``` 
  url=path/to/database
  username=username/of/database
  password=password/of/database  
  ```
  
## Usage  
  ### Annotating classes  
  All classes which represent objects in database must be annotated.
   - #### @Entity(tableName = "table_name)  
      - Indicates that this class is associated with table 'table_name'  
   - #### @Column(columnName = "column_name)  
      - Indicates that the Annotated field is a column in the table with the name 'column_name'  
   - #### @PrimaryKey(columnName = "column_name")  
      - Indicates that the Annotated field is a primary key serialized column in the table with the name 'column_name'  

  ### User API  
  
  - #### public static <T> T get(Class<T> type, int id)  
     - returns an object from the database with the given id  
  - #### public static <T> List<T> all(Class<T> type)   
     - returns a list of all objects of model's type 
  - #### public static <T> List<T> where(Class<T> type, String column_name, String requirement) 
  - #### public static <T> List<T> where(Class<T> type, String column_name, int requirement)  
     - returns a list of objects that match the given "WHERE" clause  
  - #### public static <T> void destroyAll(Class<T> type) 
     - Deletes all objects of the model's type
  - ####  public static <T> void destroy(T obj)  
     - Removes the given object from the database.
  - #### public static <T> void createTable(Class<T> type)
     - creates a given table for a given annotated class
  - #### public static <T> void dropTable(Class<T> type)
     - creates a given table for a given annotated class
  - ####  public static <T> boolean tableExists(Class<T> type)
     - returns a boolean value of whether an table exist
  - ####  public static <T> boolean recordExists(T obj)
     - returns a boolean value of whether an object exist
  - #### public static <T> void save(T obj)
     - create database table for entity if if table is doesn't exist
     - then update if obj is exist and inserts if it doesn't exist 
  - ####   public static void beginTransaction()
     - begins a transaction in the database by toggling auto commit feature 
  - ####   public static void commitTransaction()
     - commits a transaction in the database 
  - ####   public static void rollback(String savepoint)
     - rolls back to a save point
  - ####    public static void setSavepoint(String name)
     - set a save point
