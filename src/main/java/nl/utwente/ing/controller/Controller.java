package nl.utwente.ing.controller;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import nl.utwente.ing.database.DatabaseCommunication;
import nl.utwente.ing.model.CandleStick;
import nl.utwente.ing.model.Category;
import nl.utwente.ing.model.CategoryRule;
import nl.utwente.ing.model.SavingGoal;
import nl.utwente.ing.model.TimeInterval;
import nl.utwente.ing.model.Transaction;


@RestController
@RequestMapping(value = "/api/v1" , produces = "application/json", consumes = "application/json")
public class Controller {
	// ---------------- Helper Methods --------------------
	/**
	 * Checks if the session id is valid
	 * @param X_session_ID SessionID given in the header
	 * @param session_id SessionID given as a parameter
	 * @return valid sessionID
	 */
	public String checkSession(String X_session_ID, String session_id) {
		// Throw an exception if the two session variables are both null or both are not null but are different from each other
		if ((X_session_ID == null && session_id == null) ||
				(X_session_ID != null && session_id != null && !X_session_ID.equals(session_id))
				) {
			throw new SessionIDException();
		}
		
		// Only the case in which one of them is null remains
		// We use X_session_ID to represent the actual session id and proceed from there
		if (X_session_ID == null) {
			X_session_ID = session_id;
		}
		
		// If the session id is not in the session, throw an exception
		if (!DatabaseCommunication.validSessionId(Integer.parseInt(X_session_ID))) {
			throw new SessionIDException();
		}
		
		return X_session_ID;
	}
	
	// ---------------- Exception handling --------------------
	@ResponseStatus(value=HttpStatus.METHOD_NOT_ALLOWED,
            reason="Invalid input given")  // 405
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public void invalidInput() {}
	
