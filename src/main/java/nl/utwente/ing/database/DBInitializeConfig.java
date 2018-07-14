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
			
			connection.setAutoCommit(false);
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS transactions (" + 
					"id integer PRIMARY KEY," +
					"date integer NOT NULL," +
					"amount real," + 
					"description text NOT NULL," +
					"externalIBAN text NOT NULL," +
					"type text NOT NULL," +
					"categoryID integer," + 
					" FOREIGN KEY(categoryID) REFERENCES categories(id) ON DELETE SET NULL" +
					")");
			
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS categories (" + 
					"id integer PRIMARY KEY," +
					"name text NOT NULL" +
					")");
			
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS paymentRequests (" + 
					"id integer PRIMARY KEY," +
					"description text NOT NULL," +
					"due_date text NOT NULL," +
					"amount real NOT NULL," +
					"number_of_requests integer NOT NULL," +
					"filled integer NOT NULL DEFAULT 0" +
					")");
			
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS paymentRequestIds (" + 
					"session integer NOT NULL," +
					"id integer NOT NULL, " +
					"PRIMARY KEY (session,id)," +
					"FOREIGN KEY (session) REFERENCES sessions(session)," +
					"FOREIGN KEY (id) REFERENCES paymentRequests(id) ON DELETE CASCADE" + 
					")");
			
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS paymentRequestTransactions (" + 
					"paymentRequestId integer," +
					"transactionId integer," +
					"PRIMARY KEY(paymentRequestId, transactionID)," +
					"FOREIGN KEY (paymentRequestId) REFERENCES paymentRequests(id) ON DELETE CASCADE," + 
					"FOREIGN KEY (transactionId) REFERENCES transactions(id) ON DELETE CASCADE" +
					")");
			
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS sessions (" + 
					"session integer PRIMARY KEY)");
			
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS transactionIds (" + 
					"session integer NOT NULL," +
					"id integer NOT NULL, " +
					"PRIMARY KEY (session,id)," +
					"FOREIGN KEY (session) REFERENCES sessions(session)," +
					"FOREIGN KEY (id) REFERENCES transactions(id) ON DELETE CASCADE" + 
					")");
			

			statement.executeUpdate("CREATE TABLE IF NOT EXISTS internalTransactions (" + 
					"savingGoalId integer," +
					"transactionId integer," +
					"PRIMARY KEY(savingGoalId, transactionID)," +
					"FOREIGN KEY (savingGoalId) REFERENCES savingGoals(id) ON DELETE CASCADE," + 
					"FOREIGN KEY (transactionId) REFERENCES transactions(id) ON DELETE CASCADE" +
					")");
			
			statement.executeUpdate("CREATE TRIGGER IF NOT EXISTS same_session_check " + 
					"BEFORE INSERT ON internalTransactions WHEN (EXISTS (SELECT * FROM transactionIds ti, savingGoalIds sgi " + 
					"WHERE ti.id = New.transactionId " + 
					"AND sgi.id = New.savingGoalId " + 
					"AND ti.session != sgi.session)) " + 
					"BEGIN " + 
					"    SELECT RAISE(FAIL, \"The transaction and the saving goal are from different sessions\"); " + 
					"END;");
			
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS categoryIds (" + 
					"session integer NOT NULL," +
					"id integer NOT NULL, " +
					"PRIMARY KEY (session,id)," +
					"FOREIGN KEY (session) REFERENCES sessions(session)," +
					"FOREIGN KEY (id) REFERENCES categories(id) ON DELETE CASCADE" + 
					")");
			
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS categoryRuleIds (" + 
					"session integer NOT NULL," +
					"id integer NOT NULL, " +
					"PRIMARY KEY (session,id)," +
					"FOREIGN KEY (session) REFERENCES sessions(session)," +
					"FOREIGN KEY (id) REFERENCES categoryRules(id) ON DELETE CASCADE" + 
					")");
			
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS categoryRules (" + 
					"id integer PRIMARY KEY," +
					"description text NOT NULL," +
					"iBAN text NOT NULL," +
					"type text NOT NULL," +
					"category_id integer NOT NULL," +
					"applyOnHistory integer NOT NULL" +
					")");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS savingGoals (" + 
					"id integer PRIMARY KEY," +
					"name text NOT NULL," +
					"goal real NOT NULL," +
					"savePerMonth real NOT NULL," +
					"minBalanceRequired real NOT NULL," +
					"balance real NOT NULL" +
					")");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS savingGoalIds (" + 
					"session integer NOT NULL," +
					"id integer NOT NULL, " +
					"PRIMARY KEY (session,id)," +
					"FOREIGN KEY (session) REFERENCES sessions(session)," +
					"CONSTRAINT savingGoalId" + 
					"        FOREIGN KEY (id) " + 
					"        REFERENCES savingGoals (id) ON DELETE CASCADE " + 
					")");
			statement.executeUpdate("PRAGMA foreign_keys = ON");
			
			connection.commit();
			connection.setAutoCommit(true);
			statement.close();
			connection.close();
			System.out.println("Tables initialized!");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
