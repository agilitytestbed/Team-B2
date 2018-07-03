package nl.utwente.ing.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import nl.utwente.ing.model.SavingGoal;

public class SavingGoalService {
	public static List<SavingGoal> getSavingGoals(ResultSet rs){
		List<SavingGoal> result = new ArrayList<>();
		try {
			while(rs.next()) {
				result.add(new SavingGoal(rs.getInt("id"), rs.getString("name"), rs.getDouble("goal"), rs.getDouble("savePerMonth"), rs.getDouble("minBalanceRequired"), rs.getDouble("balance")));
			}
		} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
		return result;
	}
}
