package nl.utwente.ing.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import nl.utwente.ing.model.MessageRule;

public class MessageRuleService {
	public static List<MessageRule> getMessageRules(ResultSet rs){
		List<MessageRule> result = new ArrayList<>();
		try {
			while(rs.next()) {
				result.add(new MessageRule(rs.getInt("id"), rs.getString("type"), rs.getDouble("value"), rs.getInt("category_id")));
			}
		} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
		return result;
	}
}
