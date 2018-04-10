package nl.utwente.ing.transaction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Transaction {
	private int id;
	
	private String externalIBAN;
	
	private double amount;
	private String date;
	private transactionType type;
	private Category category;
	
	public enum transactionType{
		deposit, withdrawal
	}
	
	public Transaction() {
		
	}
	
	public Transaction(int id, String date, double amount,
			String externalIBAN, String type, Category category) {
		setId(id);
		setAmount(amount);
		setDate(date);
		setType(transactionType.valueOf(type));
		setExternalIBAN(externalIBAN);
		setCategory(category);
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

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public String getExternalIBAN() {
		return externalIBAN;
	}

	public void setExternalIBAN(String external_iban) {
		this.externalIBAN = external_iban;
	}

	public transactionType getType() {
		return type;
	}

	public void setType(transactionType type) {
		this.type = type;
	}
	
	public int CategoryID() {
		if (category != null) {
			return category.getId();
		} else {
			return -1;
		}
	}
	
	public boolean validTransaction() {
		
		// if a value is null
		if (externalIBAN == null || date == null || type == null) {
			return false;
		}
		
		
		// if amount is negative or zero
		if (amount < 1) {
			System.out.println("Amount value problem");
			return false;
		}
		
		// if the date is not valid date-time
		try {
			DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;
		    timeFormatter.parse(date);
		} catch (DateTimeParseException e) {
			return false;
		}
		
		return true;
		
	}
}
