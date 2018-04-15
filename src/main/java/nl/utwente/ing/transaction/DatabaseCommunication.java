package nl.utwente.ing.transaction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class DatabaseCommunication {
	public static final String FILENAME = "data.db";
	public static final String URL = 
			"jdbc:sqlite:"
			+ FILENAME;
	
	/*
	 * -------------------- Code for handling sessions --------------------
	 */
	public static Set<Integer> getTransactionIds(int sessionID) {
		Set<Integer> ids = new HashSet<>();
		
		String sql = "SELECT * FROM transactionSessions WHERE session == ?";
		try (Connection conn = connect();
	             PreparedStatement pstmt  = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, sessionID);
	        ResultSet rs  = pstmt.executeQuery();
	            
	        if (rs.next() && rs.getString("transactions") != null) {
		        	for (String segment : new ArrayList<String>(Arrays.asList(rs.getString("transactions").split(",")))) {
		        		ids.add(Integer.parseInt(String.valueOf(segment)));
		        	}
	        }
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		
		return ids;
	}
	
	public static Set<Integer> getCategoryIds(int sessionID) {
		Set<Integer> ids = new HashSet<>();
		
		String sql = "SELECT * FROM categorySessions WHERE session == ?";
		try (Connection conn = connect();
	             PreparedStatement pstmt  = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, sessionID);
	        ResultSet rs  = pstmt.executeQuery();
	            
	        if (rs.next() && rs.getString("categories") != null) {
		        	for (String segment : new ArrayList<String>(Arrays.asList(rs.getString("categories").split(",")))) {
		        		ids.add(Integer.parseInt(String.valueOf(segment)));
		        	}
	        }
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		
		return ids;
	}
	//TODO
	public static void addTransactionId(int sessionID, int id) {
		String sql = "UPDATE transactionSessions SET transactions = ? WHERE session = ?";
		
		try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
			Set<Integer> currentIds = getTransactionIds(sessionID);
			currentIds.add(id);
			String newIdList = "";
			for (int i : currentIds) {
				newIdList += "," +String.valueOf(i);
			}
			// Substring to remove the first comma
            pstmt.setString(1, newIdList.substring(1));
            pstmt.setInt(2, sessionID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}
	
	public static void addCategoryId(int sessionID, int id) {
		String sql = "UPDATE categorySessions SET categories = ? WHERE session = ?";
		
		try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
			Set<Integer> currentIds = getCategoryIds(sessionID);
			currentIds.add(id);
			String newIdList = "";
			for (int i : currentIds) {
				newIdList += "," +String.valueOf(i);
			}
			// Substring to remove the first comma
            pstmt.setString(1, newIdList.substring(1));
            pstmt.setInt(2, sessionID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}
	
	public static void addSession(int sessionID) {
		
		// Add transaction session
		String sql = "INSERT INTO transactionSessions(session) VALUES(?)";
		try (Connection conn = connect();
	             PreparedStatement pstmt  = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, sessionID);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		
		// Add category session
		sql = "INSERT INTO categorySessions(session) VALUES(?)";
		try (Connection conn = connect();
	             PreparedStatement pstmt  = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, sessionID);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
	}
	
	public static int getMaxSessionId() {
		String sql = "SELECT max(session) from transactionSessions";
		 try (Connection conn = connect();
	             Statement stmt  = conn.createStatement();
	             ResultSet rs    = stmt.executeQuery(sql)){
	            
	            // loop through the result set
	            if (rs.next()) {
	                return rs.getInt("max(session)");
	            }
	        } catch (SQLException e) {
	            System.out.println(e.getMessage());
	        }
		 return -1;
	}
	
	public static boolean validSessionId(int sessionID) {
		String sql = "SELECT * FROM transactionSessions WHERE session == ?";
		try (Connection conn = connect();
	             PreparedStatement pstmt  = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, sessionID);
	        ResultSet rs  = pstmt.executeQuery();
	            
	        if (rs.next()) {
		        return true;
	        }
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		
		return false;
	}
	
	public static void deleteTransactionId(int sessionID, int id) {
		String sql = "UPDATE transactionSessions SET transactions = ? WHERE session = ?";
		
		try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
			Set<Integer> currentIds = getTransactionIds(sessionID);

			String newIdList = "";
			for (int i : currentIds) {
				if (i != id) {
					newIdList += "," +String.valueOf(i);
				}
			}
			// Substring to remove the first comma
            pstmt.setString(1, newIdList.substring(1));
            pstmt.setInt(2, sessionID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}
	
	public static void deleteCategoryId(int sessionID, int id) {
		String sql = "UPDATE categorySessions SET categories = ? WHERE session = ?";
		
		try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
			Set<Integer> currentIds = getCategoryIds(sessionID);

			String newIdList = "";
			for (int i : currentIds) {
				if (i != id) {
					newIdList += "," +String.valueOf(i);
				}
			}
			// Substring to remove the first comma
            pstmt.setString(1, newIdList.substring(1));
            pstmt.setInt(2, sessionID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}
	
	/*
	 * -------------------- Code for normal data --------------------
	 */
	
	/**
	 * Queries the database for the largest category index.
	 * @return
	 * 		int representing the largest category index or -1 if there are no entries
	 */
	private static int getMaxCategoryIndex() {
		String sql = "SELECT  id\n" + 
				"FROM    categories mo\n" + 
				"WHERE   NOT EXISTS\n" + 
				"        (\n" + 
				"        SELECT  NULL\n" + 
				"        FROM    categories mi \n" + 
				"        WHERE   mi.id = mo.id + 1\n" + 
				"        )\n" + 
				"ORDER BY\n" + 
				"        id\n" + 
				"LIMIT 1";
		try (Connection conn = connect();
	             Statement stmt  = conn.createStatement();
	             ResultSet rs    = stmt.executeQuery(sql)){
	            
	            if (rs.next()) {
	            		return rs.getInt("id");
	            }
	            return 1;
	        } catch (SQLException e) {
	            System.out.println(e.getMessage());
	        }
		return -1;
	}
	
	/**
	 * Queries the database for the largest transaction index.
	 * @return
	 * 		int representing the largest transaction index or -1 if there are no entries
	 */
	private static int getMaxTransactionIndex() {
		String sql = "SELECT  id\n" + 
				"FROM    transactions mo\n" + 
				"WHERE   NOT EXISTS\n" + 
				"        (\n" + 
				"        SELECT  NULL\n" + 
				"        FROM    transactions mi \n" + 
				"        WHERE   mi.id = mo.id + 1\n" + 
				"        )\n" + 
				"ORDER BY\n" + 
				"        id\n" + 
				"LIMIT 1";
		try (Connection conn = connect();
	             Statement stmt  = conn.createStatement();
	             ResultSet rs    = stmt.executeQuery(sql)){
	            
	            if (rs.next()) {
	            		return rs.getInt("id");
	            }
	            return 1;
	        } catch (SQLException e) {
	            System.out.println(e.getMessage());
	        }
		return -1;
	}
	
	/**
	 * Adds the set of values as an array in the sql statement string
	 * @param sql
	 * 		String of sql to modify
	 * @param set
	 * 		Set of values to insert
	 * @return
	 * 		String of modified sql
	 */
	private static <E> String setToSql(String sql, Set<E> set) {
		Iterator<E> iter = set.iterator();
		String result = "";
		result += sql + "(";
		
		while (iter.hasNext()) {
			result += String.valueOf(iter.next());
			if (iter.hasNext()) {
				result +=",";
			}
		}
		result += ")";
		return result;
	}
	
	
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
				"id integer PRIMARY KEY," +
				"date text," +
				"amount real," + 
				"externalIBAN text NOT NULL," +
				"type text NOT NULL," +
				"categoryID integer" +
				")";
		executeSQL(sql);
		
		sql = "CREATE TABLE IF NOT EXISTS categories (" + 
				"id integer PRIMARY KEY," +
				"name text" +
				")";
		executeSQL(sql);
		
		sql = "CREATE TABLE IF NOT EXISTS transactionSessions (" + 
				"session integer PRIMARY KEY," +
				"transactions text" +
				")";
		executeSQL(sql);
		
		sql = "CREATE TABLE IF NOT EXISTS categorySessions (" + 
				"session integer PRIMARY KEY," +
				"categories text" +
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
	public static Transaction getTransaction(int id, Set<Integer> sessionIds) {
		String sql = "SELECT * FROM transactions WHERE id == ? AND id IN ";
		sql = setToSql(sql, sessionIds);
		try (Connection conn = connect();
	             PreparedStatement pstmt  = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, id);
	        ResultSet rs  = pstmt.executeQuery();
	            
	        if (rs.next()) {
	            return new Transaction(rs.getInt("id"), rs.getString("date"),
	            		rs.getDouble("amount"), rs.getString("externalIBAN"), rs.getString("type"),
	            		getCategory(rs.getInt("categoryID")));
	        }
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		return null;
	}
	
	/**
	 * Returns whether the transaction with the given id exists in the database
	 * @param id
	 * 			Integer representing the id of the transaction to query
	 * @return
	 * 			Boolean indicating whether the transaction is present or not in the database
	 */
	public static boolean transactionExists(int id) {
		String sql = "SELECT * FROM transactions WHERE id == ?";
		try (Connection conn = connect();
	             PreparedStatement pstmt  = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, id);
	            
	        ResultSet rs  = pstmt.executeQuery();
	            
	        if (rs.next()) {
	            return true;
	        }
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		return false;
	}
	
	/**
	 * Returns whether the category with the given id exists in the database
	 * @param id
	 * 			Integer representing the id of the category to query
	 * @return
	 * 			Boolean indicating whether the category is present or not in the database
	 */
	public static boolean categoryExists(int id) {
		String sql = "SELECT * FROM categories WHERE id == ?";
		try (Connection conn = connect();
	             PreparedStatement pstmt  = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, id);
	            
	        ResultSet rs  = pstmt.executeQuery();
	            
	        if (rs.next()) {
	            return true;
	        }
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		return false;
	}
	
	/**
	 * Gets all the transactions in the db by filtering with optional parameters.
	 * @return
	 * 			List of transactions
	 */
	public static List<Transaction> getAllTransactions(int offset, int limit,
			int categoryID, Set<Integer> sessionIds) {
		List<Transaction> transactions = new ArrayList<>();
		
		
		String sql = "SELECT * FROM transactions WHERE id IN ";
		sql = setToSql(sql, sessionIds);
		
		if (categoryID != -1) {
			sql += " AND categoryID = ?";
		}
		
		sql += " LIMIT ?,?";
		try (Connection conn = connect();
		     PreparedStatement pstmt  = conn.prepareStatement(sql)) {

			
			
			if (categoryID != -1) {
				pstmt.setInt(1, categoryID);
				pstmt.setInt(2, offset);
				pstmt.setInt(3, limit);
			} else {
				pstmt.setInt(1, offset);
				pstmt.setInt(2, limit);
			}
				
			ResultSet rs  = pstmt.executeQuery();
	        while (rs.next()) {
	        		transactions.add(new Transaction(rs.getInt("id"), rs.getString("date"),
	    	            	rs.getDouble("amount"), rs.getString("externalIBAN"), rs.getString("type"),
	    	            	getCategory(rs.getInt("categoryID"))));
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
            pstmt.setString(2, t.getDate());
            pstmt.setDouble(3, t.getAmount());
            pstmt.setString(4, t.getExternalIBAN());
            pstmt.setString(5, t.getType().toString());
            pstmt.setInt(6, -1);
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
	public static void updateTransaction(Transaction t, int id, Set<Integer> sessionIds) {
		String sql = "UPDATE transactions SET date = ? , "
                + "amount = ? , "
                + "externalIBAN = ? , "
                + "type = ? "
                + "WHERE id = ? AND id IN ";
		sql = setToSql(sql, sessionIds);
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
        		
        		
        		
            // set the corresponding param
        		pstmt.setString(1, t.getDate());
            pstmt.setDouble(2, t.getAmount());
            pstmt.setString(3, t.getExternalIBAN());
            pstmt.setString(4, t.getType().toString());
            pstmt.setInt(5, id);
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
	public static void deleteTransaction(int id, Set<Integer> sessionIds) {
        String sql = "DELETE FROM transactions WHERE id = ? and id IN ";
        sql = setToSql(sql, sessionIds);
        
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
                + "WHERE id = ?";
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
	public static List<Category> getAllCategories(Set<Integer> sessionIds) {
		List<Category> categories = new ArrayList<>();
		
		String sql = "SELECT * FROM categories WHERE id IN ";
		sql = setToSql(sql, sessionIds);
		try (Connection conn = connect();
	             PreparedStatement pstmt  = conn.prepareStatement(sql)) {
	        
			
			
			ResultSet rs  = pstmt.executeQuery();
			
	        while (rs.next()) {
	            categories.add(new Category(rs.getInt("id"), rs.getString("name")));
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
            pstmt.setInt(1, c.getId());
            pstmt.setString(2, c.getName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}
	
	/**
	 * Gets the category from the database with a specific id and in the given session.
	 * @param id
	 * 			Id of the category
	 * @return
	 * 			Category object from the database
	 */
	public static Category getCategory(int id, Set<Integer> sessionIds) {
		String sql = "SELECT * FROM categories WHERE id == ? AND id IN ";
		sql = setToSql(sql, sessionIds);
		try (Connection conn = connect();
	             PreparedStatement pstmt  = conn.prepareStatement(sql)) {
			
			
	        
			pstmt.setInt(1, id);
	            
	        ResultSet rs  = pstmt.executeQuery();
	            
	        if (rs.next()) {
	            return new Category(rs.getInt("id"), rs.getString("name"));
	        }
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		return null;
	}
	
	/**
	 * Gets the category from the database with a specific id.
	 * @param id
	 * 			Id of the category
	 * @return
	 * 			Category object from the database
	 */
	public static Category getCategory(int id) {
		String sql = "SELECT * FROM categories WHERE id == ?";
		try (Connection conn = connect();
	             PreparedStatement pstmt  = conn.prepareStatement(sql)) {
			pstmt.setInt(1, id);
	            
	        ResultSet rs  = pstmt.executeQuery();
	            
	        if (rs.next()) {
	            return new Category(rs.getInt("id"), rs.getString("name"));
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
	public static void deleteCategory(int id, Set<Integer> sessionIds) {
		String sql = "DELETE FROM categories WHERE id = ? AND id IN ";
		sql = setToSql(sql, sessionIds);
		
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
	public static void updateCategory(Category c, int id, Set<Integer> sessionIds) {
		String sql = "UPDATE categories SET name = ? "
                + "WHERE id = ? AND id IN ";
		sql = setToSql(sql, sessionIds);
 
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
		/*addSession(1);
		addSession(4);
		addTransactionId(1,7);
		addTransactionId(1,10);
		System.out.println(getMaxSessionId());
		System.out.println(validSessionId(5));*/
		//addTransaction(new Transaction(0, "alice", "bob", 15.0, "now", 0));
		//addTransaction(new Transaction(1, "alice", "bob", 15.0, "now + 1", 0));
		//addCategory(new Category(0, "debt"));
		
	}


	public static int getLastTransactionID() {
		return getMaxTransactionIndex();
	}



	public static int getLastCategoryID() {
		return getMaxCategoryIndex();
	}






}
