package nl.utwente.ing.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import nl.utwente.ing.model.CandleStick;
import nl.utwente.ing.model.Category;
import nl.utwente.ing.model.CategoryRule;
import nl.utwente.ing.model.TimeInterval;
import nl.utwente.ing.model.Transaction;
import nl.utwente.ing.model.TransactionType;
import nl.utwente.ing.service.TransactionService;


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
		
		String sql = "SELECT * FROM " + table + " WHERE session = ?";
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
		String sql = "SELECT * FROM sessions as s WHERE s.session = ?";
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
		String sql = "SELECT * FROM transactions WHERE id = ? AND id IN ";
		sql = setToSql(sql, sessionIds);

		try (Connection conn = connect();
	             PreparedStatement pstmt  = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, id);
	        ResultSet rs  = pstmt.executeQuery();
	        
	        List<Transaction> transactions = TransactionService.getTransactions(rs);
	        
	        if (transactions.size() == 1) {
	        	// TODO remove this below
	        		transactions.get(0).returnUnixTimestamp();
	            return transactions.get(0);
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
		String sql = "SELECT * FROM transactions WHERE id = ?";
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
		String sql = "SELECT * FROM categories WHERE id = ?";
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
	        		transactions.add(new Transaction(rs.getInt("id"), rs.getLong("date"),
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
            pstmt.setLong(2, t.returnUnixTimestamp());
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
        		pstmt.setLong(1, t.returnUnixTimestamp());
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
		String sql = "SELECT * FROM categories WHERE id = ? AND id IN ";
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
		String sql = "SELECT * FROM categories WHERE id = ?";
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
		String sql = "SELECT * FROM categoryRules WHERE id = ? AND id IN ";
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
	/**
	 * Gets all transactions from the database that happened in the given interval
	 * @param transactionIds
	 * @param intervalStart
	 * @param interval
	 * @return
	 */
	public static List<Transaction> getTransactionsAtInterval(Set<Integer> transactionIds, ZonedDateTime intervalStart, ChronoUnit interval) {
		ZonedDateTime intervalEnd = intervalStart.plus(1, interval);
		String sql =  "SELECT * FROM transactions WHERE date >= ? AND date < ? AND id IN ";
		sql = setToSql(sql, transactionIds);
		sql += " ORDER BY date ASC;";

		
		try (Connection conn = connect()){
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, intervalStart.toEpochSecond());
			pstmt.setLong(2, intervalEnd.toEpochSecond());
			
			ResultSet rs = pstmt.executeQuery();
			return TransactionService.getTransactions(rs);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
	/**
	 * Splits the session into time intervals and returns these intervals
	 * @param transactionIds
	 * 						The transaction ids associated with this session
	 * @param interval
	 * 						The interval into which the session is split
	 * @return
	 */
	public static List<ZonedDateTime> getTransactionIntervals(ChronoUnit interval, int nrIntervals){
		
		List<ZonedDateTime> result = new ArrayList<>();
		
		
		ZonedDateTime now = Instant.now().atZone(ZoneOffset.UTC);
		ZonedDateTime timeMax = null;
		
		// The code below rounds down to the beginning of the Hour, Day, Week, Month or Year
		if (interval.equals(ChronoUnit.WEEKS)) {		
			// Rounds down to the nearest day
			timeMax = now.truncatedTo(ChronoUnit.DAYS);
			
			// Rounds down to the first day of the week
			timeMax = timeMax.minus(timeMax.getDayOfWeek().getValue() - 1, ChronoUnit.DAYS);
			
		} else if (interval.equals(ChronoUnit.MONTHS)) {
			// Rounds down to the nearest day
			timeMax = now.truncatedTo(ChronoUnit.DAYS);
			
			// Rounds down to the first day of the month
			timeMax = timeMax.withDayOfMonth(1);
			
		} else if (interval.equals(ChronoUnit.YEARS)) {
			// Rounds down to the nearest day
			timeMax = now.truncatedTo(ChronoUnit.DAYS);
			
			// Rounds down to the first day of the month
			timeMax = timeMax.withDayOfMonth(1);
			
			// Rounds down to the first month of the year
			timeMax = timeMax.withMonth(1);
		}
		else {
			timeMax = now.truncatedTo(interval);
		}

		ZonedDateTime timeMin = timeMax.minus(nrIntervals, interval);


		ZonedDateTime timeAtInterval = timeMin;
		for (int i = 0; i < nrIntervals; i++) {
			timeAtInterval = timeAtInterval.plus(1, interval);
			result.add(timeAtInterval);
			}
		return result;
		}
		
		
	/**
	 * Gets the balance history split into intervals of variable size
	 * @param transactionIds
	 * @param time
	 * @param nrIntervals
	 * @return List of candlestick objects containing the necessary interval information
	 */
	public static List<CandleStick> getBalanceHistory(Set<Integer> transactionIds, ChronoUnit time, int nrIntervals){
		List<CandleStick> result = new ArrayList<>();
		// Gets all intervals from now to a given number of intervals back
		List<ZonedDateTime> zdt = getTransactionIntervals(time, nrIntervals);
		// for each interval, get transactions and generate candlestick data points
		for (ZonedDateTime z: zdt) {
			List<Transaction> transactions = getTransactionsAtInterval(transactionIds, z, time);
			double volume = 0;
			double open = getBalanceAtIntervalStart(transactionIds, z);
			double close = open;
			double high = open;
			double low = open;
			for (Transaction t: transactions) {
				if (t.getType().equals(TransactionType.deposit)) {
					close += t.getAmount();
				} else {
					close -= t.getAmount();
				}
				volume+= t.getAmount();
				high = Math.max(close, high);
				low = Math.min(close, low);
			}
			result.add(new CandleStick(open, close, high, low, volume, z.toEpochSecond()));
		}
		
		return result;
	}
	/**
	 * Gets the balance of the account at the start of an interval
	 * @param transactionIds
	 * @param intervalStart
	 * @return Amount of money stored on the account before a given date
	 */
	public static double getBalanceAtIntervalStart(Set<Integer> transactionIds, ZonedDateTime intervalStart) {
		String sql = "SELECT sum(case when type='deposit' then amount else -amount end) as initial FROM transactions WHERE date < ? AND id IN ";
		sql = setToSql(sql, transactionIds);
		try (Connection conn = connect()){
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, intervalStart.toEpochSecond());
			
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getDouble("initial");
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return 0;
		
	}
	
	
/*	public static void fillStatementWithParams(PreparedStatement stmt, Object[] params) {
		for (int i = 0; i < params.length; i++) {
			try {
				stmt.setObject(i + 1, params[i]);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static Object[] runSelectStatement(String sql, Object... params) throws SQLException {
		Connection conn = null;
		Object[] result = null;
		try {
			conn = connect();
			PreparedStatement statement = conn.prepareStatement(sql);
			fillStatementWithParams(statement, params);
			ResultSet rs = statement.executeQuery();
			int i = 0;
			result = new Object[rs.getFetchSize();];
			while(rs.next()) {
				result[i] = rs.get
			}
		} finally {
			conn.close();
		}
	}
	*/



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
