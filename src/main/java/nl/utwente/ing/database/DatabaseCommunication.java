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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import nl.utwente.ing.model.CandleStick;
import nl.utwente.ing.model.Category;
import nl.utwente.ing.model.CategoryRule;
import nl.utwente.ing.model.Message;
import nl.utwente.ing.model.MessageType;
import nl.utwente.ing.model.PaymentRequest;
import nl.utwente.ing.model.SavingGoal;
import nl.utwente.ing.model.Transaction;
import nl.utwente.ing.model.TransactionType;
import nl.utwente.ing.service.CategoryRuleService;
import nl.utwente.ing.service.CategoryService;
import nl.utwente.ing.service.MessageService;
import nl.utwente.ing.service.PaymentRequestService;
import nl.utwente.ing.service.SavingGoalService;
import nl.utwente.ing.service.TransactionService;


public class DatabaseCommunication {
	public static final String FILENAME = "data.db";
	public static final String URL = 
			"jdbc:sqlite:"
			+ FILENAME;
	
	/*
	 * -------------- Code for handling internal transactions --------------
	 */
	
	public static void addInternalTransactionId(int savingGoalId, int transactionId) {
		String sql = "INSERT INTO internalTransactions (savingGoalId, transactionId) VALUES (?,?)";
		
		runPreparedStatementUpdate(sql, savingGoalId, transactionId);
	}
	public static void addInternalTransactionIdNoSavingGoal(int transactionId) {
		String sql = "INSERT INTO internalTransactions (transactionId) VALUES (?)";
		
		runPreparedStatementUpdate(sql, transactionId);
	}
	
	public static void deleteInternalTransactionId(int savingGoalId, int transactionId, int sessionId) {
		String sql = "DELETE FROM internalTransactions WHERE savingGoalId = ? AND transactionId = ?"
				+ " AND savingGoalId IN (SELECT id FROM savingGoalIds WHERE session = ?)";
		
		runPreparedStatementUpdate(sql, savingGoalId, transactionId);
	}
	
	/*
	 * -------------------- Code for handling payment request transactions --------------------
	 */
	public static void addPaymentRequestTransaction(int paymentRequestId, int transactionId) {
		String sql = "INSERT INTO paymentRequestTransactions (paymentRequestId, transactionId) VALUES (?,?)";
		
		runPreparedStatementUpdate(sql, paymentRequestId, transactionId);
	}
	
	/*
	 * -------------------- Code for handling sessions --------------------
	 */
	
