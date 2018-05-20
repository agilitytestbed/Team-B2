package nl.utwente.ing.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import nl.utwente.ing.model.Category;
import nl.utwente.ing.model.CategoryRule;
import nl.utwente.ing.model.Transaction;


public class DatabaseCommunication {
	public static final String FILENAME = "data.db";
	public static final String URL = 
			"jdbc:sqlite:"
			+ FILENAME;
	
	/*
	 * -------------------- Code for handling sessions --------------------
	 */
	
	public static Set<Integer> getIds(int sessionID, String table){
		Set<Integer> ids = new HashSet<>();
		
		String sql = "SELECT * FROM " + table + " WHERE session == ?";
		try (Connection conn = connect();
	             PreparedStatement pstmt  = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, sessionID);
	        ResultSet rs  = pstmt.executeQuery();
	            
	        while (rs.next()) {
		        ids.add(rs.getInt(2));
	        }
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		
		return ids;
	}
	
	public static void addId(int sessionID, int id, String table) {
		String sql = "INSERT INTO " + table +"(session,id) VALUES (?,?)";
		
		try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sessionID);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}
	
	public static void deleteId(int sessionID, int id, String table) {
		String sql = "DELETE FROM " + table + " WHERE id = ? AND session = ?";
		
		try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setInt(2, sessionID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}
	
	public static Set<Integer> getTransactionIds(int sessionID) {
		return getIds(sessionID, "transactionIds");
	}
	
	public static Set<Integer> getCategoryIds(int sessionID) {
		return getIds(sessionID, "categoryIds");
	}
	
	public static Set<Integer> getCategoryRuleIds(int sessionID){
		return getIds(sessionID, "categoryRuleIds");
	}
	
	public static void addTransactionId(int sessionID, int id) {
		addId(sessionID, id, "transactionIds");
	}
	
	public static void addCategoryId(int sessionID, int id) {
		addId(sessionID, id, "categoryIds");
	}
	
	public static void addCategoryRuleId(int sessionID, int id){
		addId(sessionID, id , "categoryRuleIds");
	}
	
	public static void addSession(int sessionID) {
		
		String sql = "INSERT INTO sessions(session) VALUES(?)";
		try (Connection conn = connect();
	             PreparedStatement pstmt  = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, sessionID);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		
	}
	
	public static int getMaxSessionId() {
		String sql = "SELECT max(session) FROM sessions";
		 try (Connection conn = connect();
	             Statement stmt  = conn.createStatement();
	             ResultSet rs    = stmt.executeQuery(sql)){
	            
	            if (rs.next()) {
	                return rs.getInt("max(session)");
	            }
	        } catch (SQLException e) {
	            System.out.println(e.getMessage());
	        }
		 return -1;
	}
	
	public static boolean validSessionId(int sessionID) {
		String sql = "SELECT * FROM sessions WHERE session == ?";
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
		deleteId(sessionID, id, "transactionIds");
	}
	
	public static void deleteCategoryId(int sessionID, int id) {
		deleteId(sessionID, id, "categoryIds");
	}
	
	public static void deleteCategoryRuleId(int sessionID, int id) {
		deleteId(sessionID, id, "categoryRuleIds");
	}
	
	/*
	 * -------------------- Code for normal data --------------------
	 */
	
	/**
	 * Queries the database for the largest index of the given table.
	 * @return
	 * 		int representing the largest index or -1 if there are no entries
	 */
	private static int getMaxIndex(String table) {
		String sql = "SELECT  id\n" + 
				"FROM    " + table + " mo\n" + 
				"WHERE   NOT EXISTS\n" + 
				"        (\n" + 
				"        SELECT  NULL\n" + 
				"        FROM    " + table + " mi \n" + 
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
	public static Connection connect() {
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
		String sql = "INSERT INTO transactions VALUES(?,?,?,?,?,?,?)";
		
		try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, t.getId());
            pstmt.setString(2, t.getDate());
            pstmt.setDouble(3, t.getAmount());
            pstmt.setString(4, t.getDescription());
            pstmt.setString(5, t.getExternalIBAN());
            pstmt.setString(6, t.getType().toString());
            pstmt.setInt(7, t.CategoryID());
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
                + "type = ? , "
                + "description = ? "
                + "WHERE id = ? AND id IN ";
		sql = setToSql(sql, sessionIds);
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
        		
        		
        		
            // set the corresponding param
        		pstmt.setString(1, t.getDate());
            pstmt.setDouble(2, t.getAmount());
            pstmt.setString(3, t.getExternalIBAN());
            pstmt.setString(4, t.getType().toString());
            pstmt.setString(5, t.getDescription());
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
	
	
	/**
	 * Gets all category rules from the database.
	 * @return
	 * 			List of category rules
	 */
	public static List<CategoryRule> getAllCategoryRules(Set<Integer> sessionIds) {
		List<CategoryRule> categoryRules = new ArrayList<>();
		
		String sql = "SELECT * FROM categoryRules WHERE id IN ";
		sql = setToSql(sql, sessionIds);
		try (Connection conn = connect();
	             PreparedStatement pstmt  = conn.prepareStatement(sql)) {
	        
			
			
			ResultSet rs  = pstmt.executeQuery();
			
	        while (rs.next()) {
	            categoryRules.add(new CategoryRule(rs.getInt("id"), 
	            		rs.getString("description"), rs.getString("iBAN"),
	            		rs.getString("type"), rs.getInt("category_id"), rs.getBoolean("applyOnHistory")
	            		));
	        }
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		return categoryRules;
	}
	
	public static void applyCategoryRuleOnHistory(CategoryRule cr, Set<Integer> sessionIds) {
		String sql = "UPDATE transactions SET categoryID = ? WHERE description LIKE ? AND externalIBAN LIKE ? AND type = ? AND id IN ";
		sql = setToSql(sql, sessionIds);
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
        	
        		
            
        		// set the corresponding param
            pstmt.setInt(1, cr.getCategory_id());
            pstmt.setString(2, "%" + cr.getDescription() + "%");
            pstmt.setString(3, "%" + cr.getiBAN() + "%");
            pstmt.setString(4, cr.getType().toString());
            // update 
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}
	
	/**
	 * Adds the given category rule object to the database.
	 * @param c
	 * 			Category rule object
	 */
	public static void addCategoryRule(CategoryRule c) {
		String sql = "INSERT INTO categoryRules VALUES(?,?,?,?,?,?)";
		
		try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, c.getId());
            pstmt.setString(2, c.getDescription());
            pstmt.setString(3, c.getiBAN());
            pstmt.setString(4, c.getType().toString());
            pstmt.setInt(5, c.getCategory_id());
            pstmt.setInt(6, c.isApplyOnHistory() ? 1 : 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}
	
	/**
	 * Gets the category rule from the database with a specific id.
	 * @param id
	 * 			Id of the category id
	 * @return
	 * 			CategoryRule object from the database
	 */
	public static CategoryRule getCategoryRule(int id, Set<Integer> sessionIds) {
		String sql = "SELECT * FROM categoryRules WHERE id == ? AND id IN ";
		sql = setToSql(sql, sessionIds);
		try (Connection conn = connect();
	             PreparedStatement pstmt  = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, id);
	        ResultSet rs  = pstmt.executeQuery();
	            
	        if (rs.next()) {
	            return new CategoryRule(rs.getInt("id"), rs.getString("description"),
	            		rs.getString("iBAN"), rs.getString("type"), rs.getInt("category_id"),
	            		rs.getBoolean("applyOnHistory")
	            		);
	        }
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		return null;
	}
	
	/**
	 * Updates the category rule with the given id 
	 * @param t
	 * 			The updated category rule
	 * @param id
	 * 			Id of the category rule
	 */
	public static void updateCategoryRule(CategoryRule c, int id, Set<Integer> sessionIds) {
		String sql = "UPDATE categoryRules SET description = ?, iBAN = ?, type = ?, category_id = ? "
                + "WHERE id = ? AND id IN ";
		sql = setToSql(sql, sessionIds);
 
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
        	
        		
            
        		// set the corresponding param
            pstmt.setString(1, c.getDescription());
            pstmt.setString(2, c.getiBAN());
            pstmt.setString(3, c.getType().toString());
            pstmt.setInt(4, c.getCategory_id());
            pstmt.setInt(5, id);
            // update 
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}
	
	/**
	 * Deletes the category rule with the given id
	 * @param id
	 * 			The id of the category rule to delete
	 */
	public static void deleteCategoryRule(int id, Set<Integer> sessionIds) {
		String sql = "DELETE FROM categoryRules WHERE id = ? AND id IN ";
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
	
	public static void applyCategoryRule(Transaction t, Set<Integer> sessionIds) {
		List<CategoryRule> categoryRules = getAllCategoryRules(sessionIds);
		ListIterator<CategoryRule> li = categoryRules.listIterator();
		// Iterate the list in reverse ( oldest rule first )
		while (li.hasNext()) {
			CategoryRule cr = li.next();
			if (t.getDescription().contains(cr.getDescription()) &&
				t.getExternalIBAN().contains(cr.getiBAN())   	&&
				t.getType().equals(cr.getType())					&&
				getCategory(cr.getCategory_id())!= null) {
				
				t.setCategory(getCategory(cr.getCategory_id()));
				return;
			}
		}
	}
	
	



	public static int getLastTransactionID() {
		return getMaxIndex("transactions");
	}



	public static int getLastCategoryID() {
		return getMaxIndex("categories");
	}
	
	public static int getLastCategoryRuleID() {
		return getMaxIndex("categoryRules");
	}






}
