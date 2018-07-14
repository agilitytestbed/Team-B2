package nl.utwente.ing.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import nl.utwente.ing.model.CategoryRule;

public class CategoryRuleService {
	public static List<CategoryRule> getCategoryRules(ResultSet rs){
		List<CategoryRule> categoryRules = new ArrayList<>();
		try {
			while (rs.next()) {
			    categoryRules.add(new CategoryRule(rs.getInt("id"), 
			    		rs.getString("description"), rs.getString("iBAN"),
			    		rs.getString("type"), rs.getInt("category_id"), rs.getBoolean("applyOnHistory")
			    		));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return categoryRules;
	}
	
	public static CategoryRule getCategoryRule(ResultSet rs){
		try {
			if (rs.next()) {
			    return new CategoryRule(rs.getInt("id"), rs.getString("description"),
			    		rs.getString("iBAN"), rs.getString("type"), rs.getInt("category_id"),
			    		rs.getBoolean("applyOnHistory")
			    		);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
