package io.DPA.transaction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class DatabaseCommunication {
	public static final String FILENAME = "data.db";
	public static final String URL = 
			"jdbc:sqlite:"
			+ FILENAME;
	
	/**
	 * Connects to the database.
	 * @return
	 * 		Connection object
	 */
	private static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

	/**
	 * Takes the given sql and executes it.
	 * @param sql
	 * 			The sql message as a string
	 */
	public static void executeSQL(String sql) {
		try (Connection conn = connect(); 
				Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * Generates all tables in the database.
	 */
	public static void generateTables() {
		String sql = null;
		sql = "CREATE TABLE IF NOT EXISTS transactions (" + 
				"transactionID integer PRIMARY KEY," +
				"sender text NOT NULL," + 
				"receiver text NOT NULL," + 
				"amount real," + 
				"date text," +
				"categoryID integer" +
				")";
		executeSQL(sql);
		
		sql = "CREATE TABLE IF NOT EXISTS categories (" + 
				"categoryID integer PRIMARY KEY," +
				"name text" +
				")";
		executeSQL(sql);
		
	}
	
	/**
	 * Gets the transaction from the database with a specific id.
	 * @param id
	 * 			Id of the transaction
	 * @return
	 * 			Transaction object from the database
	 */
	public static Transaction getTransaction(int id) {
		String sql = "SELECT * FROM transactions WHERE transactionID == ?";
		try (Connection conn = connect();
	             PreparedStatement pstmt  = conn.prepareStatement(sql)) {
	            
	        pstmt.setInt(1, id);
	            
	        ResultSet rs  = pstmt.executeQuery();
	            
	        if (rs.next()) {
	            return new Transaction(rs.getInt("transactionID"), rs.getString("sender"),
	            		rs.getString("receiver"), rs.getDouble("amount"), 
	                	rs.getString("date"), rs.getInt("categoryID"));
	        }
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		return null;
	}
	
	/**
	 * Gets all the transactions in the db.
	 * @return
	 * 			List of transactions
	 */
	public static List<Transaction> getAllTransactions(int offset, int limit) {
		List<Transaction> transactions = new ArrayList<>();
		
		String sql = "SELECT * FROM transactions";
		try (Connection conn = connect();
	         Statement stmnt  = conn.createStatement();
			 ResultSet rs    = stmnt.executeQuery(sql)) {
	            
	        int element = 0;
	        while (rs.next()) {
	        		if (element >= offset && transactions.size() < limit) {
	        			transactions.add(new Transaction(rs.getInt("transactionID"), rs.getString("sender"),
	        					rs.getString("receiver"), rs.getDouble("amount"), 
	        					rs.getString("date"), rs.getInt("categoryID")));
	        		}
	        	element++;
	        }
	        return transactions;
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		return null;
		
	}
	

	
	/**
	 * Gets all the transaction that belong to the category with the given id.
	 * @param categoryID
	 * 			The id of the category
	 * @return
	 * 			List of transactions that belong to the given category
	 */
	public static List<Transaction> getAllTransactions(int categoryID){
		List<Transaction> transactions = new ArrayList<>();
		
		String sql = "SELECT * FROM transactions WHERE categoryID = ?";
		try (Connection conn = connect();
	         PreparedStatement pstmt  = conn.prepareStatement(sql)) {
			pstmt.setInt(1, categoryID);
			
			ResultSet rs  = pstmt.executeQuery();
	            
	        while (rs.next()) {
	            transactions.add(new Transaction(rs.getInt("transactionID"), rs.getString("sender"),
	            		rs.getString("receiver"), rs.getDouble("amount"), 
	                	rs.getString("date"), rs.getInt("categoryID")));
	        }
	        return transactions;
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		return null;
		
	}
	
	/**
	 * Adds a transaction object to the database.
	 * @param t
	 * 			Transaction object
	 */
	public static void addTransaction(Transaction t) {
		String sql = "INSERT INTO transactions VALUES(?,?,?,?,?,?)";
		
		try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, t.getId());
            pstmt.setString(2, t.getSender());
            pstmt.setString(3, t.getReceiver());
            pstmt.setDouble(4, t.getAmount());
            pstmt.setString(5, t.getDate());
            pstmt.setInt(6, t.getcategoryID());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}
	
	/**
	 * Updates the transaction with the given id 
	 * @param t
	 * 			The updated transaction
	 * @param id
	 * 			Id of the transaction
	 */
	public static void updateTransaction(Transaction t, int id) {
		String sql = "UPDATE transactions SET sender = ? , "
                + "receiver = ? , "
                + "amount = ? , "
                + "date = ? , "
                + "categoryID = ?"
                + "WHERE transactionID = ?";
 
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
 
            // set the corresponding param
            pstmt.setString(1, t.getSender());
            pstmt.setString(2, t.getReceiver());
            pstmt.setDouble(3, t.getAmount());
            pstmt.setString(4, t.getDate());
            pstmt.setInt(5, t.getcategoryID());
            pstmt.setInt(6, id);
            // update 
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}
	
	/**
	 * Deletes the transaction with the given id
	 * @param id
	 * 			The id of the transaction to delete
	 */
	public static void deleteTransaction(int id) {
        String sql = "DELETE FROM transactions WHERE transactionID = ?";
        
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
 
            // set the corresponding param
            pstmt.setInt(1, id);
            // execute the delete statement
            pstmt.executeUpdate();
 
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}
	
	public static void assignCategory(int categoryID, int transactionID) {
		String sql = "UPDATE transactions SET categoryID = ?"
                + "WHERE transactionID = ?";
 
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
 
            // set the corresponding param
            pstmt.setInt(1, categoryID);
            pstmt.setInt(2, transactionID);
            // update 
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}
	
	/**
	 * Gets all categories from the database.
	 * @return
	 * 			List of categories
	 */
	public static List<Category> getAllCategories() {
		List<Category> categories = new ArrayList<>();
		
		String sql = "SELECT * FROM categories";
		try (Connection conn = connect();
	         Statement stmnt  = conn.createStatement();
			 ResultSet rs    = stmnt.executeQuery(sql)) {
	            
	            
	        while (rs.next()) {
	            categories.add(new Category(rs.getInt("categoryID"), rs.getString("name")));
	        }
	        return categories;
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		return null;
	}
	/**
	 * Adds the given category object to the database.
	 * @param c
	 * 			Category object
	 */
	public static void addCategory(Category c) {
		String sql = "INSERT INTO categories VALUES(?,?)";
		
		try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, c.getCategoryID());
            pstmt.setString(2, c.getName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}
	
	/**
	 * Gets the category from the database with a specific id.
	 * @param id
	 * 			Id of the category
	 * @return
	 * 			Category object from the database
	 */
	public static Category getCategory(int id) {
		String sql = "SELECT * FROM categories WHERE categoryID == ?";
		try (Connection conn = connect();
	             PreparedStatement pstmt  = conn.prepareStatement(sql)) {
	            
	        pstmt.setInt(1, id);
	            
	        ResultSet rs  = pstmt.executeQuery();
	            
	        if (rs.next()) {
	            return new Category(rs.getInt("categoryID"), rs.getString("name"));
	        }
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		return null;
	}
	
	/**
	 * Deletes the category with the given id
	 * @param id
	 * 			The id of the category to delete
	 */
	public static void deleteCategory(int id) {
		String sql = "DELETE FROM categories WHERE categoryID = ?";
        
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
 
            // set the corresponding param
            pstmt.setInt(1, id);
            // execute the delete statement
            pstmt.executeUpdate();
 
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}
	
	/**
	 * Updates the category with the given id 
	 * @param t
	 * 			The updated category
	 * @param id
	 * 			Id of the category
	 */
	public static void updateCategory(Category c, int id) {
		String sql = "UPDATE categories SET name = ? "
                + "WHERE categoryID = ?";
 
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
 
            // set the corresponding param
            pstmt.setString(1, c.getName());
            pstmt.setInt(2, id);
            // update 
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}
	
	

	public static void main(String[] args) {
		generateTables();
		//addTransaction(new Transaction(0, "alice", "bob", 15.0, "now", 0));
		//addTransaction(new Transaction(1, "alice", "bob", 15.0, "now + 1", 0));
		addCategory(new Category(0, "debt"));
		
	}
}