	public static void addId(int sessionID, int id, String table) {
		String sql = "INSERT INTO " + table +"(session,id) VALUES (?,?)";
		
		runPreparedStatementUpdate( sql, sessionID, id);
	}
	
	
	public static void deleteId(int sessionID, int id, String table) {
		String sql = "DELETE FROM " + table + " WHERE id = ? AND session = ?";
		
		runPreparedStatementUpdate( sql, id, sessionID);
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
	
	public static void addSavingGoalId(int sessionID, int id){
		addId(sessionID, id , "savingGoalIds");
	}
	
	public static void addPaymentRequestId(int sessionID, int id){
		addId(sessionID, id , "paymentRequestIds");
	}
	
	public static void addMessageId(int sessionID, int id){
		addId(sessionID, id , "messageIds");
	}
	
	public static void addSession(int sessionID) {
		String sql = "INSERT INTO sessions(session) VALUES(?)";
		runPreparedStatementUpdate( sql, sessionID);
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
		try (Connection conn = connect()) {
	        ResultSet rs  = runPreparedStatementQuery(conn, sql, sessionID);
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
	
	public static void deleteSavingGoalId(int sessionID, int id) {
		deleteId(sessionID, id, "savingGoalIds");
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
		try(Connection conn = connect();
				Statement stmt = conn.createStatement()) {
	            ResultSet rs = stmt.executeQuery(sql);
	            if (rs.next()) {
	            		return rs.getInt("id");
	            }
	        } catch (SQLException e) {
	            System.out.println(e.getMessage());
	        }
		return -1;
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
            conn.createStatement().executeUpdate("PRAGMA foreign_keys = ON;");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

	/**
	 * Takes the given sql query and executes it.
	 * @param sql
	 * 			The sql message as a string
	 * @return resultset containing the result of the query
	 */
	public static ResultSet executeSQLQuery(String sql) {
		try (Connection conn = connect(); 
				Statement stmt = conn.createStatement()) {
			return stmt.executeQuery(sql);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
	
	/**
	 * Takes the given sql update and executes it.
	 * @param sql
	 * 			The sql message as a string
	 */
	public static void runSQLUpdate(String sql) {
		try (Connection conn = connect(); 
				Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(sql);
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
	public static Transaction getTransaction(int id, int sessionId) {
		String sql = "SELECT * FROM transactions WHERE id = ? AND id IN (SELECT id FROM transactionIds WHERE session = ?)" + 
					 " AND id NOT IN (SELECT transactionId FROM internalTransactions)";

		try (Connection conn = connect();
	             PreparedStatement pstmt  = conn.prepareStatement(sql)) {
	        List<Transaction> transactions = TransactionService.getTransactions(runPreparedStatementQuery(conn, sql, id, sessionId));
	        
	        if (transactions.size() == 1) {
	            return transactions.get(0);
	        }
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		return null;
	}
	
	/**
	 * Gets the last transaction from the database
	 * @param id
	 * 			Id of the transaction
	 * @return
	 * 			Transaction object from the database
	 */
	public static Transaction getLastTransaction(int sessionId) {
		String sql = "SELECT * FROM transactions WHERE id IN (SELECT id FROM transactionIds WHERE session = ?)  ORDER BY date DESC LIMIT 1";

		try (Connection conn = connect()) {
	        ResultSet rs  = runPreparedStatementQuery(conn, sql, sessionId);
	        
	        List<Transaction> transactions = TransactionService.getTransactions(rs);
	        
	        if (transactions.size() == 1) {
	            return transactions.get(0);
	        }
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		return null;
	}
	
	/**
	 * Gets the first transaction from the database
	 * @param id
	 * 			Id of the transaction
	 * @return
	 * 			Transaction object from the database
	 */
	public static Transaction getFirstTransaction(int sessionId) {
		String sql = "SELECT * FROM transactions WHERE id IN (SELECT id FROM transactionIds WHERE session = ?)  ORDER BY date ASC LIMIT 1";

		try (Connection conn = connect()) {
	        ResultSet rs  = runPreparedStatementQuery(conn, sql, sessionId);
	        
	        List<Transaction> transactions = TransactionService.getTransactions(rs);
	        
	        if (transactions.size() == 1) {
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
		String sql = "SELECT * FROM transactions WHERE id = ? AND id NOT IN (SELECT id FROM internalTransactions)";
		try (Connection conn = connect()) {
	            
	        ResultSet rs  = runPreparedStatementQuery(conn, sql, id);
	            
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
		try (Connection conn = connect()) {            
	        ResultSet rs  = runPreparedStatementQuery(conn, sql, id);
	            
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
	public static List<Transaction> getAllExternalTransactions(int offset, int limit,
			int categoryID, int sessionId) {
		List<Transaction> transactions = new ArrayList<>();
		
		
		String sql = "SELECT * FROM transactions WHERE id IN (SELECT id FROM transactionIds WHERE session = ?)" + 
				 " AND id NOT IN (SELECT transactionId FROM internalTransactions)";
		
		if (categoryID != -1) {
			sql += " AND categoryID = ?";
		}
		
		sql += " LIMIT ?,?";
		try (Connection conn = connect()) {
			ResultSet rs;
			
			if (categoryID != -1) {
				rs = runPreparedStatementQuery(conn, sql, sessionId, categoryID, offset, limit);
			} else {
				rs = runPreparedStatementQuery(conn, sql, sessionId, offset, limit);
			}
				
	        	transactions = TransactionService.getTransactions(rs);
	        return transactions;
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		return null;
		
	}
	
	public static List<Transaction> getAllTransactions(int sessionId){
		String sql = "SELECT * FROM transactions WHERE id IN (SELECT id FROM transactionIds WHERE session = ?)";
		
		try (Connection conn = connect()){
			return TransactionService.getTransactions(runPreparedStatementQuery(conn, sql, sessionId));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	

	
	
	/**
	 * Adds a transaction object to the database.
	 * @param t
	 * 			Transaction object
	 */
	public static Transaction addTransaction(Transaction t, int sessionId) {
		
		// Generate new id
		int newId = DatabaseCommunication.getLastTransactionID() + 1;
		t.setId(newId);
		
		// Apply category rule
		DatabaseCommunication.applyCategoryRule(t, sessionId);
		
		
		
		String sql = "INSERT INTO transactions(id, date, amount, description, externalIBAN, type) VALUES(?,?,?,?,?,?)";
		
		
        runPreparedStatementUpdate(sql, t.getId(), t.returnUnixTimestamp(), t.getAmount(), t.getDescription(),
            	t.getExternalIBAN(), t.getType().toString());
        
        // Apply saving goals
     	DatabaseCommunication.applySavingGoals(t, sessionId);
        
        // Apply payment requests
     	applyPaymentRequests(t, sessionId);
        
        DatabaseCommunication.addTransactionId(sessionId, t.getId());
        
        // Check if the balance goes negative
        applyMessages(sessionId, t.returnUnixTimestamp());
        
        return t;
	}
	
	/**
	 * Adds an internal transaction object to the database.
	 * @param t
	 * 			Transaction object
	 */
	public static Transaction addInternalTransaction(double amount, long unixTimestamp, int sessionId, int savingGoalId) {
		
		Transaction t = new Transaction(unixTimestamp, amount);
		
		// Generate new id
		int newId = DatabaseCommunication.getLastTransactionID() + 1;
		t.setId(newId);
		
		
		String sql = "INSERT INTO transactions(id, date, amount, description, externalIBAN, type) VALUES(?,?,?,?,?,?)";
	
        runPreparedStatementUpdate(sql, t.getId(), t.returnUnixTimestamp(), t.getAmount(), t.getDescription(),
            	t.getExternalIBAN(), t.getType().toString());
        
        DatabaseCommunication.addTransactionId(sessionId, t.getId());
        DatabaseCommunication.addInternalTransactionId(savingGoalId, t.getId());
  
        
        return t;
	}
	
	public static Transaction addInternalTransactionReturnMoney(double amount, long unixTimestamp, int sessionId) {
		
		Transaction t = new Transaction(unixTimestamp, amount);
		
		// Generate new id
		int newId = DatabaseCommunication.getLastTransactionID() + 1;
		t.setId(newId);
		
		
		String sql = "INSERT INTO transactions(id, date, amount, description, externalIBAN, type) VALUES(?,?,?,?,?,'deposit')";
	
        runPreparedStatementUpdate(sql, t.getId(), t.returnUnixTimestamp(), t.getAmount(), t.getDescription(),
            	t.getExternalIBAN());
        
        DatabaseCommunication.addTransactionId(sessionId, t.getId());
        DatabaseCommunication.addInternalTransactionIdNoSavingGoal(t.getId());
  
        
        return t;
	}
	
	
	/**
	 * Updates the transaction with the given id 
	 * @param t
	 * 			The updated transaction
	 * @param id
	 * 			Id of the transaction
	 */
	public static void updateTransaction(Transaction t, int id, int sessionId) {
		String sql = "UPDATE transactions SET date = ? , "
                + "amount = ? , "
                + "externalIBAN = ? , "
                + "type = ? , "
                + "description = ? "
                + "WHERE id = ? AND id IN (SELECT id FROM transactionIds WHERE session = ?)";
        	runPreparedStatementUpdate( sql, t.returnUnixTimestamp(), t.getAmount(), t.getExternalIBAN(), t.getType().toString(),
        		t.getDescription(), id, sessionId);  
	}
	
	/**
	 * Deletes the transaction with the given id
	 * @param id
	 * 			The id of the transaction to delete
	 */
	public static void deleteTransaction(int id, int sessionId) {
        String sql = "DELETE FROM transactions WHERE id = ? and id IN (SELECT id FROM transactionIds WHERE session = ?) "
        		+ "AND id NOT IN (SELECT transactionId FROM internalTransactions)";
	    runPreparedStatementUpdate(sql, id, sessionId); 

	}
	
	public static void assignCategory(int categoryID, int transactionID) {
		String sql = "UPDATE transactions SET categoryID = ?"
                + "WHERE id = ?";
        runPreparedStatementUpdate(sql, categoryID, transactionID);  
	}
	
	/**
	 * Gets all categories from the database.
	 * @return
	 * 			List of categories
	 */
	public static List<Category> getAllCategories(int sessionId) {
		List<Category> categories = new ArrayList<>();
		
		String sql = "SELECT * FROM categories WHERE id IN (SELECT id FROM categoryIds WHERE session = ?)";
		try (Connection conn = connect()) {
			ResultSet rs  = runPreparedStatementQuery(conn, sql, sessionId);  
			
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
	public static Category addCategory(Category c, int sessionId) {
		
		// Generate new id
		int newId = DatabaseCommunication.getLastCategoryID() + 1;
		c.setId(newId);
		
		String sql = "INSERT INTO categories VALUES(?,?)";
        runPreparedStatementUpdate(sql, c.getId(), c.getName());
        
        addCategoryId(sessionId, c.getId());
        
        return c;
	}
	
	/**
	 * Gets the category from the database with a specific id and in the given session.
	 * @param id
	 * 			Id of the category
	 * @return
	 * 			Category object from the database
	 */
	public static Category getCategory(int id, int sessionId) {
		String sql = "SELECT * FROM categories WHERE id = ? AND id IN (SELECT id FROM categoryIds WHERE session = ?)";
		try (Connection conn = connect()) {
	        ResultSet rs  = runPreparedStatementQuery(conn, sql, id, sessionId);
	            
	        return CategoryService.getCategory(rs);
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
		try (Connection conn = connect()) {
	            
	        ResultSet rs  = runPreparedStatementQuery(conn, sql, id);
	            
	        return CategoryService.getCategory(rs);
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
	public static void deleteCategory(int id, int sessionId) {
		String sql = "DELETE FROM categories WHERE id = ? AND id IN (SELECT id FROM categoryIds WHERE session = ?)";
        runPreparedStatementUpdate( sql, id, sessionId);
        
        
	}
	
	/**
	 * Updates the category with the given id 
	 * @param t
	 * 			The updated category
	 * @param id
	 * 			Id of the category
	 */
	public static void updateCategory(Category c, int id, int sessionId) {
		String sql = "UPDATE categories SET name = ? "
                + "WHERE id = ? AND id IN (SELECT id FROM categoryIds WHERE session = ?)";
        runPreparedStatementUpdate( sql, c.getName(), id, sessionId);
	}
	
	
	/**
	 * Gets all category rules from the database.
	 * @return
	 * 			List of category rules
	 */
	public static List<CategoryRule> getAllCategoryRules(int sessionId) {
		List<CategoryRule> categoryRules = new ArrayList<>();
		String sql = "SELECT * FROM categoryRules WHERE id IN (SELECT id FROM categoryRuleIds WHERE session = ?)";
		try (Connection conn = connect()) {
			
			ResultSet rs  = runPreparedStatementQuery(conn, sql, sessionId);
			
	        categoryRules =  CategoryRuleService.getCategoryRules(rs);
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		return categoryRules;
	}
	
	public static void applyCategoryRuleOnHistory(CategoryRule cr, int sessionId) {
		String sql = "UPDATE transactions SET categoryID = ? WHERE description LIKE ? AND externalIBAN LIKE ? AND type = ? AND id IN (SELECT id FROM transactionIds WHERE session = ?)";
        runPreparedStatementUpdate( sql, cr.getCategory_id(), "%" + cr.getDescription() + "%",
            	"%" + cr.getiBAN() + "%", cr.getType().toString(), sessionId);
	}
	
	/**
	 * Adds the given category rule object to the database.
	 * @param c
	 * 			Category rule object
	 */
	public static CategoryRule addCategoryRule(CategoryRule c, int sessionId) {
		
		// Generate new id
		int newId = DatabaseCommunication.getLastCategoryRuleID() + 1;
		c.setId(newId);
		
		String sql = "INSERT INTO categoryRules VALUES(?,?,?,?,?,?)";
        runPreparedStatementUpdate(sql, c.getId(), c.getDescription(),
            	c.getiBAN(), c.getType().toString(), c.getCategory_id(), c.isApplyOnHistory() ? 1 : 0);
        
        DatabaseCommunication.addCategoryRuleId(sessionId, c.getId());
        
        return c;
	}
	
	/**
	 * Gets the category rule from the database with a specific id.
	 * @param id
	 * 			Id of the category id
	 * @return
	 * 			CategoryRule object from the database
	 */
	public static CategoryRule getCategoryRule(int id, int sessionId) {
		String sql = "SELECT * FROM categoryRules WHERE id = ? AND id IN (SELECT id FROM categoryRuleIds WHERE session = ?)";
		try (Connection conn = connect()) {
	        ResultSet rs  = runPreparedStatementQuery(conn, sql, id, sessionId);
	            
	        return CategoryRuleService.getCategoryRule(rs);
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
	public static void updateCategoryRule(CategoryRule c, int id, int sessionId) {
		String sql = "UPDATE categoryRules SET description = ?, iBAN = ?, type = ?, category_id = ? "
                + "WHERE id = ? AND id IN (SELECT id FROM categoryRuleIds WHERE session = ?)";

        runPreparedStatementUpdate(sql, c.getDescription(), c.getiBAN(),
            c.getType().toString(), c.getCategory_id(), id, sessionId);
	}
	
	/**
	 * Deletes the category rule with the given id
	 * @param id
	 * 			The id of the category rule to delete
	 */
	public static void deleteCategoryRule(int id, int sessionId) {
		String sql = "DELETE FROM categoryRules WHERE id = ? AND id IN (SELECT id FROM categoryRuleIds WHERE session = ?)";
        runPreparedStatementUpdate(sql, id, sessionId);
        
	}
	
	public static void applyCategoryRule(Transaction t, int sessionId) {
		List<CategoryRule> categoryRules = getAllCategoryRules(sessionId);
		ListIterator<CategoryRule> li = categoryRules.listIterator();

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
	public static List<Transaction> getAllTransactionsAtInterval(int sessionId, ZonedDateTime intervalStart, ChronoUnit interval) {
		ZonedDateTime intervalEnd = intervalStart.plus(1, interval);
		String sql =  "SELECT * FROM transactions WHERE date >= ? AND date < ? AND id IN (SELECT id FROM transactionIds WHERE session = ?)";
		sql += " ORDER BY date ASC;";

		
		try (Connection conn = connect()){
			ResultSet rs = runPreparedStatementQuery(conn, sql, intervalStart.toEpochSecond(), intervalEnd.toEpochSecond(), sessionId);
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
	public static List<CandleStick> getBalanceHistory(int sessionId, ChronoUnit time, int nrIntervals){
		List<CandleStick> result = new ArrayList<>();
		// Gets all intervals from now to a given number of intervals back
		List<ZonedDateTime> zdt = getTransactionIntervals(time, nrIntervals);
		// for each interval, get transactions and generate candlestick data points
		for (ZonedDateTime z: zdt) {
			List<Transaction> transactions = getAllTransactionsAtInterval(sessionId, z, time);
			double open = getBalanceAtIntervalStart(sessionId, z);
			result.add(getCandlestick(transactions, open, z));
		}
		
		return result;
	}
	
	public static CandleStick getCandlestick(List<Transaction> transactions, double open, ZonedDateTime z) {
		double close = open;
		double volume = 0;
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
		if (z == null) {
			z = ZonedDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
		}
		return new CandleStick(open, close, high, low, volume, z.toEpochSecond());
	}
	/**
	 * Gets the balance of the account at the start of an interval
	 * @param transactionIds
	 * @param intervalStart
	 * @return Amount of money stored on the account before a given date
	 */
	public static double getBalanceAtIntervalStart(int sessionId, ZonedDateTime intervalStart) {
		String sql = "SELECT sum(case when type='deposit' then amount else -amount end) as initial FROM transactions WHERE date < ? AND id IN (SELECT id FROM transactionIds WHERE session = ?)";
		try (Connection conn = connect()){
			ResultSet rs = runPreparedStatementQuery(conn, sql, intervalStart.toEpochSecond(), sessionId);
			if (rs.next()) {
				return rs.getDouble("initial");
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return 0;
		
	}
	
	/**
	 * Gets the balance of the account at the current moment in time based on all previous
	 * transactions
	 * @param transactionIds
	 * @return Amount of money stored on the account
	 */
	public static double getBalance(int sessionId) {
		String sql = "SELECT (sum(case when type='deposit' "
				+ "then amount else -amount end)) as balance FROM transactions "
				+ "WHERE id IN (SELECT id FROM transactionIds WHERE session = ?)";
		try (Connection conn = connect()){
			
			ResultSet rs = runPreparedStatementQuery(conn, sql, sessionId);
			if (rs.next()) {
				return rs.getDouble("balance");
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return 0;
		
	}
	
	public static List<SavingGoal> getAllSavingGoals(int sessionId){
		String sql = "SELECT * FROM savingGoals WHERE id IN (SELECT id FROM savingGoalIds WHERE session = ?)";

		try (Connection conn = connect()) {
	        ResultSet rs  = runPreparedStatementQuery(conn, sql, sessionId);
	        return SavingGoalService.getSavingGoals(rs);
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		return null;
	}
	
	/**
	 * Adds the given saving goal object to the database.
	 * @param c
	 * 			Saving goal object
	 */
	public static SavingGoal addSavingGoal(SavingGoal sg, int sessionId) {
		
		// Generate new id
		int newId = DatabaseCommunication.getLastSavingGoalID() + 1;
		sg.setId(newId);
		
		String sql = "INSERT INTO savingGoals VALUES(?,?,?,?,?,?)";
        runPreparedStatementUpdate(sql, sg.getId(), sg.getName(), sg.getGoal(), sg.getSavePerMonth(), sg.getMinBalanceRequired(), 0.0);
        
        DatabaseCommunication.addSavingGoalId(sessionId, sg.getId());
        
        return sg;
	}
	
	/**
	 * Gets the saving goal from the database with a specific id.
	 * @param id
	 * 			Id of the saving goal id
	 * @return
	 * 			SavingGoal object from the database
	 */
	public static SavingGoal getSavingGoal(int id, int sessionId) {
		String sql = "SELECT * FROM savingGoals WHERE id = ? AND id IN (SELECT id FROM savingGoalIds WHERE session = ?)";
		try (Connection conn = connect()) {
	        ResultSet rs  = runPreparedStatementQuery(conn, sql, id, sessionId);
	            
	        List<SavingGoal> goals = SavingGoalService.getSavingGoals(rs);
	        
	        if (goals.size() == 1) {
	        		return goals.get(0);
	        }
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		return null;
	}
	
	/**
	 * Deletes the saving goal with the given id
	 * @param id
	 * 			The id of the saving goal to delete
	 */
	public static void deleteSavingGoal(int id, int sessionId) {
		returnMoney(id, sessionId);
		
		String sql = "DELETE FROM savingGoals WHERE id = ? AND id IN (SELECT id FROM savingGoalIds WHERE session = ?)";
        runPreparedStatementUpdate(sql, id, sessionId);
        

	}
	
	/**
	 * Returns the money for all internal transactions in a saving goal.
	 * @param savingGoalId id of the saving goal the internal transactions belong to
	 * @param sessionId id of the session to which the transactions belong
	 */
	public static void returnMoney(int savingGoalId, int sessionId) {
		List<Transaction> internalTransactions = new ArrayList<>();
		Instant now = Instant.now();
		String sql = "SELECT * FROM transactions WHERE id IN "
				+ " (SELECT transactionId FROM internalTransactions WHERE savingGoalId = ?)";
		try (Connection conn = connect()){
			internalTransactions = TransactionService.getTransactions(runPreparedStatementQuery(conn, sql, savingGoalId));
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		for (Transaction t: internalTransactions) {
			addInternalTransactionReturnMoney(t.getAmount(), now.getEpochSecond(), sessionId);
		}
	}
	
	
	/**
	 * Updates the saving goal balance with the given id 
	 * @param t
	 * 			The updated saving goal
	 * @param id
	 * 			Id of the saving goal
	 */
	public static void updateSavingGoalBalance(SavingGoal sg, int id, int sessionId) {
		String sql = "UPDATE savingGoals SET balance = ? "
                + "WHERE id = ? AND id IN (SELECT id FROM savingGoalIds WHERE session = ?)";
        runPreparedStatementUpdate(sql, sg.getBalance(), id, sessionId);
	}
	
	
	public static void applySavingGoals(Transaction newTransaction, int sessionId) {
		Transaction lastTransaction = getLastTransaction(sessionId);
		List<ZonedDateTime> crossings = new ArrayList<>();
		int diff = 0;
		
		if (lastTransaction != null) {
			ZonedDateTime lastTransactionTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(lastTransaction.returnUnixTimestamp()), ZoneOffset.UTC);
			ZonedDateTime newTransactionTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(newTransaction.returnUnixTimestamp()), ZoneOffset.UTC);
			
			// The number of "month crossings" between the last and newest transaction
			diff = Math.max((newTransactionTime.getYear() - lastTransactionTime.getYear()) * 12 +  
					   (newTransactionTime.getMonthValue() - lastTransactionTime.getMonthValue()), 0);
			
			ZonedDateTime lastMonthBeginning = newTransactionTime.truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1);
			
			for (int i = 0; i < diff; i++) {
				crossings.add(lastMonthBeginning.minus(i, ChronoUnit.MONTHS));
			}
			
		}
		
		double accountBalance = getBalance(sessionId);

		
		List<SavingGoal> savingGoals = getAllSavingGoals(sessionId);
		// For every beginning of the month go over the saving goals
		for (int i = 0; i < diff; i++) {
			if (savingGoals == null) {
				savingGoals = new ArrayList<>();
			}
			// Go through every saving goal and try to apply it
			for (SavingGoal sg : savingGoals) {
				double sgBalance = sg.getBalance();
				// Get the amount that needs to be added to the goal for the month
				double perMonth = Math.min(sg.getSavePerMonth(), sg.getGoal() - sgBalance);

				// There should be enough money on the account for putting aside and the goal must be still unmet
				if (accountBalance >= sg.getMinBalanceRequired() && accountBalance >= perMonth && sgBalance <= sg.getGoal() &&
						perMonth > 0) {
					
					// Add an internal transaction to put money aside for the goal
					addInternalTransaction(perMonth, crossings.get(i).toEpochSecond(), sessionId, sg.getId());
					
					// If the saving goal is met, add a message
					if (sgBalance + perMonth == sg.getGoal()) {
						String msg = "Saving goal with id " + sg.getId() + " has been filled!";
						addMessage(msg, MessageType.info, newTransaction.returnUnixTimestamp(), sessionId);
					}
					

					sg.setBalance(sgBalance + perMonth);
					updateSavingGoalBalance(sg, sg.getId(), sessionId);
					accountBalance -= perMonth;
				}
			}
		}
	}
	
	public static List<PaymentRequest> getAllPaymentRequests(int sessionId){
		String sql = "SELECT * FROM paymentRequests WHERE id IN (SELECT id FROM paymentRequestIds WHERE session = ?)";

		try (Connection conn = connect()) {
	        ResultSet rs  = runPreparedStatementQuery(conn, sql, sessionId);
	        return PaymentRequestService.getPaymentRequests(rs);
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		return null;
	}
	
	public static List<PaymentRequest> getAllUnfilledAndExpiredPaymentRequests(int sessionId, long unixTimestamp){
		String sql = "SELECT * FROM paymentRequests WHERE id IN (SELECT id FROM paymentRequestIds WHERE session = ?) AND due_date < ? AND filled = 0";
		
		try(Connection conn = connect()){
			return PaymentRequestService.getPaymentRequests(runPreparedStatementQuery(conn, sql, sessionId, unixTimestamp));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static List<Transaction> getTransactionsForPaymentRequest(int paymentRequestId){
		String sql = "SELECT * FROM transactions WHERE id IN (SELECT transactionId FROM paymentRequestTransactions WHERE paymentRequestId = ?)";
		try (Connection conn = connect()) {
	        ResultSet rs  = runPreparedStatementQuery(conn, sql, paymentRequestId);
	        return TransactionService.getTransactions(rs);
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		return null;
	}
	
	/**
	 * Adds the given payment request object to the database.
	 * @param sg
	 * 			PaymentRequest object
	 */
	public static PaymentRequest addPaymentRequest(PaymentRequest pr, int sessionId) {
		
		// Generate new id
		int newId = DatabaseCommunication.getLastPaymentRequestID() + 1;
		pr.setId(newId);
		
		String sql = "INSERT INTO paymentRequests(id, description, due_date, amount, number_of_requests) VALUES(?,?,?,?,?)";
        runPreparedStatementUpdate(sql, pr.getId(), pr.getDescription(),
        		pr.returnUnixTimestamp(), pr.getAmount(), pr.getNumber_of_requests());
        
        addPaymentRequestId(sessionId, pr.getId());
        
        return pr;
	}
	
	/**
	 * Go through every payment request and check if it is applicable ( i.e. the amount is right, is not filled and is
	 * the transaction is before the due date )
	 * @param t Transaction that is to be added
	 * @param sessionId
	 */
	public static void applyPaymentRequests(Transaction t , int sessionId) {
		List<PaymentRequest> prList = getAllPaymentRequests(sessionId);
		
		for (PaymentRequest pr: prList) {
			if (t.getType().equals(TransactionType.deposit) && !pr.isFilled() && t.getAmount() == pr.getAmount() && t.returnUnixTimestamp() <= pr.returnUnixTimestamp()) {
				if (pr.transactionNumber() + 1 == pr.getNumber_of_requests()) {
					fillPaymentRequest(pr.getId());
					String msg = "Payment request with id " + pr.getId() + " filled!";
					addMessage(msg, MessageType.info, t.returnUnixTimestamp(), sessionId);
				}
				addPaymentRequestTransaction(pr.getId(), t.getId());
				return;
			}
		}
		
	}
	
	
	public static List<Message> getAllUnreadMessages(int sessionId){
		String sql = "SELECT * FROM messages WHERE id IN (SELECT id FROM messageIds WHERE session = ?) AND read = 0";
		try(Connection conn = connect()){
			return MessageService.getMessages(runPreparedStatementQuery(conn, sql, sessionId));
		} catch (SQLException e) {
			System.out.println("Error getting all messages, sql error: " + e.getMessage());
		}
		
		return null;
	}
	
	/**
	 * Check if a message with the given id exists in a session.
	 * @param sessionId
	 * @param messageId
	 * @return boolean true if exists, false otherwise
	 */
	public static boolean messageExists(int sessionId, int messageId) {
		String sql = "SELECT * FROM messages WHERE id IN (SELECT id FROM messageIds WHERE session = ?) AND id = ?";
		
		try(Connection conn = connect()) {
			ResultSet rs = runPreparedStatementQuery(conn, sql, sessionId, messageId);
			if (rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean messageWithBalanceHighExists(int sessionId) {
		String sql = "SELECT * FROM messages WHERE id IN (SELECT id FROM messageIds WHERE session = ?) AND message LIKE ? AND read = 0";
		
		try(Connection conn = connect()) {
			ResultSet rs = runPreparedStatementQuery(conn, sql, sessionId, "Your balance reached a new high%");
			if (rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void addMessage(String message, MessageType type, long unixTimestamp, int sessionId) {
		// Generate new id
		int newId = DatabaseCommunication.getLastMessageID() + 1;
		
		
		String sql = "INSERT INTO messages(id, message, date, type) VALUES(?,?,?,?)";
		
		runPreparedStatementUpdate(sql, newId, message, unixTimestamp, type.toString());
		
		addMessageId(sessionId, newId);
	}
	
	public static void readMessage(int sessionId, int messageId) {
		String sql = "UPDATE messages SET read = 1 WHERE id IN (SELECT id FROM messageIds WHERE session = ?) AND id = ?";
		runPreparedStatementUpdate(sql, sessionId, messageId);
	}
	
	public static void fillPaymentRequest(int paymentRequestId) {
		String sql = "UPDATE paymentRequests SET filled = 1 WHERE id = ?";
		runPreparedStatementUpdate(sql, paymentRequestId);

		
	}
	
	/**
	 * Performs checks and adds messages whenever is the case
	 * @param sessionId Id of the session for which checks are made
	 */
	public static void applyMessages(int sessionId, long unixTimestamp) {
		double bal = getBalance(sessionId);
		
		if (bal < 0) {
			String msg = "Balance dropped below zero!";
			addMessage(msg, MessageType.warning, unixTimestamp, sessionId);
		}
		
		Transaction first = getFirstTransaction(sessionId);
		Transaction last = getLastTransaction(sessionId);
		
		// If there is at least one transaction
		if (first != null) {
			ZonedDateTime firstDate = ZonedDateTime.ofInstant(Instant.ofEpochSecond(first.returnUnixTimestamp()), ZoneOffset.UTC);
			ZonedDateTime lastDate = ZonedDateTime.ofInstant(Instant.ofEpochSecond(last.returnUnixTimestamp()), ZoneOffset.UTC);
			// If there is at least 3 months of data available
			if (getMonthDiff(firstDate, lastDate) >= 3) {
				List<Transaction> allTransactions = getAllTransactions(sessionId);
				double maxAmount = getCandlestick(allTransactions, 0, null).getHigh();
				// If there isn't already an unread message about the new high of the balance
				if (!messageWithBalanceHighExists(sessionId)) {
					// Generate an info message and add it
					String msg = "Your balance reached a new high of " + maxAmount + "!";
					addMessage(msg, MessageType.info, unixTimestamp, sessionId);
				}
			}
		}
		
		// Add a warning for every payment request not filled on time
		List<PaymentRequest> paymentRequests = getAllUnfilledAndExpiredPaymentRequests(sessionId, unixTimestamp);
		for (PaymentRequest pr: paymentRequests) {
			String msg = "Payment request with id " + pr.getId() + " has not been filled on time!";
			addMessage(msg, MessageType.warning, unixTimestamp, sessionId);
		}
	}
	
	
	
	

	
	public static void fillStatementWithParams(PreparedStatement stmt, Object[] params) {
		for (int i = 0; i < params.length; i++) {
			try {
				stmt.setObject(i + 1, params[i]);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static ResultSet runPreparedStatementQuery(Connection conn, String sql, Object... params) throws SQLException {
		PreparedStatement statement = conn.prepareStatement(sql);
		fillStatementWithParams(statement, params);
		return statement.executeQuery();
	}
	
	private static void runPreparedStatementUpdate(String sql, Object... params) {
		try(Connection conn = connect()){
			PreparedStatement statement = conn.prepareStatement(sql);
			fillStatementWithParams(statement, params);
			statement.executeUpdate();
		} catch (SQLException e) {
			System.out.println(sql);
			System.out.println(e.getMessage());
		}
	}
	
	private static int getMonthDiff(ZonedDateTime t1, ZonedDateTime t2) {
		ZonedDateTime earlier;
		ZonedDateTime later;
		if (t1.isAfter(t2)) {
			later = t1;
			earlier = t2;
		} else {
			later = t2;
			earlier = t1;
		}
		return Math.max((later.getYear() - earlier.getYear()) * 12 +  
				   (later.getMonthValue() - earlier.getMonthValue()), 0);
	}
	



	public static int getLastTransactionID() {
		return getMaxIndex("transactions");
	}
	
	public static int getLastMessageID() {
		return getMaxIndex("messages");
	}



	public static int getLastCategoryID() {
		return getMaxIndex("categories");
	}
	
	public static int getLastCategoryRuleID() {
		return getMaxIndex("categoryRules");
	}
	
	public static int getLastSavingGoalID() {
		return getMaxIndex("savingGoals");
	}
	
	public static int getLastPaymentRequestID() {
		return getMaxIndex("paymentRequests");
	}
	
	public static void main(String[] args) {
		/*String sql = "PRAGMA foreign_keys";
        try (Connection conn = connect();
        		Statement st = conn.createStatement()){
        		ResultSet rs = st.executeQuery(sql);
        		System.out.println(rs.getInt(1));
        } catch (SQLException e) {
        		System.out.println(e.getMessage());
        }*/
		//deleteSavingGoal(2,1);
	}






}
