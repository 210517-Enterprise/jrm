package com.revature.metamodel;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import com.revature.annotations.Column;
import com.revature.annotations.Entity;
import com.revature.annotations.PrimaryKey;

public class Metamodel<T> {
	
	//The class
	private Class<T> clazz;
	
	//field for primary keys
	private PrimaryKeyField primaryKeyField;
	
	//A list for column fields
	private List<ColumnField> columnFields;
	
	//A list for foreign Key fields
//	private List<ForeignKeyField> foreignKeyFields;

	//contructor with only the class as a parameter
	public Metamodel(Class<T> clazz) {
		super();
		this.clazz = clazz;
		this.columnFields = new LinkedList<>();
//		this.foreignKeyFields = new LinkedList<>();
		
	}
	
	//
	public static <T> Metamodel<T> of(Class<T> clazz){
		
		if(clazz.getAnnotation(Entity.class) == null) {
			throw new IllegalArgumentException("Cannot create Metamodel object because the following class, " + clazz.getName() +" isn't annotated with @Entity");
		}
		
		return new Metamodel<>(clazz);
		
	}
	
	
	
	//a method to get the class name
	public String getClassName() {
		return clazz.getName();
	}
	
	//a method to get the class name in a simpler representation
	public String getSimpleClassName() {
		return clazz.getSimpleName();
	}
	
	
	//making a getter for field idField
	public PrimaryKeyField getPrimaryKey() {
			
		//Declaring a field array // import java reflect package
		Field[] fields = clazz.getDeclaredFields();
		
		//iterating through field checking if a field has been annotated with the right annotation then returning the field
		for(Field field: fields) {
			PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
			if(primaryKey != null) {
				return new PrimaryKeyField(field);
			}
		}
		
		throw new RuntimeException("Did not find the field annotated with @Id in: "+ clazz.getName());
		
	}
	
	public List<ColumnField> getColumns() {
		
		//Declaring a field array // import java reflect package
		Field[] fields = clazz.getDeclaredFields();
		
		//iterating through field checking if a field has been annotated with the right annotation then returning the field
		for(Field field: fields) {
			Column column = field.getAnnotation(Column.class);
			if(column != null) {
				columnFields.add(new ColumnField(field));
			}
		}
		
		if(columnFields.isEmpty()) {
		throw new RuntimeException("Did not find the field annotated with @Column in: "+ clazz.getName());
		}
		
		return columnFields;
		
	}
	
//public List<ForeignKeyField> getForeignKeys() {
//		
//		//Declaring a field array // import java reflect package
//		Field[] fields = clazz.getDeclaredFields();
//		
//		//iterating through field checking if a field has been annotated with the right annotation then returning the field
//		for(Field field: fields) {
//			JoinColumn column = field.getAnnotation(JoinColumn.class);
//			if(column != null) {
//				foreignKeyFields.add(new ForeignKeyField(field));
//			}
//		}
//		
//		if(foreignKeyFields.isEmpty()) {
//		throw new RuntimeException("Did not find the field annotated with @JoinColumn in: "+ clazz.getName());
//		}
//		
//		return foreignKeyFields;
//		
//	}
	
	
	
	
}
