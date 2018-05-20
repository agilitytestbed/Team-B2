package nl.utwente.ing.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;

@Configuration
public class DBInitializeConfig {
	@PostConstruct
	public void initialize() {
		try {
			Connection connection = DatabaseCommunication.connect();
			Statement statement = connection.createStatement();
		
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS transactions (" + 
					"id integer PRIMARY KEY," +
					"date text," +
					"amount real," + 
					"description text," +
					"externalIBAN text NOT NULL," +
					"type text NOT NULL," +
					"categoryID integer" +
					")");
			
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS categories (" + 
					"id integer PRIMARY KEY," +
					"name text" +
					")");
			
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS sessions (" + 
					"session integer PRIMARY KEY)");
			
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS transactionIds (" + 
					"session integer," +
					"id integer" +
					")");
			
			
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS categoryIds (" + 
					"session integer," +
					"id integer" +
					")");
			
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS categoryRuleIds (" + 
					"session integer," +
					"id integer" +
					")");
			
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS categoryRules (" + 
					"id integer PRIMARY KEY," +
					"description text," +
					"iBAN text," +
					"type text," +
					"category_id integer," +
					"applyOnHistory integer" +
					")");
			statement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
