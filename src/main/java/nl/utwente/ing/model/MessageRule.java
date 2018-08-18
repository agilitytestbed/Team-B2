package nl.utwente.ing.model;

public class MessageRule {
	private int id;
	private MessageType type;
	private double value;
	private int category_id;
	
	public MessageRule() {
		
	}
	
	public MessageRule(int id, String type, double value, int category_id) {
		setId(id);
		setType(MessageType.valueOf(type));
		setValue(value);
		setCategory_id(category_id);
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public MessageType getType() {
		return type;
	}
	public void setType(MessageType type) {
		this.type = type;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public int getCategory_id() {
		return category_id;
	}
	public void setCategory_id(int category_id) {
		this.category_id = category_id;
	}
	
	public boolean validMessageRule() {
		if (type == null || value < 0) {
			return false;
		}
		return true;
	}
	
	
}
