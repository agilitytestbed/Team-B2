package nl.utwente.ing.service;

import java.sql.ResultSet;
import java.sql.SQLException;

import nl.utwente.ing.model.Category;

public class CategoryService {
	public static Category getCategory(ResultSet rs) {
		try {
			if (rs.next()) {
			    return new Category(rs.getInt("id"), rs.getString("name"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
