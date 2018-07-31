package nl.utwente.ing.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import nl.utwente.ing.model.Message;

public class MessageService {
	public static List<Message> getMessages(ResultSet rs){
		List<Message> result = new ArrayList<>();
		try {
			while(rs.next()) {
				result.add(new Message(rs.getInt("id"), rs.getString("message"), rs.getLong("date"), rs.getBoolean("read"), rs.getString("type")));
			}
		} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
		return result;
	}
}
