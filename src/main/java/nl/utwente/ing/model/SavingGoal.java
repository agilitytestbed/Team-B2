package nl.utwente.ing.model;

public class SavingGoal {
	private int id;
	private String name;
	private double goal;
	private double savePerMonth;
	private double minBalanceRequired;
	private double balance;
	
	public SavingGoal() {
		
	}

	
	public SavingGoal(int id, String name, double goal, double savePerMonth, double minBalanceRequired, double balance) {
		setId(id);
		setName(name);
		setGoal(goal);
		setSavePerMonth(savePerMonth);
		setMinBalanceRequired(minBalanceRequired);
		setBalance(balance);
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public double getGoal() {
		return goal;
	}


	public void setGoal(double goal) {
		this.goal = goal;
	}


	public double getSavePerMonth() {
		return savePerMonth;
	}


	public void setSavePerMonth(double savePerMonth) {
		this.savePerMonth = savePerMonth;
	}


	public double getMinBalanceRequired() {
		return minBalanceRequired;
	}


	public void setMinBalanceRequired(double minBalanceRequired) {
		this.minBalanceRequired = minBalanceRequired;
	}


	public double getBalance() {
		return balance;
	}


	public void setBalance(double balance) {
		this.balance = balance;
	}
	
	public boolean validSavingGoal() {
		if (name == null || goal <= 0 || savePerMonth <= 0 || minBalanceRequired < 0 || balance < 0 || balance > goal) {
			return false;
		} else {
			return true;
		}
	}
}
