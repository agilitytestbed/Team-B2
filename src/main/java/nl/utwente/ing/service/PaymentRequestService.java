package nl.utwente.ing.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import nl.utwente.ing.database.DatabaseCommunication;
import nl.utwente.ing.model.PaymentRequest;
import nl.utwente.ing.model.Transaction;

public class PaymentRequestService {
	public static List<PaymentRequest> getPaymentRequests(ResultSet rs){
		List<PaymentRequest> result = new ArrayList<>();
		try {
			while(rs.next()) {
				List<Transaction> transactions = DatabaseCommunication.getTransactionsForPaymentRequest(rs.getInt("id"));
				PaymentRequest request = new PaymentRequest(rs.getInt("id"),rs.getString("description"), 
						rs.getLong("due_date"), rs.getDouble("amount"), rs.getInt("number_of_requests"),
						rs.getBoolean("filled"), transactions);
				result.add(request);
			}
		} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
		return result;
	}
}
