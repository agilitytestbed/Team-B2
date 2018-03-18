package nl.utwente.ing.transaction;

public class Transaction {
	private int id;
	
	private String externalIBAN;
	
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
			String externalIBAN, String type, int categoryID) {
		setId(id);
		setAmount(amount);
		setDate(date);
		setType(transactionType.valueOf(type));
		setExternalIBAN(externalIBAN);
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
	
	public boolean validTransaction() {
		boolean response = true;
		
		// if a value is null
		if (externalIBAN == null || date == null || type == null) {
			response = false;
		}
		
		
		// if amount is negative or zero
		if (amount < 1) {
			response = false;
			System.out.println("Amount value problem");
		}
		
		// if category ID is negative category is not in the database
		if (categoryID < 0 || !DatabaseCommunication.categoryExists(categoryID)) {
			response = false;
			System.out.println("CategoryID problem");
		}
		
		
		
		return response;
	}
}