	// ---------------- Responses --------------------
	// No/Wrong sessionID
	@SuppressWarnings("serial")
	@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason="Session ID is missing or invalid")
	public class SessionIDException extends RuntimeException {}
	
	// Invalid input
	@SuppressWarnings("serial")
	@ResponseStatus(value = HttpStatus.METHOD_NOT_ALLOWED, reason="Invalid input given")
	public class InvalidInputException extends RuntimeException {}
	
	// Invalid input
	@SuppressWarnings("serial")
	@ResponseStatus(value = HttpStatus.NOT_FOUND, reason="Item(s) not found")
	public class ItemNotFound extends RuntimeException {}
	
	
	// ---------------- Transactions -----------------
	// GET - Offset, limit and category parameter
	
	@RequestMapping(value = "/transactions")
	public List<Transaction> getAllTransactions(
			@RequestParam(value="offset", defaultValue="0") int offset,
			@RequestParam(value="limit", defaultValue="20") int limit,
			@RequestParam(value="category", defaultValue="-1") int categoryID,
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		int sessionId = Integer.parseInt(checkSession(X_session_ID, session_id));
		
		// Enforce the limits for offset and limit
		offset = Math.max(offset, 0);
		limit = Math.max(limit, 1);
		limit = Math.min(limit, 100);
		
		
		return DatabaseCommunication.getAllExternalTransactions(offset, limit, categoryID, sessionId);
	}
	
	// POST
	@RequestMapping(method = RequestMethod.POST, value = "/transactions")
	public ResponseEntity<Transaction> addTransaction(
			@RequestBody Transaction t,
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		
		int sessionId = Integer.parseInt(checkSession(X_session_ID, session_id));
		
		
		// If it's not a valid transaction
		if(t == null || !t.validTransaction()) {
			throw new InvalidInputException();
		}
		
		
		
		// Create a response add the created object to it
		ResponseEntity<Transaction> response = new ResponseEntity<Transaction>(DatabaseCommunication.addTransaction(t, sessionId) , HttpStatus.CREATED);
		
		return response;
	}
	
	// GET
	@RequestMapping("/transactions/{id}")
	public Transaction getTransaction(
			@PathVariable int id,
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		int sessionId = Integer.parseInt(checkSession(X_session_ID, session_id));
		
		Transaction transaction = DatabaseCommunication.getTransaction(id, sessionId);
		if (transaction == null) {
			throw new ItemNotFound();
		}
		
		return transaction;
	}
	
	// PUT
	@RequestMapping(method = RequestMethod.PUT, value = "/transactions/{id}")
	public ResponseEntity<Transaction> updateTransaction(
			@RequestBody Transaction t ,
			@PathVariable int id,
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		int sessionId = Integer.parseInt(checkSession(X_session_ID, session_id));
		
		if(t == null || !t.validTransaction()) {
			throw new InvalidInputException();
		}
		
		
		if (DatabaseCommunication.getTransaction(id, sessionId) == null) {
			throw new ItemNotFound();
		}
		DatabaseCommunication.updateTransaction(t ,id, sessionId);
		
		return new ResponseEntity<Transaction>(DatabaseCommunication.getTransaction(id, sessionId), HttpStatus.OK);
	}
	
	// DELETE
	@SuppressWarnings("rawtypes") // Because we don't care about RensponseEntity's generic type
	@RequestMapping(method = RequestMethod.DELETE, value = "/transactions/{id}")
	public ResponseEntity deleteTransaction(
			@PathVariable int id,
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		int sessionId = Integer.parseInt(checkSession(X_session_ID, session_id));
		
		
		if (DatabaseCommunication.getTransaction(id, sessionId) == null) {
			throw new ItemNotFound();
		}
		DatabaseCommunication.deleteTransaction(id, sessionId);
		
		
		return new ResponseEntity(HttpStatus.NO_CONTENT);
	}
	
	// PATCH
	@RequestMapping(method = RequestMethod.PATCH, value = "/transactions/{transactionID}/category")
	public ResponseEntity<Transaction> assignCategory(
			@RequestBody String category_id,
			@PathVariable int transactionID,
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		int sessionId = Integer.parseInt(checkSession(X_session_ID, session_id));
		

		
		// Create a JSON object
		JSONObject category;
		int categoryID;
		try {
			category = new JSONObject(category_id);
			categoryID = category.getInt("category_id");
		} catch (JSONException e) {
			throw new ItemNotFound();
		}
		
		if (DatabaseCommunication.getTransaction(transactionID, sessionId) == null || 
				DatabaseCommunication.getCategory(categoryID, sessionId) == null) {
			throw new ItemNotFound();
		}
		
		DatabaseCommunication.assignCategory(categoryID, transactionID);
		
		return new ResponseEntity<Transaction>(DatabaseCommunication.getTransaction(transactionID, sessionId), HttpStatus.OK);
	}
	
	// ---------------- Categories -----------------
	
	// GET
	@RequestMapping("/categories")
	public List<Category> getCategories(
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		int sessionId = Integer.parseInt(checkSession(X_session_ID, session_id));
		
		return DatabaseCommunication.getAllCategories(sessionId);
	}
	
	// POST
	@RequestMapping(method = RequestMethod.POST, value = "/categories")
	public ResponseEntity<Category> addCategory(
			@RequestBody Category category,
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		
		int sessionId = Integer.parseInt(checkSession(X_session_ID, session_id));
		
		if (category == null || !category.validCategory()) {
			throw new InvalidInputException();
		}
		
		
		return new ResponseEntity<Category>(DatabaseCommunication.addCategory(category, sessionId) ,HttpStatus.CREATED);
	}
	
	// GET
	@RequestMapping("/categories/{id}")
	public Category getCategory(
			@PathVariable int id,
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		int sessionId = Integer.parseInt(checkSession(X_session_ID, session_id));
		
		
		Category category = DatabaseCommunication.getCategory(id, sessionId);
		if (category == null) {
			throw new ItemNotFound();
		}
		
		return category;
	}
	
	// PUT
	@RequestMapping(method = RequestMethod.PUT, value = "/categories/{id}")
	public ResponseEntity<Category> putCategory(
			@RequestBody Category category ,
			@RequestParam(value="session_id", required =false) String session_id,
			@PathVariable int id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		int sessionId = Integer.parseInt(checkSession(X_session_ID, session_id));
		
		if (category == null || !category.validCategory()) {
			throw new InvalidInputException();
		}
		
		
		if (DatabaseCommunication.getCategory(id, sessionId) == null) {
			throw new ItemNotFound();
		}
		
		DatabaseCommunication.updateCategory(category, id, sessionId);
		
		return new ResponseEntity<Category>(DatabaseCommunication.getCategory(id, sessionId), HttpStatus.OK);
	}
	
	// DELETE
	@RequestMapping(method = RequestMethod.DELETE, value = "/categories/{id}")
	public ResponseEntity<Category> deleteCategory(
			@PathVariable int id,
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		int sessionId = Integer.parseInt(checkSession(X_session_ID, session_id));
		
		
		if (DatabaseCommunication.getCategory(id, sessionId) == null) {
			throw new ItemNotFound();
		}
		
		DatabaseCommunication.deleteCategory(id, sessionId);
		
		
		return new ResponseEntity<Category>(HttpStatus.NO_CONTENT);
	}
	
	// ---------------- Sessions -----------------
	// POST
	@RequestMapping(value = "/sessions", method = RequestMethod.POST, produces = "application/json", consumes = "*")
	public String getSessionId() {
		// One more than the maximum session Id present.
		int newSessionId = DatabaseCommunication.getMaxSessionId() + 1;
		DatabaseCommunication.addSession(newSessionId);
		return "{\n" + 
				"  \"id\": \"" + newSessionId + "\"\n" + 
				"}";
	}
	
	// ---------------- CategoryRules -----------------
	// GET
	@RequestMapping("/categoryRules")
	public List<CategoryRule> getCategoryRules(
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		int sessionId = Integer.parseInt(checkSession(X_session_ID, session_id));
		
		return DatabaseCommunication.getAllCategoryRules(sessionId);
	}
	
	// POST
	@RequestMapping(method = RequestMethod.POST, value = "/categoryRules")
	public ResponseEntity<CategoryRule> addCategoryRule(
			@RequestBody CategoryRule categoryRule,
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		
		int sessionId = Integer.parseInt(checkSession(X_session_ID, session_id));
		
		if (categoryRule == null || !categoryRule.validCategoryRule()) {
			throw new InvalidInputException();
		}
		
		
		
		//Add the category rule id to the session
		
		CategoryRule addedCategoryRule = DatabaseCommunication.addCategoryRule(categoryRule, sessionId);
		
		// If it is apply on history, try to apply it on previous transactions
		if (addedCategoryRule.isApplyOnHistory() && DatabaseCommunication.getCategory(addedCategoryRule.getCategory_id(), sessionId)!= null) {
			DatabaseCommunication.applyCategoryRuleOnHistory(addedCategoryRule, sessionId);
		}
		
		return new ResponseEntity<CategoryRule>(addedCategoryRule ,HttpStatus.CREATED);
	}
	
	// GET
	@RequestMapping("/categoryRules/{id}")
	public CategoryRule getCategoryRule(
			@PathVariable int id,
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		int sessionId = Integer.parseInt(checkSession(X_session_ID, session_id));
		
		
		CategoryRule categoryRule = DatabaseCommunication.getCategoryRule(id, sessionId);
		if (categoryRule == null) {
			throw new ItemNotFound();
		}
		
		return categoryRule;
	}
	
	// PUT
	@RequestMapping(method = RequestMethod.PUT, value = "/categoryRules/{id}")
	public ResponseEntity<CategoryRule> putCategoryRule(
			@RequestBody CategoryRule categoryRule ,
			@RequestParam(value="session_id", required =false) String session_id,
			@PathVariable int id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		int sessionId = Integer.parseInt(checkSession(X_session_ID, session_id));
		
		if (categoryRule == null || !categoryRule.validCategoryRule()) {
			throw new InvalidInputException();
		}
		
		
		if (DatabaseCommunication.getCategoryRule(id, sessionId) == null) {
			throw new ItemNotFound();
		}
		
		DatabaseCommunication.updateCategoryRule(categoryRule, id, sessionId);
		
		return new ResponseEntity<CategoryRule>(DatabaseCommunication.getCategoryRule(id, sessionId), HttpStatus.OK);
	}
	
	// DELETE
	@RequestMapping(method = RequestMethod.DELETE, value = "/categoryRules/{id}")
	public ResponseEntity<CategoryRule> deleteCategoryRule(
			@PathVariable int id,
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		int sessionId = Integer.parseInt(checkSession(X_session_ID, session_id));
		
		
		if (DatabaseCommunication.getCategoryRule(id, sessionId) == null) {
			throw new ItemNotFound();
		}
		
		DatabaseCommunication.deleteCategoryRule(id, sessionId);
		
		
		return new ResponseEntity<CategoryRule>(HttpStatus.NO_CONTENT);
	}

	// ---------------- Balance History -----------------
	// GET
	@RequestMapping("/balance/history")
	public List<CandleStick> getBalanceHistory(
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID,
			@RequestParam(value="interval", defaultValue="month", required=false) String interval,
			@RequestParam(value="intervals", defaultValue="24", required=false) int intervals) {
		int sessionId = Integer.parseInt(checkSession(X_session_ID, session_id));
		
		// Minimum value
		intervals = Math.max(intervals, 1);
		
		// Maximum value
		intervals = Math.min(intervals, 200);
		
		ChronoUnit time = null;
		
		try {
			time = TimeInterval.valueOf(interval.toUpperCase()).getUnit();
		} catch (IllegalArgumentException e) {
			throw new InvalidInputException();
		}
		
		
		return DatabaseCommunication.getBalanceHistory(sessionId, time, intervals);
	}
	// ---------------- Saving Goals -----------------
	// GET
	@RequestMapping("/savingGoals")
	public List<SavingGoal> getSavingGoals(
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		int sessionId = Integer.parseInt(checkSession(X_session_ID, session_id));
		
		
		return DatabaseCommunication.getAllSavingGoals(sessionId);
	}
	
	// POST
	@RequestMapping(method = RequestMethod.POST, value = "/savingGoals")
	public ResponseEntity<SavingGoal> addSavingGoal(
			@RequestBody SavingGoal savingGoal,
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		
		int sessionId = Integer.parseInt(checkSession(X_session_ID, session_id));
		
		if (savingGoal == null || !savingGoal.validSavingGoal()) {
			throw new InvalidInputException();
		}
		
		
		return new ResponseEntity<SavingGoal>(DatabaseCommunication.addSavingGoal(savingGoal, sessionId) ,HttpStatus.CREATED);
	}
	
	// DELETE
	@RequestMapping(method = RequestMethod.DELETE, value = "/savingGoals/{id}")
	public ResponseEntity<SavingGoal> deleteSavingGoal(
			@PathVariable int id,
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		int sessionId = Integer.parseInt(checkSession(X_session_ID, session_id));
		
		
		if (DatabaseCommunication.getSavingGoal(id, sessionId) == null) {
			throw new ItemNotFound();
		}
		
		DatabaseCommunication.deleteSavingGoal(id, sessionId);
		
		
		return new ResponseEntity<SavingGoal>(HttpStatus.NO_CONTENT);
	}
}
