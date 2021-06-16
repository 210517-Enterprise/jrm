package com.revature.metamodel;

import java.lang.reflect.Field;

import com.revature.annotations.PrimaryKey;



//Creating our fields
public class PrimaryKeyField {
	
	//declaring field type
	private Field field;
	
	//creating constructor for IdField
	public PrimaryKeyField(Field field) {
		
		if(field.getAnnotation(PrimaryKey.class) == null) {
			throw new IllegalArgumentException("Cannot create IdField object because it's not annotated with @Id");
		}
		
		this.field = field;
		
	}
	
	//Getter
	public String getName() {
		return field.getName();
	}
	
	//Get class method
	public Class<?> getType(){
		return field.getClass();
	}
	
	//ColumnName Method
	public String getColumnName() {
		return field.getAnnotation(PrimaryKey.class).columnName();
	}
	
	
	
	
}
