package io.DPA.transaction;

public class Transaction {
	private int id;
	private String sender;
	private String receiver;
	private double amount;
	private String date;
	private int categoryID;
	
	public Transaction() {
		
	}
	
	public Transaction(int id, String sender, String receiver,
			double amount, String date, int categoryID) {
		this.setId(id);
		this.setSender(sender);
		this.setReceiver(receiver);
		this.setAmount(amount);
		this.setDate(date);
		this.setcategoryID(categoryID);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public int getcategoryID() {
		return categoryID;
	}

	public void setcategoryID(int categoryID) {
		this.categoryID = categoryID;
	}
}
