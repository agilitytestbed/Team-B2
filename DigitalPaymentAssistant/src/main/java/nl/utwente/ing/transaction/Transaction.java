package nl.utwente.ing.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Transaction {
	private int id;
	
	@JsonProperty("external-iban")
	private String external_iban;
	
	private double amount;
	private String date;
	private transactionType type;
	private int categoryID;
	
	public enum transactionType{
		deposit, withdrawal
	}
	
	public Transaction() {
		
	}
	
	public Transaction(int id, String date, double amount,
			String external_iban, String type, int categoryID) {
		setId(id);
		setAmount(amount);
		setDate(date);
		setType(transactionType.valueOf(type));
		setExternal_iban(external_iban);
		setcategoryID(categoryID);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public String getExternal_iban() {
		return external_iban;
	}

	public void setExternal_iban(String external_iban) {
		this.external_iban = external_iban;
	}

	public transactionType getType() {
		return type;
	}

	public void setType(transactionType type) {
		this.type = type;
	}
	
	public boolean validTransaction() {
		boolean response = true;
		
		// if the transaction id already exists in the database
		if (DatabaseCommunication.transactionExists(id)) {
			response = false;
		}
		
		// if amount is negative or zero
		if (amount < 1) {
			response = false;
		}
		
		// if category ID is negative or zero or category is not in the database
		if (categoryID < 1 || !DatabaseCommunication.transactionExists(categoryID)) {
			response = false;
		}
		
		
		return response;
	}
}
