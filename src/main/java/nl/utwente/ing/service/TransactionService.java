package nl.utwente.ing.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import nl.utwente.ing.database.DatabaseCommunication;
import nl.utwente.ing.model.Transaction;

public class TransactionService {
	public static List<Transaction> getTransactions(ResultSet rs){
		List<Transaction> result = new ArrayList<>();
		try {
			while(rs.next()) {
				int categoryID = rs.getInt("categoryID");
				if (rs.wasNull()) {
					categoryID = -1;
				}
				result.add(new Transaction(rs.getInt("id"), rs.getLong("date"),
	            		rs.getDouble("amount"), rs.getString("description"), rs.getString("externalIBAN"), rs.getString("type"),
	            		DatabaseCommunication.getCategory(categoryID)));
			}
		} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
		return result;
	}
	
}
