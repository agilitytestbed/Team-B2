package io.DPA.transaction;

public class Category {
	private int categoryID;
	private String name;
	
	public Category() {
		
	}
	
	public Category(int categoryID, String name) {
		this.setCategoryID(categoryID);
		this.setName(name);
	}

	public int getCategoryID() {
		return categoryID;
	}

	public void setCategoryID(int categoryID) {
		this.categoryID = categoryID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
